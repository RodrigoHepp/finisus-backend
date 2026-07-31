package com.financeiro.application.ports.out;

import com.financeiro.domain.model.ConfiguracaoCompartilhamento;

import java.util.Optional;

public interface ConfiguracaoCompartilhamentoRepositoryPort {
	ConfiguracaoCompartilhamento salvar(ConfiguracaoCompartilhamento configuracao);

	Optional<ConfiguracaoCompartilhamento> buscarPorUsuarioId(Long usuarioId);
}
