package com.finisus.domain;

public class ConflitoAtualizacaoException extends DomainException {
	public ConflitoAtualizacaoException() {
		super("error.conflito.atualizacao");
	}
}
