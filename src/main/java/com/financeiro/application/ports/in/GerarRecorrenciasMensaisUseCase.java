package com.financeiro.application.ports.in;

import com.financeiro.domain.model.Transacao;

import java.util.List;

public interface GerarRecorrenciasMensaisUseCase {
	List<Transacao> gerarMes(Long usuarioId, String anoMes);
}
