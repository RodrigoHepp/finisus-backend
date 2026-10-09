package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.AlocacaoPagamentoDivisaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AlocacaoPagamentoDivisaoJpaRepository
        extends JpaRepository<AlocacaoPagamentoDivisaoJpaEntity, Long> {
    List<AlocacaoPagamentoDivisaoJpaEntity> findByVinculoDivisaoIdAndVinculoTransacaoIdOrderByCriadaEmAscIdAsc(
            Long divisaoId, Long transacaoId);

    @Query("""
            select a from AlocacaoPagamentoDivisaoJpaEntity a
             join fetch a.transacao pagamento
            where a.vinculo.divisao.id = :divisaoId
              and a.vinculo.transacao.id = :transacaoId
              and a.vinculo.canceladoEm is null
              and a.canceladaEm is null
              and pagamento.tipo = com.finisus.domain.model.TipoTransacao.SAIDA
              and pagamento.estornadoEm is null
              and pagamento.faturaId is null
              and pagamento.compraParceladaId is null
            order by a.criadaEm, a.id
            """)
    List<AlocacaoPagamentoDivisaoJpaEntity> findAtivasPorVinculo(@Param("divisaoId") Long divisaoId,
            @Param("transacaoId") Long transacaoId);

    @Query("""
            select a from AlocacaoPagamentoDivisaoJpaEntity a
             join fetch a.transacao pagamento
             join a.vinculo vinculo
             join vinculo.transacao despesa
            where vinculo.divisao.id = :divisaoId
              and vinculo.canceladoEm is null
              and vinculo.statusSnapshot = com.finisus.domain.model.StatusSnapshotDivisao.CONFIRMADO
              and a.canceladaEm is null
              and pagamento.tipo = com.finisus.domain.model.TipoTransacao.SAIDA
              and pagamento.estornadoEm is null
              and pagamento.faturaId is null
              and pagamento.compraParceladaId is null
              and despesa.data between :inicio and :fim
            """)
    List<AlocacaoPagamentoDivisaoJpaEntity> findAtivasPorDivisaoEPeriodo(@Param("divisaoId") Long divisaoId,
            @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
            select coalesce(sum(a.valor), 0) from AlocacaoPagamentoDivisaoJpaEntity a
            where a.transacao.id = :transacaoId
              and a.canceladaEm is null
              and a.vinculo.canceladoEm is null
              and not (a.vinculo.divisao.id = :divisaoId and a.vinculo.transacao.id = :transacaoDespesaId)
            """)
    BigDecimal sumAtivoPorTransacaoExcluindoVinculo(@Param("transacaoId") Long transacaoId,
            @Param("divisaoId") Long divisaoId, @Param("transacaoDespesaId") Long transacaoDespesaId);

    @Modifying
    @Query("""
            update AlocacaoPagamentoDivisaoJpaEntity a
               set a.canceladaEm = :agora, a.canceladaPor = :canceladoPor
            where a.vinculo.id = :vinculoId and a.canceladaEm is null
            """)
    int cancelarAtivas(@Param("vinculoId") Long vinculoId, @Param("agora") LocalDateTime agora,
            @Param("canceladoPor") Long canceladoPor);

    @Modifying
    @Query("""
            update AlocacaoPagamentoDivisaoJpaEntity a
               set a.canceladaEm = :agora, a.canceladaPor = :canceladoPor
             where a.id = :alocacaoId
               and a.vinculo.divisao.id = :divisaoId
               and a.vinculo.transacao.id = :transacaoDespesaId
               and a.vinculo.canceladoEm is null
               and a.canceladaEm is null
            """)
    int cancelarAtiva(@Param("divisaoId") Long divisaoId,
            @Param("transacaoDespesaId") Long transacaoDespesaId, @Param("alocacaoId") Long alocacaoId,
            @Param("agora") LocalDateTime agora, @Param("canceladoPor") Long canceladoPor);
}
