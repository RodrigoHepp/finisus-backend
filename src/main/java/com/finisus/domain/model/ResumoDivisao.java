package com.finisus.domain.model;

import com.finisus.domain.vo.ValorMonetario;

import java.util.List;

public record ResumoDivisao(Long divisaoId, String divisao, ValorMonetario total,
		List<ResumoParticipanteDivisao> participantes, long lancamentosPendentesRevisao) {
	public ResumoDivisao(Long divisaoId, String divisao, ValorMonetario total,
			List<ResumoParticipanteDivisao> participantes) {
		this(divisaoId, divisao, total, participantes, 0);
	}
}
