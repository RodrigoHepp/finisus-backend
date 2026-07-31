package com.finisus.application.service;

import com.finisus.domain.DomainException;
import com.finisus.domain.model.Conta;

public final class ContaAtivaValidator {

	private ContaAtivaValidator() {
	}

	public static Conta exigirAtiva(Conta conta) {
		if (!conta.isAtivo()) {
			throw new DomainException("error.conta.inativa");
		}
		return conta;
	}
}
