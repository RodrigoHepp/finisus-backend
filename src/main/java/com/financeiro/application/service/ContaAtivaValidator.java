package com.financeiro.application.service;

import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.Conta;

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
