package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.InvestimentoUseCase;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.InvestimentoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.Investimento;
import com.finisus.domain.model.TipoConta;

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
		validarCustodia(null, command.contaOrigemId(), command.contaCustodiaId(), usuarioId);
		return investimentos
				.salvar(Investimento.novo(usuarioId, command.nome(), command.tipo(), command.contaOrigemId(),
						command.contaCustodiaId()));
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
		validarCustodia(investimentoId, command.contaOrigemId(), command.contaCustodiaId(), usuarioId);
		return investimentos.salvar(Investimento.reconstituir(investimentoId, usuarioId, command.nome(), command.tipo(),
				command.contaOrigemId(), command.contaCustodiaId(), atual.isAtivo()));
	}

	@Override
	public Investimento inativar(Long usuarioId, Long investimentoId) {
		Investimento investimento = buscar(usuarioId, investimentoId);
		return investimentos.salvar(Investimento.reconstituir(investimento.getId(), usuarioId, investimento.getNome(),
				investimento.getTipo(), investimento.getContaOrigemId(), investimento.getContaCustodiaId(), false));
	}

	@Override
	public List<Investimento> listar(Long usuarioId) {
		return investimentos.listarPorUsuario(usuarioId);
	}

	@Override
	public Pagina<Investimento> listar(Long usuarioId, Paginacao paginacao) {
		return investimentos.listarPorUsuario(usuarioId, paginacao);
	}

	private Conta conta(Long contaId, Long usuarioId) {
		Conta conta = contas.buscarPorIdEUsuario(contaId, usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
		ContaAtivaValidator.exigirAtiva(conta);
		return conta;
	}

	private void validarCustodia(Long investimentoId, Long contaOrigemId, Long contaCustodiaId, Long usuarioId) {
		if (contaCustodiaId == null) return;
		if (contaCustodiaId.equals(contaOrigemId) || conta(contaCustodiaId, usuarioId).getTipo() != TipoConta.APLICACAO
				|| investimentos.existePorContaCustodiaExceto(contaCustodiaId, investimentoId)) {
			throw new DomainException("error.investimento.custodia.invalida");
		}
	}
}
