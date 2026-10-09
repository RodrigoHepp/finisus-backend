package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.TransacaoJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import com.finisus.domain.model.TipoTransacao;

public interface TransacaoJpaRepository extends JpaRepository<TransacaoJpaEntity, Long>, JpaSpecificationExecutor<TransacaoJpaEntity> {

	Optional<TransacaoJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select t from TransacaoJpaEntity t where t.id = :id")
	Optional<TransacaoJpaEntity> findByIdForUpdate(@Param("id") Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select t from TransacaoJpaEntity t where t.id = :id and t.usuarioId = :usuarioId")
	Optional<TransacaoJpaEntity> findByIdAndUsuarioIdForUpdate(@Param("id") Long id,
			@Param("usuarioId") Long usuarioId);

	List<TransacaoJpaEntity> findByUsuarioId(Long usuarioId);

	Page<TransacaoJpaEntity> findByUsuarioId(Long usuarioId, Pageable pageable);

	List<TransacaoJpaEntity> findByContaId(Long contaId);

	List<TransacaoJpaEntity> findByFaturaId(Long faturaId);
	@EntityGraph(attributePaths = "itens")
	List<TransacaoJpaEntity> findByFaturaIdIn(List<Long> faturasIds);

	List<TransacaoJpaEntity> findByFaturaPagamentoId(Long faturaPagamentoId);

	List<TransacaoJpaEntity> findByCompraParceladaId(Long compraParceladaId);

	List<TransacaoJpaEntity> findByTransferenciaId(Long transferenciaId);

	@EntityGraph(attributePaths = "itens")
	List<TransacaoJpaEntity> findByUsuarioIdAndDataGreaterThanEqualAndDataLessThan(Long usuarioId, LocalDate inicio,
			LocalDate fimExclusivo);

	@Query("""
			select t from TransacaoJpaEntity t
			where t.usuarioId = :usuarioId and t.contaId = :contaId and t.tipo = :tipo and t.valor = :valor
			  and t.data between :inicio and :fim and t.estornadoEm is null
			order by t.data, t.id
			""")
	List<TransacaoJpaEntity> findCandidatosImportacaoPorConta(@Param("usuarioId") Long usuarioId,
			@Param("contaId") Long contaId, @Param("tipo") TipoTransacao tipo, @Param("valor") java.math.BigDecimal valor,
			@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

	@Query("""
			select t from TransacaoJpaEntity t
			where t.usuarioId = :usuarioId and t.faturaId = :faturaId and t.tipo = :tipo and t.valor = :valor
			  and t.data between :inicio and :fim and t.estornadoEm is null
			order by t.data, t.id
			""")
	List<TransacaoJpaEntity> findCandidatosImportacaoPorFatura(@Param("usuarioId") Long usuarioId,
			@Param("faturaId") Long faturaId, @Param("tipo") TipoTransacao tipo, @Param("valor") java.math.BigDecimal valor,
			@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
