package com.financeiro.application.service;

import com.financeiro.application.ports.in.ConfiguracaoCompartilhamentoUseCase;
import com.financeiro.application.ports.out.ConfiguracaoCompartilhamentoRepositoryPort;
import com.financeiro.domain.model.ConfiguracaoCompartilhamento;
import org.springframework.transaction.annotation.Transactional;

public class ConfiguracaoCompartilhamentoService implements ConfiguracaoCompartilhamentoUseCase {
	private final ConfiguracaoCompartilhamentoRepositoryPort configuracoes;

	public ConfiguracaoCompartilhamentoService(ConfiguracaoCompartilhamentoRepositoryPort configuracoes) {
		this.configuracoes = configuracoes;
	}

	@Override
	@Transactional
	public ConfiguracaoCompartilhamento atualizarOptIn(Long usuarioId, boolean aceita) {
		ConfiguracaoCompartilhamento configuracao = consultarOptIn(usuarioId);
		configuracao.atualizarOptIn(aceita);
		return configuracoes.salvar(configuracao);
	}

	@Override
	@Transactional(readOnly = true)
	public ConfiguracaoCompartilhamento consultarOptIn(Long usuarioId) {
		return configuracoes.buscarPorUsuarioId(usuarioId)
				.orElseGet(() -> ConfiguracaoCompartilhamento.padrao(usuarioId));
	}
}
