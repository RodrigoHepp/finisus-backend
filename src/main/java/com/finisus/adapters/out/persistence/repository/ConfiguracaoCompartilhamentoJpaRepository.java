package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.ConfiguracaoCompartilhamentoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoCompartilhamentoJpaRepository
		extends JpaRepository<ConfiguracaoCompartilhamentoJpaEntity, Long> {

	Optional<ConfiguracaoCompartilhamentoJpaEntity> findByUsuarioId(Long usuarioId);
}
