package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.MovimentoInvestimentoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovimentoInvestimentoJpaRepository extends JpaRepository<MovimentoInvestimentoJpaEntity, Long> {

    List<MovimentoInvestimentoJpaEntity> findByInvestimentoIdOrderByDataDesc(Long investimentoId);
    Page<MovimentoInvestimentoJpaEntity> findByInvestimentoId(Long investimentoId, Pageable pageable);
}
