package com.finisus.domain;

public class FinanciamentoInvalidoException extends DomainException {
	public FinanciamentoInvalidoException() {
		super("error.financiamento.invalido");
	}
}
