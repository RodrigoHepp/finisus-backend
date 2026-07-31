package com.financeiro.domain;

public class ConflitoAtualizacaoException extends DomainException {
	public ConflitoAtualizacaoException() {
		super("error.conflito.atualizacao");
	}
}
