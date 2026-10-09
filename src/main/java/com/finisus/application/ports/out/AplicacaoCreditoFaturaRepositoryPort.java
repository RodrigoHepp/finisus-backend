package com.finisus.application.ports.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.finisus.domain.model.AplicacaoCreditoFatura;

public interface AplicacaoCreditoFaturaRepositoryPort {
	AplicacaoCreditoFatura salvar(AplicacaoCreditoFatura aplicacao);
	BigDecimal somarPorPagamentoOrigem(Long pagamentoOrigemId);
	BigDecimal somarPorFaturaDestino(Long faturaDestinoId);
	Map<Long, BigDecimal> somarPorFaturasDestino(List<Long> faturasIds);
	boolean existePorPagamentoOrigem(Long pagamentoOrigemId);
}
