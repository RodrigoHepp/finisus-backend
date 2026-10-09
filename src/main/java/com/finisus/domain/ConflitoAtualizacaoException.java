package com.finisus.domain;

public class ConflitoAtualizacaoException extends DomainException {
	private static final long serialVersionUID = 1L;

	public ConflitoAtualizacaoException() {
		super("error.conflito.atualizacao");
	}
}
