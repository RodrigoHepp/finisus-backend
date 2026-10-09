package com.finisus.adapters.out.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finisus.adapters.out.persistence.entity.PagamentoFaturaJpaEntity;

public interface PagamentoFaturaJpaRepository extends JpaRepository<PagamentoFaturaJpaEntity, Long> {
	Optional<PagamentoFaturaJpaEntity> findByUsuarioIdAndChaveIdempotencia(Long usuarioId, String chaveIdempotencia);
	List<PagamentoFaturaJpaEntity> findByFaturaIdAndUsuarioIdOrderByDataPagamentoAscIdAsc(Long faturaId, Long usuarioId);
	List<PagamentoFaturaJpaEntity> findByFaturaIdInAndUsuarioId(List<Long> faturasIds, Long usuarioId);
}
