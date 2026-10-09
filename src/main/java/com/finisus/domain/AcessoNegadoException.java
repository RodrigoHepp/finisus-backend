package com.finisus.domain;

public class AcessoNegadoException extends DomainException {
	public AcessoNegadoException() {
		super("error.auth.forbidden");
	}
}
