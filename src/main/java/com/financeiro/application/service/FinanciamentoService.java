package com.financeiro.application.service;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.FinanciamentoUseCase;
import com.financeiro.application.ports.in.GerenciarParcelasFinanciamentoUseCase;
import com.financeiro.application.ports.out.ContaRepositoryPort;
import com.financeiro.application.ports.out.FinanciamentoRepositoryPort;
import com.financeiro.application.ports.out.ObterDataAtualPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.Financiamento;
import com.financeiro.domain.vo.ValorMonetario;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import com.financeiro.application.ports.out.ParcelaFinanciamentoRepositoryPort;

public class FinanciamentoService implements FinanciamentoUseCase {
	private final FinanciamentoRepositoryPort financiamentos;
	private final ContaRepositoryPort contas;
	private final GerenciarParcelasFinanciamentoUseCase parcelas;
	private final ObterDataAtualPort dataAtual;
	private final ParcelaFinanciamentoRepositoryPort parcelasPersistidas;

	public FinanciamentoService(FinanciamentoRepositoryPort financiamentos, ContaRepositoryPort contas,
			GerenciarParcelasFinanciamentoUseCase parcelas, ParcelaFinanciamentoRepositoryPort parcelasPersistidas,
			ObterDataAtualPort dataAtual) {
		this.financiamentos = financiamentos;
		this.contas = contas;
		this.parcelas = parcelas;
		this.parcelasPersistidas = parcelasPersistidas;
		this.dataAtual = dataAtual;
	}

	@Override
	@Transactional
	public Financiamento criar(Long usuarioId, CriarCommand command) {
		if (command.numeroParcelas() < 1 || command.taxaJurosMensal().signum() < 0) {
			throw new DomainException("error.financiamento.invalido");
		}
		ContaAtivaValidator.exigirAtiva(contas.buscarPorIdEUsuario(command.contaId(), usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado")));
		Financiamento financiamento = financiamentos
				.salvar(Financiamento.novo(usuarioId, command.descricao(), ValorMonetario.of(command.principal()),
						command.taxaJurosMensal(), command.numeroParcelas(), command.dataInicio(), command.contaId()));
		parcelas.gerar(financiamento);
		return financiamento;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Financiamento> listar(Long usuarioId) {
		return financiamentos.listarPorUsuario(usuarioId);
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<Financiamento> listar(Long usuarioId, Paginacao paginacao) {
		return financiamentos.listarPorUsuario(usuarioId, paginacao);
	}

	@Override
	@Transactional(readOnly = true)
	public Financiamento buscar(Long usuarioId, Long financiamentoId) {
		return financiamentos.buscarPorIdEUsuario(financiamentoId, usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}

	@Override
	@Transactional
	public RefinanciamentoResult excluirPorRefinanciamento(Long usuarioId, Long financiamentoId, Long parcelaId) {
		Financiamento financiamento = buscar(usuarioId, financiamentoId);
		if (financiamento.isFinalizado()) {
			throw new DomainException("error.financiamento.finalizado");
		}
		int excluidas = parcelas.excluirPendentesAPartirDe(financiamento, parcelaId);
		Financiamento finalizado = financiamentos.salvar(financiamento.finalizar(dataAtual.obterDataHora()));
		return new RefinanciamentoResult(finalizado, excluidas);
	}

	@Override
	@Transactional
	public int excluirPorErroDeLancamento(Long usuarioId, Long financiamentoId, Long parcelaId) {
		Financiamento financiamento = buscar(usuarioId, financiamentoId);
		if (financiamento.isFinalizado()) {
			throw new DomainException("error.financiamento.finalizado");
		}
		int numeroParcelas = parcelas.excluirERecalcular(financiamento, parcelaId).size();
		financiamentos.salvar(financiamento.comNumeroParcelas(numeroParcelas));
		return numeroParcelas;
	}

	@Override
	@Transactional
	public Financiamento cancelar(Long usuarioId, Long financiamentoId) {
		Financiamento financiamento = buscar(usuarioId, financiamentoId);
		if (parcelasPersistidas.existePagaPorFinanciamento(financiamentoId)) {
			throw new DomainException("error.financiamento.cancelamento.invalido");
		}
		return financiamentos.salvar(financiamento.cancelar(dataAtual.obterDataHora()));
	}
}
