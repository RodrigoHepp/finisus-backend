package com.finisus.domain.model;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.finisus.domain.DomainException;

public record AjusteSaldoConta(Long id, Long usuarioId, Long contaId, BigDecimal saldoAnterior,
		BigDecimal saldoCalculadoAnterior, BigDecimal saldoInformado, BigDecimal valorAjuste,
		String motivo, LocalDate dataAjuste, String chaveIdempotencia, String hashRequisicao, long version) {

	public static AjusteSaldoConta novo(Long usuarioId, Long contaId, BigDecimal saldoAnterior,
			BigDecimal saldoCalculadoAnterior, BigDecimal saldoInformado, String motivo,
			LocalDate dataAjuste, String chaveIdempotencia, String hashRequisicao) {
		if (usuarioId == null || contaId == null || saldoAnterior == null || saldoCalculadoAnterior == null
				|| saldoInformado == null || motivo == null || motivo.isBlank() || dataAjuste == null
				|| chaveIdempotencia == null || chaveIdempotencia.isBlank() || hashRequisicao == null)
			throw new DomainException("error.ajuste.saldo.invalido");
		BigDecimal anterior = saldoAnterior.setScale(2, RoundingMode.HALF_UP);
		BigDecimal calculado = saldoCalculadoAnterior.setScale(2, RoundingMode.HALF_UP);
		BigDecimal informado = saldoInformado.setScale(2, RoundingMode.HALF_UP);
		if (informado.signum() < 0) throw new DomainException("error.ajuste.saldo.invalido");
		BigDecimal valorAjuste = informado.subtract(calculado);
		return new AjusteSaldoConta(null, usuarioId, contaId, anterior, calculado, informado,
				valorAjuste, motivo.trim(), dataAjuste, chaveIdempotencia, hashRequisicao, 0);
	}
}
