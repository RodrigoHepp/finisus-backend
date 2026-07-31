package com.finisus.application.ports.in;

import com.finisus.domain.model.ConfiguracaoCompartilhamento;

public interface ConfiguracaoCompartilhamentoUseCase {
	ConfiguracaoCompartilhamento atualizarOptIn(Long usuarioId, boolean aceita);

	ConfiguracaoCompartilhamento consultarOptIn(Long usuarioId);
}
