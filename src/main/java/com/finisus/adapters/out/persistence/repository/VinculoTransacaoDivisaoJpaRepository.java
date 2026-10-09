package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.VinculoTransacaoDivisaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.time.LocalDate;
import java.util.List;

public interface VinculoTransacaoDivisaoJpaRepository extends JpaRepository<VinculoTransacaoDivisaoJpaEntity, Long> {
    boolean existsByTransacaoAtivaId(Long transacaoId);

    @Modifying
    @Query("""
            update VinculoTransacaoDivisaoJpaEntity v
               set v.canceladoEm = CURRENT_TIMESTAMP,
                   v.canceladoPor = :canceladoPor,
                   v.transacaoAtivaId = null
             where v.divisao.id = :divisaoId
               and v.transacao.id = :transacaoId
               and v.canceladoEm is null
            """)
    int cancelar(@Param("divisaoId") Long divisaoId, @Param("transacaoId") Long transacaoId,
            @Param("canceladoPor") Long canceladoPor);

    @Query("""
            select v
              from VinculoTransacaoDivisaoJpaEntity v
              join v.transacao t
             where v.divisao.id = :divisaoId
               and v.canceladoEm is null
               and t.tipo = com.finisus.domain.model.TipoTransacao.SAIDA
               and t.estornadoEm is null
               and t.data between :inicio and :fim
            """)
    List<VinculoTransacaoDivisaoJpaEntity> findLancamentosAtivos(@Param("divisaoId") Long divisaoId,
            @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
            select v from VinculoTransacaoDivisaoJpaEntity v
             join fetch v.transacao t
             where v.divisao.id = :divisaoId
               and v.canceladoEm is null
               and v.statusSnapshot = com.finisus.domain.model.StatusSnapshotDivisao.PENDENTE_REVISAO
             order by t.data, t.id
            """)
    List<VinculoTransacaoDivisaoJpaEntity> findPendentes(@Param("divisaoId") Long divisaoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VinculoTransacaoDivisaoJpaEntity> findByDivisaoIdAndTransacaoIdAndCanceladoEmIsNull(Long divisaoId,
            Long transacaoId);

    @Query("""
            select v.baseCompartilhada from VinculoTransacaoDivisaoJpaEntity v
            where v.divisao.id = :divisaoId
              and v.transacao.id = :transacaoId
              and v.canceladoEm is null
            """)
    Optional<java.math.BigDecimal> findBaseCompartilhadaAtiva(@Param("divisaoId") Long divisaoId,
            @Param("transacaoId") Long transacaoId);
}
