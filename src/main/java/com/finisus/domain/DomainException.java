package com.finisus.domain;

public class DomainException extends RuntimeException {

	private static final long serialVersionUID = -5050061862590803094L;

	private final String messageKey;
	private final Object[] args;

	public DomainException(String messageKey, Object... args) {
		super(messageKey);
		this.messageKey = messageKey;
		this.args = args;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public Object[] getArgs() {
		return args;
	}
}
