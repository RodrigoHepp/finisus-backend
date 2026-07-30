package com.financeiro.domain;

public class FinanciamentoInvalidoException extends DomainException {
    public FinanciamentoInvalidoException() {
        super("error.financiamento.invalido");
    }
}
