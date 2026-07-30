package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.CartaoCreditoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CartaoCreditoJpaRepository extends JpaRepository<CartaoCreditoJpaEntity, Long> {

    Optional<CartaoCreditoJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<CartaoCreditoJpaEntity> findByUsuarioId(Long usuarioId);
    Page<CartaoCreditoJpaEntity> findByUsuarioId(Long usuarioId, Pageable pageable);
}
