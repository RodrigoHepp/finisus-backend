package com.finisus.adapters.out.persistence.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.finisus.adapters.out.persistence.entity.AplicacaoCreditoFaturaJpaEntity;

public interface AplicacaoCreditoFaturaJpaRepository extends JpaRepository<AplicacaoCreditoFaturaJpaEntity, Long> {
	@Query("select coalesce(sum(a.valor), 0) from AplicacaoCreditoFaturaJpaEntity a where a.pagamentoOrigemId = :id")
	BigDecimal sumByPagamentoOrigemId(@Param("id") Long pagamentoOrigemId);
	@Query("select coalesce(sum(a.valor), 0) from AplicacaoCreditoFaturaJpaEntity a where a.faturaDestinoId = :id")
	BigDecimal sumByFaturaDestinoId(@Param("id") Long faturaDestinoId);
	@Query("select a.faturaDestinoId, coalesce(sum(a.valor), 0) from AplicacaoCreditoFaturaJpaEntity a where a.faturaDestinoId in :ids group by a.faturaDestinoId")
	List<Object[]> sumByFaturaDestinoIdIn(@Param("ids") List<Long> faturasIds);
	boolean existsByPagamentoOrigemId(Long pagamentoOrigemId);
}
