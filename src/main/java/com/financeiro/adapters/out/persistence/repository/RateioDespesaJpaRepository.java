package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.RateioDespesaJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RateioDespesaJpaRepository extends JpaRepository<RateioDespesaJpaEntity, Long> {
	List<RateioDespesaJpaEntity> findByDespesaCompartilhadaId(Long despesaCompartilhadaId);

	Page<RateioDespesaJpaEntity> findByDespesaCompartilhadaId(Long despesaCompartilhadaId, Pageable pageable);

	Page<RateioDespesaJpaEntity> findByUsuarioIdAndTipoParticipante(Long usuarioId,
			com.financeiro.domain.model.TipoParticipante tipoParticipante, Pageable pageable);

}
