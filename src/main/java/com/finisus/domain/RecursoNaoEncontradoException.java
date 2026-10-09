package com.finisus.domain;

public class RecursoNaoEncontradoException extends DomainException {
	public RecursoNaoEncontradoException() {
		super("error.recurso.nao.encontrado");
	}
}
