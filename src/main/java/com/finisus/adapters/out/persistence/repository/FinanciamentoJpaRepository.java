package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.FinanciamentoJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinanciamentoJpaRepository extends JpaRepository<FinanciamentoJpaEntity, Long> {
	Optional<FinanciamentoJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);

	List<FinanciamentoJpaEntity> findByUsuarioId(Long usuarioId);

	Page<FinanciamentoJpaEntity> findByUsuarioId(Long usuarioId, Pageable pageable);
}
