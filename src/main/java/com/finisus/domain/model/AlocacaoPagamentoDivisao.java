package com.finisus.domain.model;

import com.finisus.domain.vo.ValorMonetario;

import java.time.LocalDateTime;

public record AlocacaoPagamentoDivisao(Long id, Long transacaoId, Long pagadorId,
        ValorMonetario valor, LocalDateTime criadaEm, LocalDateTime canceladaEm, Long canceladaPor) { }
