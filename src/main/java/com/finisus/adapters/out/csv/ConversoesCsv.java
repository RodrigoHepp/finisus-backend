package com.finisus.adapters.out.csv;

import com.finisus.domain.DomainException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

final class ConversoesCsv {
	private static final DateTimeFormatter DATA_BRASILEIRA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
			.withResolverStyle(java.time.format.ResolverStyle.STRICT);

	private ConversoesCsv() { }

	static LocalDate data(String valor) {
		try {
			return LocalDate.parse(exigir(valor), DATA_BRASILEIRA);
		} catch (DateTimeParseException exception) {
			throw invalido();
		}
	}

	static BigDecimal monetario(String valor) {
		String normalizado = exigir(valor).replace('\u2212', '-').replace("R$", "").replace(" ", "")
				.replace("\u00a0", "");
		boolean formatoBrasileiro = normalizado.indexOf(',') >= 0;
		if (formatoBrasileiro) normalizado = normalizado.replace(".", "").replace(',', '.');
		if (!normalizado.matches("[+-]?\\d+(?:\\.\\d{1,2})?")) throw invalido();
		try {
			return new BigDecimal(normalizado);
		} catch (NumberFormatException exception) {
			throw invalido();
		}
	}

	static String descricao(String valor) {
		String descricao = exigir(valor).replaceAll("\\s+", " ").trim();
		return descricao.substring(0, Math.min(descricao.length(), 500));
	}

	private static String exigir(String valor) {
		if (valor == null || valor.isBlank()) throw invalido();
		return valor.trim();
	}

	static DomainException invalido() {
		return new DomainException("error.importacao.arquivo.invalido");
	}
}
