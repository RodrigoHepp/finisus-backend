package com.finisus.application.service;

import com.finisus.application.ports.in.BancoUseCase;
import com.finisus.application.ports.in.FaturaUseCase;
import com.finisus.application.ports.in.ImportacaoFinanceiraUseCase;
import com.finisus.application.ports.in.RegistrarTransacaoUseCase;
import com.finisus.application.ports.out.ImportacaoFinanceiraRepositoryPort;
import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Fatura;
import com.finisus.domain.model.ImportacaoFinanceira;
import com.finisus.domain.model.LancamentoImportado;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class ImportacaoFinanceiraService implements ImportacaoFinanceiraUseCase {
	private static final int TAMANHO_MAXIMO_ARQUIVO = 10 * 1024 * 1024;
	private final BancoUseCase bancos;
	private final ImportacaoFinanceiraRepositoryPort importacoes;
	private final LeitorDocumentoFinanceiroPort leitor;
	private final TransacaoRepositoryPort transacoes;
	private final RegistrarTransacaoUseCase registrarTransacao;
	private final FaturaUseCase faturas;

	public ImportacaoFinanceiraService(BancoUseCase bancos, ImportacaoFinanceiraRepositoryPort importacoes,
			LeitorDocumentoFinanceiroPort leitor, TransacaoRepositoryPort transacoes,
			RegistrarTransacaoUseCase registrarTransacao, FaturaUseCase faturas) {
		this.bancos = bancos;
		this.importacoes = importacoes;
		this.leitor = leitor;
		this.transacoes = transacoes;
		this.registrarTransacao = registrarTransacao;
		this.faturas = faturas;
	}

	@Override
	@Transactional
	public Revisao iniciar(Long usuarioId, IniciarCommand command) {
		validarArquivo(command);
		var banco = bancos.buscar(usuarioId, command.bancoId());
		var lido = leitor.ler(new LeitorDocumentoFinanceiroPort.ArquivoPdf(command.conteudo(), command.nomeArquivo(),
				banco.getCodigo(), banco.getNome()));
		ImportacaoFinanceira importacao = importacoes.salvar(ImportacaoFinanceira.nova(usuarioId, banco.getId(),
				limitar(command.nomeArquivo(), 255), sha256(command.conteudo()), lido.leitor(), lido.tipoDocumento(), lido.identificadorOrigem(),
				lido.periodoInicio(), lido.periodoFim(), lido.dataVencimento(), lido.saldoInicial(), lido.saldoFinal(),
				lido.valorTotal(), lido.lancamentos()));
		return revisao(usuarioId, importacao);
	}

	@Override
	@Transactional(readOnly = true)
	public Revisao buscar(Long usuarioId, Long importacaoId) {
		return revisao(usuarioId, exigirImportacao(importacaoId, usuarioId));
	}

	@Override
	@Transactional
	public Revisao revisar(Long usuarioId, Long importacaoId, RevisarCommand command) {
		ImportacaoFinanceira importacao = exigirImportacaoParaAtualizacao(importacaoId, usuarioId);
		List<ImportacaoFinanceira.RevisaoLancamento> revisoes = command.lancamentos().stream()
				.map(l -> new ImportacaoFinanceira.RevisaoLancamento(l.id(), l.data(), limitar(l.descricao(), 500),
						l.valor(), l.tipo(), l.importar(), l.categoriaId(), l.itemId())).toList();
		importacao.revisar(command.contaId(), command.faturaId(), revisoes);
		return revisao(usuarioId, importacoes.salvar(importacao));
	}

	@Override
	@Transactional
	public ImportacaoFinanceira confirmar(Long usuarioId, Long importacaoId) {
		ImportacaoFinanceira importacao = exigirImportacaoParaAtualizacao(importacaoId, usuarioId);
		importacao.confirmar();
		Fatura fatura = null;
		if (importacao.getTipoDocumento() == TipoDocumentoFinanceiro.FATURA_CARTAO) {
			fatura = faturas.buscar(usuarioId, importacao.getFaturaId());
		}
		for (LancamentoImportado lancamento : importacao.getLancamentos()) {
			if (!lancamento.isImportar()) continue;
			if (importacao.getTipoDocumento() == TipoDocumentoFinanceiro.FATURA_CARTAO) {
				if (lancamento.getTipo() != TipoTransacao.SAIDA)
					throw new DomainException("error.importacao.revisao.invalida");
				Transacao transacao = faturas.lancarGasto(usuarioId, new FaturaUseCase.LancarGastoCommand(fatura.getId(),
						lancamento.getValor(), lancamento.getData(), lancamento.getDescricao(),
						fatura.getContaPagamentoId(), lancamento.getCategoriaId(), itens(lancamento)));
				lancamento.vincularTransacao(transacao.getId());
			} else {
				Transacao transacao = registrarTransacao.registrar(usuarioId,
						new com.finisus.application.ports.in.TransacaoUseCase.RegistrarCommand(lancamento.getTipo(),
								lancamento.getValor(), lancamento.getData(), lancamento.getDescricao(), importacao.getContaId(),
								lancamento.getCategoriaId(), null, itens(lancamento)));
				lancamento.vincularTransacao(transacao.getId());
			}
		}
		return importacoes.salvar(importacao);
	}

	private List<com.finisus.application.ports.in.TransacaoUseCase.ItemCommand> itens(LancamentoImportado lancamento) {
		return lancamento.getItemId() == null ? List.of()
				: List.of(new com.finisus.application.ports.in.TransacaoUseCase.ItemCommand(lancamento.getItemId(),
						lancamento.getValor()));
	}

	private Revisao revisao(Long usuarioId, ImportacaoFinanceira importacao) {
		List<Transacao> existentes = transacoes.listarPorUsuario(usuarioId);
		List<PossivelDuplicidade> duplicidades = importacao.getLancamentos().stream().filter(LancamentoImportado::isImportar)
				.filter(l -> l.getData() != null && l.getValor() != null && l.getDescricao() != null)
				.flatMap(l -> existentes.stream().filter(t -> mesmoAlvo(importacao, t)).filter(t -> t.getData().equals(l.getData()))
						.filter(t -> t.getValor().valor().compareTo(l.getValor()) == 0)
						.filter(t -> normalizar(t.getDescricao()).equals(normalizar(l.getDescricao())))
						.map(t -> new PossivelDuplicidade(l.getId(), t.getId(), t.getData(), t.getValor().valor(),
								t.getDescricao()))).toList();
		return new Revisao(importacao, duplicidades);
	}

	private boolean mesmoAlvo(ImportacaoFinanceira importacao, Transacao transacao) {
		return importacao.getTipoDocumento() == TipoDocumentoFinanceiro.FATURA_CARTAO
				? importacao.getFaturaId() != null && importacao.getFaturaId().equals(transacao.getFaturaId())
				: importacao.getContaId() != null && importacao.getContaId().equals(transacao.getContaId());
	}

	private ImportacaoFinanceira exigirImportacao(Long id, Long usuarioId) {
		return importacoes.buscarPorIdEUsuario(id, usuarioId).orElseThrow(this::notFound);
	}

	private ImportacaoFinanceira exigirImportacaoParaAtualizacao(Long id, Long usuarioId) {
		return importacoes.buscarPorIdEUsuarioParaAtualizacao(id, usuarioId).orElseThrow(this::notFound);
	}

	private void validarArquivo(IniciarCommand command) {
		if (command == null || command.bancoId() == null || command.conteudo() == null || command.conteudo().length == 0
				|| command.conteudo().length > TAMANHO_MAXIMO_ARQUIVO || command.nomeArquivo() == null
				|| command.nomeArquivo().isBlank() || !iniciaComPdf(command.conteudo()))
			throw new DomainException("error.importacao.arquivo.invalido");
	}

	private boolean iniciaComPdf(byte[] conteudo) {
		return conteudo.length >= 5 && new String(conteudo, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-");
	}

	private String sha256(byte[] conteudo) {
		try {
			return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(conteudo));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private String normalizar(String valor) { return valor.replaceAll("\\s+", " ").trim().toLowerCase(); }
	private String limitar(String valor, int tamanho) { return valor == null ? null : valor.substring(0, Math.min(valor.length(), tamanho)); }
	private DomainException notFound() { return new DomainException("error.recurso.nao.encontrado"); }
}
