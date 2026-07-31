package com.finisus.application.ports.out;

import com.finisus.domain.model.ConfiguracaoCompartilhamento;

import java.util.Optional;

public interface ConfiguracaoCompartilhamentoRepositoryPort {
	ConfiguracaoCompartilhamento salvar(ConfiguracaoCompartilhamento configuracao);

	Optional<ConfiguracaoCompartilhamento> buscarPorUsuarioId(Long usuarioId);
}
