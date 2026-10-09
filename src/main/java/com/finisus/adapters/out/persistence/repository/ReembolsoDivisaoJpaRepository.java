package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.ReembolsoDivisaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReembolsoDivisaoJpaRepository extends JpaRepository<ReembolsoDivisaoJpaEntity, Long> {
    boolean existsByTransacaoAtivaId(Long transacaoId);
    List<ReembolsoDivisaoJpaEntity> findByDivisaoIdOrderByCriadoEmAscIdAsc(Long divisaoId);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ReembolsoDivisaoJpaEntity r
               set r.canceladoEm = :agora, r.canceladoPor = :canceladoPor, r.transacaoAtivaId = null
             where r.id = :reembolsoId and r.divisao.id = :divisaoId and r.canceladoEm is null
            """)
    int cancelarAtivo(@Param("divisaoId") Long divisaoId, @Param("reembolsoId") Long reembolsoId,
            @Param("agora") LocalDateTime agora, @Param("canceladoPor") Long canceladoPor);
    @Query("""
            select r from ReembolsoDivisaoJpaEntity r join fetch r.transacao t
            where r.divisao.id = :divisaoId and r.canceladoEm is null
              and t.estornadoEm is null and t.data between :inicio and :fim
            order by t.data, r.id
            """)
    List<ReembolsoDivisaoJpaEntity> findAtivos(@Param("divisaoId") Long divisaoId,
            @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
