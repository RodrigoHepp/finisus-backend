package com.finisus.application.ports.in;

import com.finisus.domain.model.ParcelaFinanciamento;

import java.time.LocalDate;

public interface PagarParcelaFinanciamentoUseCase {
	ParcelaFinanciamento pagarParcela(Long usuarioId, Long financiamentoId, Long parcelaId, LocalDate dataPagamento);
	ParcelaFinanciamento estornarPagamento(Long usuarioId, Long financiamentoId, Long parcelaId);
}
