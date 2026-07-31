package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.MovimentoInvestimentoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MovimentoInvestimentoJpaRepository extends JpaRepository<MovimentoInvestimentoJpaEntity, Long> {

	List<MovimentoInvestimentoJpaEntity> findByInvestimentoIdOrderByDataDesc(Long investimentoId);

	Page<MovimentoInvestimentoJpaEntity> findByInvestimentoId(Long investimentoId, Pageable pageable);

	Optional<MovimentoInvestimentoJpaEntity> findByIdAndInvestimentoId(Long id, Long investimentoId);

	boolean existsByMovimentoOrigemId(Long movimentoOrigemId);
}
