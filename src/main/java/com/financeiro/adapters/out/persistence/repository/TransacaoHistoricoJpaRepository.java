package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.TransacaoHistoricoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransacaoHistoricoJpaRepository extends JpaRepository<TransacaoHistoricoJpaEntity, Long> {

	List<TransacaoHistoricoJpaEntity> findByTransacaoIdOrderByAlteradoEmDesc(Long transacaoId);

	Page<TransacaoHistoricoJpaEntity> findByTransacaoId(Long transacaoId, Pageable pageable);
}
