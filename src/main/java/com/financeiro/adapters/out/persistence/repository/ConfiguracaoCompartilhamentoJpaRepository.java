package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.ConfiguracaoCompartilhamentoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoCompartilhamentoJpaRepository extends JpaRepository<ConfiguracaoCompartilhamentoJpaEntity, Long> {

    Optional<ConfiguracaoCompartilhamentoJpaEntity> findByUsuarioId(Long usuarioId);
}
