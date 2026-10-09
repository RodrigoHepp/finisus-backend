package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.ObrigacaoFinanceiraJpaEntity;
import com.finisus.domain.model.StatusObrigacaoFinanceira;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ObrigacaoFinanceiraJpaRepository extends JpaRepository<ObrigacaoFinanceiraJpaEntity, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select o from ObrigacaoFinanceiraJpaEntity o where o.id = :id and o.usuarioId = :usuarioId")
	Optional<ObrigacaoFinanceiraJpaEntity> findByIdAndUsuarioIdForUpdate(@Param("id") Long id,
			@Param("usuarioId") Long usuarioId);

	@Query("""
			select o from ObrigacaoFinanceiraJpaEntity o
			where o.usuarioId = :usuarioId
			  and (:status is null or o.status = :status)
			  and (:inicio is null or o.dataVencimento >= :inicio)
			  and (:fim is null or o.dataVencimento <= :fim)
			""")
	Page<ObrigacaoFinanceiraJpaEntity> findByUsuarioComFiltro(@Param("usuarioId") Long usuarioId,
			@Param("status") StatusObrigacaoFinanceira status, @Param("inicio") LocalDate inicio,
			@Param("fim") LocalDate fim, Pageable pageable);

	List<ObrigacaoFinanceiraJpaEntity> findByStatusAndDataVencimentoBefore(StatusObrigacaoFinanceira status,
			LocalDate dataReferencia);

	@Query("""
			select o from ObrigacaoFinanceiraJpaEntity o
			where o.usuarioId = :usuarioId and o.status in :status
			  and o.dataVencimento >= :inicio and o.dataVencimento <= :fim
			order by o.dataVencimento, o.id
			""")
	List<ObrigacaoFinanceiraJpaEntity> findPendentesByUsuarioIdAndPeriodo(@Param("usuarioId") Long usuarioId,
			@Param("status") List<StatusObrigacaoFinanceira> status, @Param("inicio") LocalDate inicio,
			@Param("fim") LocalDate fim);
	@Query("""
			select o from ObrigacaoFinanceiraJpaEntity o
			where o.usuarioId = :usuarioId and o.contaPagamentoId = :contaId
			  and o.dataVencimento between :inicio and :fim and o.valor = :valor
			  and o.status <> com.finisus.domain.model.StatusObrigacaoFinanceira.CANCELADA
			order by o.id
			""")
	List<ObrigacaoFinanceiraJpaEntity> findCandidatosImportacao(@Param("usuarioId") Long usuarioId,
			@Param("contaId") Long contaId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim,
			@Param("valor") BigDecimal valor);
	Optional<ObrigacaoFinanceiraJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);
	boolean existsByTransacaoIdAndUsuarioId(Long transacaoId, Long usuarioId);
}
