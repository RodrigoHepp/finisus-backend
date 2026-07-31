package com.financeiro.domain.vo;

import com.financeiro.domain.DomainException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public record AnoMes(int ano, int mes) {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

	public AnoMes {
		if (mes < 1 || mes > 12) {
			throw new DomainException("error.anomes.invalid");
		}
	}

	public static AnoMes of(int ano, int mes) {
		return new AnoMes(ano, mes);
	}

	public static AnoMes from(LocalDate data) {
		return new AnoMes(data.getYear(), data.getMonthValue());
	}

	public static AnoMes parse(String valor) {
		YearMonth ym = YearMonth.parse(valor, FORMATTER);
		return new AnoMes(ym.getYear(), ym.getMonthValue());
	}

	public AnoMes proximo() {
		YearMonth ym = YearMonth.of(ano, mes).plusMonths(1);
		return new AnoMes(ym.getYear(), ym.getMonthValue());
	}

	public LocalDate primeiroDia() {
		return LocalDate.of(ano, mes, 1);
	}

	public String formatado() {
		return YearMonth.of(ano, mes).format(FORMATTER);
	}
}
