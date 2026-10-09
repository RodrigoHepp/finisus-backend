package com.finisus.application.service;

import com.finisus.application.ports.in.BancoUseCase;
import com.finisus.application.ports.in.FaturaUseCase;
import com.finisus.application.ports.in.ImportacaoFinanceiraUseCase;
import com.finisus.application.ports.in.RegistrarTransacaoUseCase;
import com.finisus.application.ports.in.ObrigacaoFinanceiraUseCase;
import com.finisus.application.ports.out.ImportacaoFinanceiraRepositoryPort;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.ItemRepositoryPort;
import com.finisus.application.ports.out.ObrigacaoFinanceiraRepositoryPort;
import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.application.ports.out.EventoOperacionalPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Fatura;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.ImportacaoFinanceira;
import com.finisus.domain.model.LancamentoImportado;
import com.finisus.domain.model.StatusImportacaoFinanceira;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
	private final ObrigacaoFinanceiraUseCase obrigacoes;
	private final ObrigacaoFinanceiraRepositoryPort obrigacoesRepository;
	private final ContaRepositoryPort contas;
	private final CategoriaRepositoryPort categorias;
	private final ItemRepositoryPort itens;
	private final UsuarioRepositoryPort usuarios;
	private final EventoOperacionalPort eventos;

	public ImportacaoFinanceiraService(BancoUseCase bancos, ImportacaoFinanceiraRepositoryPort importacoes,
			LeitorDocumentoFinanceiroPort leitor, TransacaoRepositoryPort transacoes,
			RegistrarTransacaoUseCase registrarTransacao, FaturaUseCase faturas, ObrigacaoFinanceiraUseCase obrigacoes,
			ObrigacaoFinanceiraRepositoryPort obrigacoesRepository,
			ContaRepositoryPort contas, CategoriaRepositoryPort categorias, ItemRepositoryPort itens,
			UsuarioRepositoryPort usuarios) {
		this(bancos, importacoes, leitor, transacoes, registrarTransacao, faturas, obrigacoes, obrigacoesRepository,
				contas, categorias, itens, usuarios, EventoOperacionalPort.NENHUM);
	}

	public ImportacaoFinanceiraService(BancoUseCase bancos, ImportacaoFinanceiraRepositoryPort importacoes,
			LeitorDocumentoFinanceiroPort leitor, TransacaoRepositoryPort transacoes,
			RegistrarTransacaoUseCase registrarTransacao, FaturaUseCase faturas, ObrigacaoFinanceiraUseCase obrigacoes,
			ObrigacaoFinanceiraRepositoryPort obrigacoesRepository,
			ContaRepositoryPort contas, CategoriaRepositoryPort categorias, ItemRepositoryPort itens,
			UsuarioRepositoryPort usuarios, EventoOperacionalPort eventos) {
		this.bancos = bancos;
		this.importacoes = importacoes;
		this.leitor = leitor;
		this.transacoes = transacoes;
		this.registrarTransacao = registrarTransacao;
		this.faturas = faturas;
		this.obrigacoes = obrigacoes;
		this.obrigacoesRepository = obrigacoesRepository;
		this.contas = contas;
		this.categorias = categorias;
		this.itens = itens;
		this.usuarios = usuarios;
		this.eventos = eventos;
	}

	@Override
	@Transactional
	public Revisao iniciar(Long usuarioId, IniciarCommand command) {
		validarArquivo(command);
		var banco = bancos.buscar(usuarioId, command.bancoId());
		validarDestinoInicial(usuarioId, command, banco.getId());
		String hashArquivo = sha256(command.conteudo());
		var existente = importacoes.buscarPorUsuarioBancoEHash(usuarioId, banco.getId(), hashArquivo);
		if (existente.isPresent()) {
			validarMesmoContexto(existente.get(), command);
			eventos.registrar("duplicidade_importacao_evitada");
			return revisao(usuarioId, existente.get());
		}
		var lido = leitor.ler(new LeitorDocumentoFinanceiroPort.ArquivoPdf(command.conteudo(), command.nomeArquivo(),
				banco.getCodigo(), banco.getNome()));
		usuarios.buscarPorIdParaAtualizacao(usuarioId).orElseThrow(this::notFound);
		existente = importacoes.buscarPorUsuarioBancoEHash(usuarioId, banco.getId(), hashArquivo);
		if (existente.isPresent()) {
			validarMesmoContexto(existente.get(), command);
			eventos.registrar("duplicidade_importacao_evitada");
			return revisao(usuarioId, existente.get());
		}
		TipoDocumentoFinanceiro tipoPretendido = command.tipoPretendido() == null ? lido.tipoDocumento()
				: command.tipoPretendido();
		ImportacaoFinanceira importacao = importacoes.salvar(ImportacaoFinanceira.nova(usuarioId, banco.getId(),
				limitar(command.nomeArquivo(), 255), hashArquivo, lido.leitor(), lido.tipoDocumento(), tipoPretendido,
				lido.identificadorOrigem(),
				lido.periodoInicio(), lido.periodoFim(), lido.dataVencimento(), lido.saldoInicial(), lido.saldoFinal(),
				lido.valorTotal(), command.contaId(), command.faturaId(), lido.lancamentos()));
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
		validarReferenciasDaRevisao(usuarioId, importacao, command);
		List<ImportacaoFinanceira.RevisaoLancamento> revisoes = command.lancamentos().stream()
				.map(l -> new ImportacaoFinanceira.RevisaoLancamento(l.id(), l.data(), limitar(l.descricao(), 500),
						l.valor(), l.tipo(), l.importar(), l.categoriaId(), l.itemId(), l.transacaoId(),
						l.obrigacaoFinanceiraId(), limitar(l.justificativa(), 500))).toList();
		importacao.revisar(usuarioId, command.contaId(), command.faturaId(), revisoes, LocalDateTime.now());
		return revisao(usuarioId, importacoes.salvar(importacao));
	}

	private void validarReferenciasDaRevisao(Long usuarioId, ImportacaoFinanceira importacao, RevisarCommand command) {
		if (command.contaId() != null && contas.buscarPorIdEUsuario(command.contaId(), usuarioId).isEmpty()) {
			throw notFound();
		}
		if (command.faturaId() != null) {
			faturas.buscar(usuarioId, command.faturaId());
		}
		for (var lancamento : command.lancamentos()) {
			validarAssociacao(importacao, command, lancamento, usuarioId);
			if (lancamento.categoriaId() != null)
				CategoriaAtivaValidator.exigirAtiva(categorias.buscarPorIdEUsuario(lancamento.categoriaId(), usuarioId)
						.orElseThrow(this::notFound));
			if (lancamento.itemId() != null && itens.buscarPorIdEUsuario(lancamento.itemId(), usuarioId).isEmpty()) {
				throw notFound();
			}
		}
	}

	private void validarAssociacao(ImportacaoFinanceira importacao, RevisarCommand revisao,
			LancamentoCommand lancamento, Long usuarioId) {
		if (lancamento.transacaoId() != null && lancamento.obrigacaoFinanceiraId() != null) {
			throw new DomainException("error.importacao.revisao.invalida");
		}
		if (lancamento.transacaoId() != null) {
			if (importacao.getTipoDocumento() == TipoDocumentoFinanceiro.COBRANCA) {
				throw new DomainException("error.importacao.revisao.invalida");
			}
			Transacao transacao = transacoes.buscarPorIdEUsuario(lancamento.transacaoId(), usuarioId)
					.orElseThrow(this::notFound);
			boolean mesmoAlvo = importacao.getTipoDocumento() == TipoDocumentoFinanceiro.FATURA_CARTAO
					? revisao.faturaId() != null && revisao.faturaId().equals(transacao.getFaturaId())
					: revisao.contaId() != null && revisao.contaId().equals(transacao.getContaId());
			boolean mesmosDados = lancamento.data() != null && lancamento.data().equals(transacao.getData())
					&& lancamento.valor() != null
					&& lancamento.valor().compareTo(transacao.getValor().valor()) == 0
					&& lancamento.tipo() == transacao.getTipo();
			if (!mesmoAlvo || !mesmosDados || transacao.isEstornada()) {
				throw new DomainException("error.importacao.revisao.invalida");
			}
		}
		if (lancamento.obrigacaoFinanceiraId() != null) {
			if (importacao.getTipoDocumento() != TipoDocumentoFinanceiro.COBRANCA) {
				throw new DomainException("error.importacao.revisao.invalida");
			}
			var obrigacao = obrigacoes.buscar(usuarioId, lancamento.obrigacaoFinanceiraId());
			if (revisao.contaId() == null || !revisao.contaId().equals(obrigacao.getContaPagamentoId())) {
				throw new DomainException("error.importacao.revisao.invalida");
			}
		}
	}

	@Override
	@Transactional
	public ImportacaoFinanceira confirmar(Long usuarioId, Long importacaoId) {
		ImportacaoFinanceira importacao = exigirImportacaoParaAtualizacao(importacaoId, usuarioId);
		if (importacao.getStatus() == StatusImportacaoFinanceira.CONFIRMADA) {
			eventos.registrar("duplicidade_importacao_evitada");
			return importacao;
		}
		importacao.confirmar();
		Fatura fatura = null;
		if (importacao.getTipoDocumento() == TipoDocumentoFinanceiro.FATURA_CARTAO) {
			fatura = faturas.buscar(usuarioId, importacao.getFaturaId());
		}
		for (LancamentoImportado lancamento : importacao.getLancamentos()) {
			if (!lancamento.isImportar()) continue;
			if (lancamento.getTransacaoId() != null || lancamento.getObrigacaoFinanceiraId() != null) continue;
			if (importacao.getTipoDocumento() == TipoDocumentoFinanceiro.FATURA_CARTAO) {
				var command = new FaturaUseCase.LancarGastoCommand(fatura.getId(),
						lancamento.getValor(), lancamento.getData(), lancamento.getDescricao(),
						fatura.getContaPagamentoId(), lancamento.getCategoriaId(), itens(lancamento));
				Transacao transacao = lancamento.getTipo() == TipoTransacao.ENTRADA
						? faturas.lancarCredito(usuarioId, command) : faturas.lancarGasto(usuarioId, command);
				lancamento.marcarCriadaComTransacao(transacao.getId());
			} else if (importacao.getTipoDocumento() == TipoDocumentoFinanceiro.COBRANCA) {
				if (lancamento.getTipo() != TipoTransacao.SAIDA) {
					throw new DomainException("error.importacao.revisao.invalida");
				}
				var obrigacao = obrigacoes.criar(usuarioId, new ObrigacaoFinanceiraUseCase.CriarCommand(
						lancamento.getDescricao(), credor(importacao), lancamento.getValor(), lancamento.getData(),
						importacao.getContaId(), lancamento.getCategoriaId()));
				lancamento.marcarCriadaComObrigacaoFinanceira(obrigacao.getId());
			} else {
				Transacao transacao = registrarTransacao.registrar(usuarioId,
						new com.finisus.application.ports.in.TransacaoUseCase.RegistrarCommand(lancamento.getTipo(),
								lancamento.getValor(), lancamento.getData(), lancamento.getDescricao(), importacao.getContaId(),
								lancamento.getCategoriaId(), null, itens(lancamento)));
				lancamento.marcarCriadaComTransacao(transacao.getId());
			}
		}
		return importacoes.salvar(importacao);
	}

	private String credor(ImportacaoFinanceira importacao) {
		return importacao.getIdentificadorOrigem() == null || importacao.getIdentificadorOrigem().isBlank()
				? "Cobrança importada" : limitar(importacao.getIdentificadorOrigem(), 150);
	}

	private List<com.finisus.application.ports.in.TransacaoUseCase.ItemCommand> itens(LancamentoImportado lancamento) {
		return List.of(new com.finisus.application.ports.in.TransacaoUseCase.ItemCommand(lancamento.getItemId(),
				lancamento.getDescricao(), null, lancamento.getValor(), lancamento.getCategoriaId()));
	}

	private Revisao revisao(Long usuarioId, ImportacaoFinanceira importacao) {
		if (importacao.getTipoDocumento() == TipoDocumentoFinanceiro.COBRANCA) {
			return new Revisao(importacao, duplicidadesObrigacoes(usuarioId, importacao), divergencias(importacao));
		}
		List<PossivelDuplicidade> duplicidades = importacao.getLancamentos().stream().filter(LancamentoImportado::isImportar)
				.filter(l -> l.getData() != null && l.getValor() != null && l.getDescricao() != null)
				.flatMap(l -> candidatosTransacao(usuarioId, importacao, l).stream()
						.map(t -> duplicidade(l, t))).toList();
		return new Revisao(importacao, duplicidades, divergencias(importacao));
	}

	private List<Divergencia> divergencias(ImportacaoFinanceira importacao) {
		if (importacao.getTipoDocumentoPretendido() == importacao.getTipoDocumento())
			return List.of();
		return List.of(new Divergencia("TIPO_DOCUMENTO_DIVERGENTE",
				"O tipo detectado no documento difere do tipo informado no início da importação.",
				importacao.getTipoDocumentoPretendido().name(), importacao.getTipoDocumento().name()));
	}

	private void validarDestinoInicial(Long usuarioId, IniciarCommand command, Long bancoId) {
		if (command.tipoPretendido() == null)
			return;
		switch (command.tipoPretendido()) {
		case EXTRATO_CONTA -> {
			if (command.contaId() == null || command.faturaId() != null)
				throw new DomainException("error.importacao.destino.invalido");
			Conta conta = ContaAtivaValidator.exigirAtiva(
					contas.buscarPorIdEUsuario(command.contaId(), usuarioId).orElseThrow(this::notFound));
			if (!bancoId.equals(conta.getBancoId()))
				throw new DomainException("error.importacao.destino.incompativel");
		}
		case COBRANCA -> {
			if (command.contaId() == null || command.faturaId() != null)
				throw new DomainException("error.importacao.destino.invalido");
			ContaAtivaValidator.exigirAtiva(
					contas.buscarPorIdEUsuario(command.contaId(), usuarioId).orElseThrow(this::notFound));
		}
		case FATURA_CARTAO -> {
			if (command.faturaId() == null || command.contaId() != null)
				throw new DomainException("error.importacao.destino.invalido");
			faturas.buscar(usuarioId, command.faturaId());
		}
		}
	}

	private void validarMesmoContexto(ImportacaoFinanceira existente, IniciarCommand command) {
		if (command.tipoPretendido() == null)
			return;
		if (existente.getTipoDocumentoPretendido() != command.tipoPretendido()
				|| !java.util.Objects.equals(existente.getContaId(), command.contaId())
				|| !java.util.Objects.equals(existente.getFaturaId(), command.faturaId()))
			throw new DomainException("error.importacao.contexto.conflitante");
	}

	private List<PossivelDuplicidade> duplicidadesObrigacoes(Long usuarioId, ImportacaoFinanceira importacao) {
		if (importacao.getContaId() == null) {
			return List.of();
		}
		return importacao.getLancamentos().stream().filter(LancamentoImportado::isImportar)
				.filter(l -> l.getData() != null && l.getValor() != null && l.getDescricao() != null)
				.flatMap(l -> obrigacoesRepository.buscarCandidatosImportacao(usuarioId, importacao.getContaId(),
						l.getData().minusDays(3), l.getData().plusDays(3), l.getValor()).stream()
						.map(o -> new PossivelDuplicidade(l.getId(), null, null, o.getId(), o.getDataVencimento(),
								o.getValor().valor(), o.getDescricao(), nivel(l, o.getDataVencimento(), o.getDescricao()),
								evidencias(l, o.getDataVencimento(), o.getDescricao()))))
				.toList();
	}

	private List<Transacao> candidatosTransacao(Long usuarioId, ImportacaoFinanceira importacao,
			LancamentoImportado lancamento) {
		LocalDate inicio = lancamento.getData().minusDays(3);
		LocalDate fim = lancamento.getData().plusDays(3);
		if (importacao.getTipoDocumento() == TipoDocumentoFinanceiro.FATURA_CARTAO) {
			return transacoes.buscarCandidatosImportacaoPorFatura(usuarioId, importacao.getFaturaId(),
					lancamento.getTipo(), lancamento.getValor(), inicio, fim);
		}
		return transacoes.buscarCandidatosImportacaoPorConta(usuarioId, importacao.getContaId(),
				lancamento.getTipo(), lancamento.getValor(), inicio, fim);
	}

	private PossivelDuplicidade duplicidade(LancamentoImportado lancamento, Transacao transacao) {
		return new PossivelDuplicidade(lancamento.getId(), transacao.getId(), transacao.getTransferenciaId(), null,
				transacao.getData(), transacao.getValor().valor(), transacao.getDescricao(),
				nivel(lancamento, transacao.getData(), transacao.getDescricao()),
				evidencias(lancamento, transacao.getData(), transacao.getDescricao()));
	}

	private NivelDuplicidade nivel(LancamentoImportado lancamento, LocalDate data, String descricao) {
		return lancamento.getData().equals(data)
				&& normalizar(lancamento.getDescricao()).equals(normalizar(descricao))
				? NivelDuplicidade.EXATA : NivelDuplicidade.PROVAVEL;
	}

	private List<String> evidencias(LancamentoImportado lancamento, LocalDate data, String descricao) {
		return List.of("MESMO_DESTINO", "MESMO_TIPO", "MESMO_VALOR",
				lancamento.getData().equals(data) ? "DATA_EXATA" : "DATA_PROXIMA",
				normalizar(lancamento.getDescricao()).equals(normalizar(descricao))
						? "DESCRICAO_EXATA" : "DESCRICAO_DIFERENTE");
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
				|| command.nomeArquivo().isBlank())
			throw new DomainException("error.importacao.arquivo.invalido");
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
