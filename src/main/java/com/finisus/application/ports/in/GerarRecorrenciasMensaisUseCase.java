package com.finisus.application.ports.in;

import com.finisus.domain.model.Transacao;

import java.util.List;

public interface GerarRecorrenciasMensaisUseCase {
	List<Transacao> gerarMes(Long usuarioId, String anoMes);
}
