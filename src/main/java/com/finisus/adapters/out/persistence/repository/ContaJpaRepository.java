package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.ContaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ContaJpaRepository extends JpaRepository<ContaJpaEntity, Long> {

	List<ContaJpaEntity> findByUsuarioId(Long usuarioId);

	Page<ContaJpaEntity> findByUsuarioId(Long usuarioId, Pageable pageable);

	Optional<ContaJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);

	boolean existsByIdAndUsuarioId(Long id, Long usuarioId);
}
