package com.finisus.domain;

public class CredenciaisInvalidasException extends DomainException {
	public CredenciaisInvalidasException() {
		super("error.auth.invalid");
	}
}
