package com.finisus.adapters.out.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.finisus.adapters.out.persistence.entity.AjusteSaldoContaJpaEntity;

public interface AjusteSaldoContaJpaRepository extends JpaRepository<AjusteSaldoContaJpaEntity, Long> {
	Optional<AjusteSaldoContaJpaEntity> findByUsuarioIdAndChaveIdempotencia(Long usuarioId, String chaveIdempotencia);
	Page<AjusteSaldoContaJpaEntity> findByContaIdAndUsuarioId(Long contaId, Long usuarioId, Pageable pageable);
}
