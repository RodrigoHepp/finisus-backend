package com.finisus.domain.model;

import java.time.LocalDateTime;

public record SolicitacaoPrivacidade(Long id, Long usuarioId, Tipo tipo, Status status, String motivo,
		LocalDateTime solicitadaEm, LocalDateTime concluidaEm, String observacao, long version) {
	public enum Tipo { ANONIMIZACAO }
	public enum Status { SOLICITADA, EM_ANALISE, CONCLUIDA, RECUSADA }
}
