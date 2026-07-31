package com.financeiro.domain;

public class RateioInvalidoException extends DomainException {
	public RateioInvalidoException() {
		super("error.rateio.invalido");
	}
}
