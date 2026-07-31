package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.ConfiguracaoCompartilhamentoJpaEntity;
import com.finisus.adapters.out.persistence.repository.ConfiguracaoCompartilhamentoJpaRepository;
import com.finisus.application.ports.out.ConfiguracaoCompartilhamentoRepositoryPort;
import com.finisus.domain.model.ConfiguracaoCompartilhamento;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
public class ConfiguracaoCompartilhamentoPersistenceAdapter implements ConfiguracaoCompartilhamentoRepositoryPort {
	private final ConfiguracaoCompartilhamentoJpaRepository repository;

	public ConfiguracaoCompartilhamentoPersistenceAdapter(ConfiguracaoCompartilhamentoJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public ConfiguracaoCompartilhamento salvar(ConfiguracaoCompartilhamento configuracao) {
		ConfiguracaoCompartilhamentoJpaEntity entity = new ConfiguracaoCompartilhamentoJpaEntity();
		entity.setId(configuracao.getId());
		entity.setUsuarioId(configuracao.getUsuarioId());
		entity.setAceitaCompartilhamento(configuracao.isAceitaCompartilhamento());
		return toDomain(repository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ConfiguracaoCompartilhamento> buscarPorUsuarioId(Long usuarioId) {
		return repository.findByUsuarioId(usuarioId).map(this::toDomain);
	}

	private ConfiguracaoCompartilhamento toDomain(ConfiguracaoCompartilhamentoJpaEntity entity) {
		return ConfiguracaoCompartilhamento.reconstituir(entity.getId(), entity.getUsuarioId(),
				entity.isAceitaCompartilhamento());
	}
}
