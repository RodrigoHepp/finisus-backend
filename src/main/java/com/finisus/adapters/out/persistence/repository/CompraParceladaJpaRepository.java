package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.CompraParceladaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CompraParceladaJpaRepository extends JpaRepository<CompraParceladaJpaEntity, Long> {

	Optional<CompraParceladaJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);

	List<CompraParceladaJpaEntity> findByUsuarioId(Long usuarioId);

	Page<CompraParceladaJpaEntity> findByUsuarioId(Long usuarioId, Pageable pageable);
}
