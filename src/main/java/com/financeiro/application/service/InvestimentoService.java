package com.financeiro.application.service;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.InvestimentoUseCase;
import com.financeiro.application.ports.out.ContaRepositoryPort;
import com.financeiro.application.ports.out.InvestimentoRepositoryPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.Investimento;

import java.util.List;

public class InvestimentoService implements InvestimentoUseCase {
	private final InvestimentoRepositoryPort investimentos;
	private final ContaRepositoryPort contas;

	public InvestimentoService(InvestimentoRepositoryPort investimentos, ContaRepositoryPort contas) {
		this.investimentos = investimentos;
		this.contas = contas;
	}

	@Override
	public Investimento criar(Long usuarioId, CriarCommand command) {
		conta(command.contaOrigemId(), usuarioId);
		return investimentos
				.salvar(Investimento.novo(usuarioId, command.nome(), command.tipo(), command.contaOrigemId()));
	}

	@Override
	public Investimento buscar(Long usuarioId, Long investimentoId) {
		return investimentos.buscarPorIdEUsuario(investimentoId, usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}

	@Override
	public Investimento atualizar(Long usuarioId, Long investimentoId, CriarCommand command) {
		Investimento atual = buscar(usuarioId, investimentoId);
		conta(command.contaOrigemId(), usuarioId);
		return investimentos.salvar(Investimento.reconstituir(investimentoId, usuarioId, command.nome(), command.tipo(),
				command.contaOrigemId(), atual.isAtivo()));
	}

	@Override
	public Investimento inativar(Long usuarioId, Long investimentoId) {
		Investimento investimento = buscar(usuarioId, investimentoId);
		return investimentos.salvar(Investimento.reconstituir(investimento.getId(), usuarioId, investimento.getNome(),
				investimento.getTipo(), investimento.getContaOrigemId(), false));
	}

	@Override
	public List<Investimento> listar(Long usuarioId) {
		return investimentos.listarPorUsuario(usuarioId);
	}

	@Override
	public Pagina<Investimento> listar(Long usuarioId, Paginacao paginacao) {
		return investimentos.listarPorUsuario(usuarioId, paginacao);
	}

	private void conta(Long contaId, Long usuarioId) {
		ContaAtivaValidator.exigirAtiva(contas.buscarPorIdEUsuario(contaId, usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado")));
	}
}
