package com.finisus.domain.vo;

import com.finisus.domain.DomainException;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String valor) {

	private static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

	public Email {
		if (valor == null || valor.isBlank()) {
			throw new DomainException("error.email.required");
		}
		String normalizado = valor.trim().toLowerCase(Locale.ROOT);
		if (!PATTERN.matcher(normalizado).matches()) {
			throw new DomainException("error.email.invalid");
		}
		valor = normalizado;
	}
}
