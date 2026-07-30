package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.CategoriaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaJpaEntity, Long> {

    List<CategoriaJpaEntity> findByUsuarioIdAndAtivoTrue(Long usuarioId);
    Page<CategoriaJpaEntity> findByUsuarioIdAndAtivoTrue(Long usuarioId, Pageable pageable);

    Optional<CategoriaJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);
}
