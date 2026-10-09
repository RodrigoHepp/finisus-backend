package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.ParcelaFinanciamentoJpaEntity;
import com.finisus.domain.model.StatusParcelaFinanciamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ParcelaFinanciamentoJpaRepository extends JpaRepository<ParcelaFinanciamentoJpaEntity, Long> {
	List<ParcelaFinanciamentoJpaEntity> findByFinanciamentoIdOrderByNumero(Long financiamentoId);

	Page<ParcelaFinanciamentoJpaEntity> findByFinanciamentoId(Long financiamentoId, Pageable pageable);

	List<ParcelaFinanciamentoJpaEntity> findByStatusAndDataVencimentoBefore(StatusParcelaFinanciamento status,
			LocalDate data);

	List<ParcelaFinanciamentoJpaEntity> findByFinanciamentoIdAndNumeroGreaterThanEqualAndStatusNot(Long financiamentoId,
			Integer numero, StatusParcelaFinanciamento status);

	boolean existsByFinanciamentoIdAndStatus(Long financiamentoId, StatusParcelaFinanciamento status);
	boolean existsByTransacaoId(Long transacaoId);

	@Query("""
			select p from ParcelaFinanciamentoJpaEntity p
			join fetch p.financiamento f
			where f.usuarioId = :usuarioId
			  and p.status <> :statusPaga
			  and p.dataVencimento >= :inicio
			  and p.dataVencimento < :fimExclusivo
			order by p.dataVencimento, p.id
			""")
	List<ParcelaFinanciamentoJpaEntity> findRelevantByUsuarioIdAndPeriodo(@Param("usuarioId") Long usuarioId,
			@Param("inicio") LocalDate inicio, @Param("fimExclusivo") LocalDate fimExclusivo,
			@Param("statusPaga") StatusParcelaFinanciamento statusPaga);

	@Query("""
			select p from ParcelaFinanciamentoJpaEntity p
			join fetch p.financiamento f
			where f.usuarioId = :usuarioId and f.status = com.finisus.domain.model.StatusFinanciamento.ATIVO
			  and p.status <> :statusPaga
			order by p.dataVencimento, p.id
			""")
	List<ParcelaFinanciamentoJpaEntity> findPendentesByUsuarioId(@Param("usuarioId") Long usuarioId,
			@Param("statusPaga") StatusParcelaFinanciamento statusPaga);
}
