package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.MeioPagamentoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MeioPagamentoJpaRepository extends JpaRepository<MeioPagamentoJpaEntity, Long> {

    List<MeioPagamentoJpaEntity> findByUsuarioIdAndAtivoTrue(Long usuarioId);
    Page<MeioPagamentoJpaEntity> findByUsuarioIdAndAtivoTrue(Long usuarioId, Pageable pageable);

    Optional<MeioPagamentoJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);
}
