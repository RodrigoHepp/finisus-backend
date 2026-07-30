package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.TransacaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TransacaoJpaRepository extends JpaRepository<TransacaoJpaEntity, Long> {

    Optional<TransacaoJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<TransacaoJpaEntity> findByUsuarioId(Long usuarioId);
    Page<TransacaoJpaEntity> findByUsuarioId(Long usuarioId, Pageable pageable);

    List<TransacaoJpaEntity> findByContaId(Long contaId);

    List<TransacaoJpaEntity> findByFaturaId(Long faturaId);

    List<TransacaoJpaEntity> findByCompraParceladaId(Long compraParceladaId);
}
