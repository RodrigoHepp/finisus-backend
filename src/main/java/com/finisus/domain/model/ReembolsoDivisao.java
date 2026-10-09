package com.finisus.domain.model;

import com.finisus.domain.vo.ValorMonetario;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReembolsoDivisao(Long id, Long transacaoId, Long pagadorId, Long recebedorId,
        ValorMonetario valor, LocalDate data, LocalDateTime criadoEm, LocalDateTime canceladoEm,
        Long canceladoPor) { }
