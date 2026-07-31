package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.RecorrenciaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RecorrenciaJpaRepository extends JpaRepository<RecorrenciaJpaEntity, Long> {

	Optional<RecorrenciaJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);

	List<RecorrenciaJpaEntity> findByUsuarioId(Long usuarioId);

	Page<RecorrenciaJpaEntity> findByUsuarioId(Long usuarioId, Pageable pageable);

	List<RecorrenciaJpaEntity> findByUsuarioIdAndAtivoTrue(Long usuarioId);
}
