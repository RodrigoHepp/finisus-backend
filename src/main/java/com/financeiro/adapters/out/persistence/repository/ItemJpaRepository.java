package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.ItemJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemJpaRepository extends JpaRepository<ItemJpaEntity, Long> {
	Optional<ItemJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);

	Page<ItemJpaEntity> findByUsuarioIdAndAtivoTrue(Long usuarioId, Pageable pageable);
}
