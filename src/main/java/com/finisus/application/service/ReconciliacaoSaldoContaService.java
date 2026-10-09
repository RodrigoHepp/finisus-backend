package com.finisus.application.service;

import org.springframework.transaction.annotation.Transactional;

import com.finisus.application.ports.in.ReconciliarSaldoContaUseCase;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Conta;

public class ReconciliacaoSaldoContaService implements ReconciliarSaldoContaUseCase {
	private final ContaRepositoryPort contas;

	public ReconciliacaoSaldoContaService(ContaRepositoryPort contas) {
		this.contas = contas;
	}

	@Override
	@Transactional(readOnly = true)
	public ReconciliacaoSaldo reconciliar(Long usuarioId, Long contaId) {
		Conta conta = contas.buscarPorIdEUsuario(contaId, usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
		var movimentos = contas.calcularMovimentosEficazes(contaId, usuarioId);
		var saldoMaterializado = conta.getSaldo().valor();
		var divergencia = saldoMaterializado.subtract(movimentos.saldoCalculado());
		return new ReconciliacaoSaldo(contaId, saldoMaterializado, movimentos.saldoCalculado(), divergencia,
				movimentos.quantidade(), movimentos.quantidadeAjustes(), divergencia.signum() == 0);
	}
}
