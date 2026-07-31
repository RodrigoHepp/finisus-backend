package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.InvestimentoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InvestimentoJpaRepository extends JpaRepository<InvestimentoJpaEntity, Long> {

	Optional<InvestimentoJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);

	List<InvestimentoJpaEntity> findByUsuarioId(Long usuarioId);

	Page<InvestimentoJpaEntity> findByUsuarioId(Long usuarioId, Pageable pageable);
}
