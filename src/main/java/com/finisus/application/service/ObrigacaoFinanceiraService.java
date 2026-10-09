package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.ObrigacaoFinanceiraUseCase;
import com.finisus.application.ports.in.RegistrarTransacaoUseCase;
import com.finisus.application.ports.in.TransacaoUseCase;
import com.finisus.application.ports.in.EstornarTransacaoVinculadaUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.ObrigacaoFinanceiraRepositoryPort;
import com.finisus.application.ports.out.PagamentoObrigacaoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.ObrigacaoFinanceira;
import com.finisus.domain.model.PagamentoObrigacao;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.vo.ValorMonetario;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

public class ObrigacaoFinanceiraService implements ObrigacaoFinanceiraUseCase {
	private final ObrigacaoFinanceiraRepositoryPort obrigacoes;
	private final ContaRepositoryPort contas;
	private final CategoriaRepositoryPort categorias;
	private final RegistrarTransacaoUseCase transacoes;
	private final ObterDataAtualPort dataAtual;
	private final EstornarTransacaoVinculadaUseCase estornos;
	private final PagamentoObrigacaoRepositoryPort pagamentos;

	public ObrigacaoFinanceiraService(ObrigacaoFinanceiraRepositoryPort obrigacoes, ContaRepositoryPort contas,
			CategoriaRepositoryPort categorias, RegistrarTransacaoUseCase transacoes, ObterDataAtualPort dataAtual,
			EstornarTransacaoVinculadaUseCase estornos, PagamentoObrigacaoRepositoryPort pagamentos) {
		this.obrigacoes = obrigacoes;
		this.contas = contas;
		this.categorias = categorias;
		this.transacoes = transacoes;
		this.dataAtual = dataAtual;
		this.estornos = estornos;
		this.pagamentos = pagamentos;
	}

	@Override
	@Transactional
	public ObrigacaoFinanceira criar(Long usuarioId, CriarCommand command) {
		ContaAtivaValidator.exigirAtiva(contas.buscarPorIdEUsuario(command.contaPagamentoId(), usuarioId)
				.orElseThrow(this::naoEncontrado));
		if (command.categoriaId() != null) CategoriaAtivaValidator.exigirAtiva(
				categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId).orElseThrow(this::naoEncontrado));
		return obrigacoes.salvar(ObrigacaoFinanceira.nova(usuarioId, command.descricao(), command.credor(),
				ValorMonetario.of(command.valor()), command.dataVencimento(), command.contaPagamentoId(),
				command.categoriaId()));
	}

	@Override
	@Transactional(readOnly = true)
	public ObrigacaoFinanceira buscar(Long usuarioId, Long obrigacaoId) {
		return obrigacoes.buscarPorIdEUsuario(obrigacaoId, usuarioId).orElseThrow(this::naoEncontrado);
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<ObrigacaoFinanceira> listar(Long usuarioId, FiltroListagem filtro, Paginacao paginacao) {
		return obrigacoes.listarPorUsuario(usuarioId, filtro, paginacao);
	}

	@Override
	@Transactional
	public ObrigacaoFinanceira pagar(Long usuarioId, Long obrigacaoId, PagamentoCommand command) {
		ObrigacaoFinanceira obrigacao = obrigacoes.buscarPorIdEUsuarioParaAtualizacao(obrigacaoId, usuarioId)
				.orElseThrow(this::naoEncontrado);
		ValorMonetario juros = monetarioOuZero(command.juros());
		ValorMonetario encargos = monetarioOuZero(command.encargos());
		ValorMonetario desconto = monetarioOuZero(command.desconto());
		if (desconto.valor().compareTo(obrigacao.getSaldoPendente().valor()) > 0) {
			throw new DomainException("error.obrigacao.pagamento.invalido");
		}
		ValorMonetario valorPagamento = command.valor() == null
				? obrigacao.getSaldoPendente().subtrair(desconto) : ValorMonetario.of(command.valor());
		ValorMonetario valorAbatido = valorPagamento.somar(desconto);
		ValorMonetario valorCaixa = valorPagamento.somar(juros).somar(encargos);
		if (valorAbatido.isZero() || valorCaixa.isZero()
				|| valorAbatido.valor().compareTo(obrigacao.getSaldoPendente().valor()) > 0) {
			throw new DomainException("error.obrigacao.pagamento.invalido");
		}
		var transacao = transacoes.registrar(usuarioId, new TransacaoUseCase.RegistrarCommand(TipoTransacao.SAIDA,
				valorCaixa.valor(), command.dataPagamento(), "Pagamento - " + obrigacao.getCredor() + " - "
						+ obrigacao.getDescricao(), obrigacao.getContaPagamentoId(), obrigacao.getCategoriaId(), null,
				java.util.List.of()));
		pagamentos.salvar(PagamentoObrigacao.novo(obrigacaoId, usuarioId, transacao.getId(), valorPagamento, juros,
				encargos, desconto, command.dataPagamento()));
		return obrigacoes.salvar(obrigacao.registrarPagamento(valorAbatido, command.dataPagamento(), transacao.getId()));
	}

	public ObrigacaoFinanceira pagar(Long usuarioId, Long obrigacaoId, LocalDate dataPagamento,
			java.math.BigDecimal valor) {
		return pagar(usuarioId, obrigacaoId, new PagamentoCommand(dataPagamento, valor, null, null, null));
	}

	@Override
	@Transactional(readOnly = true)
	public java.util.List<PagamentoObrigacao> listarPagamentos(Long usuarioId, Long obrigacaoId) {
		buscar(usuarioId, obrigacaoId);
		return pagamentos.listarPorObrigacaoEUsuario(obrigacaoId, usuarioId);
	}

	@Override
	@Transactional
	public ObrigacaoFinanceira estornarPagamento(Long usuarioId, Long obrigacaoId, Long pagamentoId) {
		ObrigacaoFinanceira obrigacao = obrigacoes.buscarPorIdEUsuarioParaAtualizacao(obrigacaoId, usuarioId)
				.orElseThrow(this::naoEncontrado);
		PagamentoObrigacao pagamento = pagamentos.buscarPorIdObrigacaoEUsuario(pagamentoId, obrigacaoId, usuarioId)
				.filter(PagamentoObrigacao::ativo).orElseThrow(this::naoEncontrado);
		estornos.estornarVinculada(usuarioId, pagamento.transacaoId());
		pagamentos.salvar(pagamento.estornar(dataAtual.obterDataHora()));
		return obrigacoes.salvar(obrigacao.estornarPagamento(pagamento.valorAbatido(), dataAtual.obter()));
	}

	@Override
	@Transactional
	public ObrigacaoFinanceira cancelar(Long usuarioId, Long obrigacaoId) {
		ObrigacaoFinanceira obrigacao = obrigacoes.buscarPorIdEUsuarioParaAtualizacao(obrigacaoId, usuarioId)
				.orElseThrow(this::naoEncontrado);
		return obrigacoes.salvar(obrigacao.cancelar(dataAtual.obterDataHora()));
	}

	@Override
	@Transactional
	public ObrigacaoFinanceira estornarPagamento(Long usuarioId, Long obrigacaoId) {
		ObrigacaoFinanceira obrigacao = obrigacoes.buscarPorIdEUsuarioParaAtualizacao(obrigacaoId, usuarioId)
				.orElseThrow(this::naoEncontrado);
		var ativos = pagamentos.listarPorObrigacaoEUsuario(obrigacaoId, usuarioId).stream()
				.filter(PagamentoObrigacao::ativo).toList();
		if (ativos.isEmpty()) throw new DomainException("error.obrigacao.estorno.pagamento.invalido");
		ObrigacaoFinanceira reaberta = obrigacao;
		for (PagamentoObrigacao pagamento : ativos) {
			estornos.estornarVinculada(usuarioId, pagamento.transacaoId());
			pagamentos.salvar(pagamento.estornar(dataAtual.obterDataHora()));
			reaberta = reaberta.estornarPagamento(pagamento.valorAbatido(), dataAtual.obter());
		}
		return obrigacoes.salvar(reaberta);
	}

	@Override
	@Transactional
	public int processarVencimentos(LocalDate dataReferencia) {
		return obrigacoes.listarEmAbertoVencidasAte(dataReferencia).stream().map(obrigacao ->
				obrigacoes.salvar(obrigacao.marcarVencida(dataReferencia))).toList().size();
	}

	private DomainException naoEncontrado() {
		return new DomainException("error.recurso.nao.encontrado");
	}

	private ValorMonetario monetarioOuZero(java.math.BigDecimal valor) {
		return valor == null ? ValorMonetario.zero() : ValorMonetario.of(valor);
	}
}
