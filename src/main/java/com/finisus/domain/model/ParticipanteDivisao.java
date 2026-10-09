package com.finisus.domain.model;

import com.finisus.domain.DomainException;

import java.math.BigDecimal;

public record ParticipanteDivisao(Long usuarioId, BigDecimal percentual) {
    public ParticipanteDivisao {
        if (usuarioId == null || usuarioId <= 0 || percentual != null && (percentual.signum() <= 0
                || percentual.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new DomainException("error.divisao.participante.invalido");
        }
        percentual = percentual == null ? null : percentual.stripTrailingZeros();
    }
}
