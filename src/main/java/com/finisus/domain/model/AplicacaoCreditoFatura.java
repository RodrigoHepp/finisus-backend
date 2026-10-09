package com.finisus.domain.model;

import java.time.LocalDateTime;

import com.finisus.domain.DomainException;
import com.finisus.domain.vo.ValorMonetario;

public record AplicacaoCreditoFatura(Long id, Long pagamentoOrigemId, Long faturaDestinoId,
		ValorMonetario valor, LocalDateTime criadaEm) {
	public AplicacaoCreditoFatura {
		if (pagamentoOrigemId == null || faturaDestinoId == null || valor == null || valor.isZero() || criadaEm == null)
			throw new DomainException("error.fatura.credito.aplicacao.invalida");
	}
	public static AplicacaoCreditoFatura nova(Long pagamentoOrigemId, Long faturaDestinoId,
			ValorMonetario valor, LocalDateTime criadaEm) {
		return new AplicacaoCreditoFatura(null, pagamentoOrigemId, faturaDestinoId, valor, criadaEm);
	}
}
