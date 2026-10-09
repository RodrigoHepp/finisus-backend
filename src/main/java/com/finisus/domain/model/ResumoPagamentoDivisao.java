package com.finisus.domain.model;

import com.finisus.domain.vo.ValorMonetario;

import java.util.List;

public record ResumoPagamentoDivisao(Long divisaoId, Long transacaoId, ValorMonetario baseCompartilhada,
        ValorMonetario pago, ValorMonetario pendente, StatusPagamentoDivisao status,
        List<AlocacaoPagamentoDivisao> alocacoes) {
}
