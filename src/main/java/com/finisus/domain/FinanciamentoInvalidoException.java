package com.finisus.domain;

public class FinanciamentoInvalidoException extends DomainException {

	private static final long serialVersionUID = 2119467276920609L;

	public FinanciamentoInvalidoException() {
		super("error.financiamento.invalido");
	}
}
