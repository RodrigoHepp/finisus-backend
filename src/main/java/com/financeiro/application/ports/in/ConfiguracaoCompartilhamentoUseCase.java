package com.financeiro.application.ports.in;

import com.financeiro.domain.model.ConfiguracaoCompartilhamento;

public interface ConfiguracaoCompartilhamentoUseCase {
	ConfiguracaoCompartilhamento atualizarOptIn(Long usuarioId, boolean aceita);

	ConfiguracaoCompartilhamento consultarOptIn(Long usuarioId);
}
