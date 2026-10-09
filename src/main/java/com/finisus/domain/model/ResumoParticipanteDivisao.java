package com.finisus.domain.model;

import com.finisus.domain.vo.ValorMonetario;

import java.math.BigDecimal;

public record ResumoParticipanteDivisao(Long usuarioId, BigDecimal percentual, ValorMonetario pago,
        ValorMonetario devido, BigDecimal saldo) {
}
