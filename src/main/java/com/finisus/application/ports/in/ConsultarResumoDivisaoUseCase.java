package com.finisus.application.ports.in;

import com.finisus.domain.model.ResumoDivisao;

import java.time.LocalDate;

public interface ConsultarResumoDivisaoUseCase {
    ResumoDivisao consultar(Long usuarioId, Long divisaoId, LocalDate inicio, LocalDate fim);
}
