package com.finisus.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricoParticipanteDivisao(Long usuarioId, BigDecimal percentual,
        LocalDateTime vigenteDesde, LocalDateTime vigenteAte) { }
