package com.finisus.domain;

public class UsuarioBloqueadoException extends DomainException {
	public UsuarioBloqueadoException() {
		super("error.usuario.bloqueado");
	}
}
