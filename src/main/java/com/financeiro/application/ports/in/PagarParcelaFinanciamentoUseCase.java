package com.financeiro.application.ports.in;

import com.financeiro.domain.model.ParcelaFinanciamento;

import java.time.LocalDate;

public interface PagarParcelaFinanciamentoUseCase {
	ParcelaFinanciamento pagarParcela(Long usuarioId, Long financiamentoId, Long parcelaId, LocalDate dataPagamento);
}
