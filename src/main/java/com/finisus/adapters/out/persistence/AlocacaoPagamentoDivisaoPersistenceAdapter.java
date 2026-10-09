package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.AlocacaoPagamentoDivisaoJpaEntity;
import com.finisus.adapters.out.persistence.entity.TransacaoJpaEntity;
import com.finisus.adapters.out.persistence.entity.VinculoTransacaoDivisaoJpaEntity;
import com.finisus.adapters.out.persistence.repository.AlocacaoPagamentoDivisaoJpaRepository;
import com.finisus.adapters.out.persistence.repository.VinculoTransacaoDivisaoJpaRepository;
import com.finisus.application.ports.out.AlocacaoPagamentoDivisaoRepositoryPort;
import com.finisus.domain.model.AlocacaoPagamentoDivisao;
import com.finisus.domain.vo.ValorMonetario;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class AlocacaoPagamentoDivisaoPersistenceAdapter implements AlocacaoPagamentoDivisaoRepositoryPort {
    private final AlocacaoPagamentoDivisaoJpaRepository repository;
    private final VinculoTransacaoDivisaoJpaRepository vinculos;
    private final EntityManager entityManager;

    public AlocacaoPagamentoDivisaoPersistenceAdapter(AlocacaoPagamentoDivisaoJpaRepository repository,
            VinculoTransacaoDivisaoJpaRepository vinculos, EntityManager entityManager) {
        this.repository = repository;
        this.vinculos = vinculos;
        this.entityManager = entityManager;
    }

    @Override
    public AlocacaoPagamentoDivisao criarInicial(Long divisaoId, Long transacaoId, Long pagadorId,
            ValorMonetario valor) {
        VinculoTransacaoDivisaoJpaEntity vinculo = vinculos
                .findByDivisaoIdAndTransacaoIdAndCanceladoEmIsNull(divisaoId, transacaoId).orElseThrow();
        AlocacaoPagamentoDivisaoJpaEntity entity = new AlocacaoPagamentoDivisaoJpaEntity();
        entity.setVinculo(vinculo);
        entity.setTransacao(entityManager.getReference(TransacaoJpaEntity.class, transacaoId));
        entity.setPagadorId(pagadorId);
        entity.setValor(valor.valor());
        entity.setCriadaEm(LocalDateTime.now());
        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlocacaoPagamentoDivisao> listar(Long divisaoId, Long transacaoDespesaId) {
        return repository.findByVinculoDivisaoIdAndVinculoTransacaoIdOrderByCriadaEmAscIdAsc(
                divisaoId, transacaoDespesaId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlocacaoPagamentoDivisao> listarAtivas(Long divisaoId, Long transacaoDespesaId) {
        return repository.findAtivasPorVinculo(divisaoId, transacaoDespesaId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlocacaoPagamentoDivisao> listarAtivas(Long divisaoId, LocalDate inicio, LocalDate fim) {
        return repository.findAtivasPorDivisaoEPeriodo(divisaoId, inicio, fim).stream()
                .map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ValorMonetario> buscarBaseCompartilhadaAtiva(Long divisaoId, Long transacaoDespesaId) {
        return vinculos.findBaseCompartilhadaAtiva(divisaoId, transacaoDespesaId).map(ValorMonetario::of);
    }

    @Override
    public Optional<ValorMonetario> buscarBaseCompartilhadaAtivaParaAtualizacao(Long divisaoId,
            Long transacaoDespesaId) {
        return vinculos.findByDivisaoIdAndTransacaoIdAndCanceladoEmIsNull(divisaoId, transacaoDespesaId)
                .map(vinculo -> ValorMonetario.of(vinculo.getBaseCompartilhada()));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal totalAtivoPorTransacaoExcluindoVinculo(Long transacaoId, Long divisaoId,
            Long transacaoDespesaId) {
        return repository.sumAtivoPorTransacaoExcluindoVinculo(transacaoId, divisaoId, transacaoDespesaId);
    }

    @Override
    public List<AlocacaoPagamentoDivisao> substituir(Long divisaoId, Long transacaoDespesaId, Long canceladoPor,
            List<NovaAlocacao> novasAlocacoes) {
        VinculoTransacaoDivisaoJpaEntity vinculo = vinculos
                .findByDivisaoIdAndTransacaoIdAndCanceladoEmIsNull(divisaoId, transacaoDespesaId).orElseThrow();
        LocalDateTime agora = LocalDateTime.now();
        repository.cancelarAtivas(vinculo.getId(), agora, canceladoPor);
        return repository.saveAll(novasAlocacoes.stream().map(nova -> {
            AlocacaoPagamentoDivisaoJpaEntity entity = new AlocacaoPagamentoDivisaoJpaEntity();
            entity.setVinculo(vinculo);
            entity.setTransacao(entityManager.getReference(TransacaoJpaEntity.class, nova.transacaoId()));
            entity.setPagadorId(nova.pagadorId());
            entity.setValor(nova.valor().valor());
            entity.setCriadaEm(agora);
            return entity;
        }).toList()).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean cancelar(Long divisaoId, Long transacaoDespesaId, Long alocacaoId, Long canceladoPor) {
        return repository.cancelarAtiva(divisaoId, transacaoDespesaId, alocacaoId, LocalDateTime.now(), canceladoPor) > 0;
    }

    private AlocacaoPagamentoDivisao toDomain(AlocacaoPagamentoDivisaoJpaEntity entity) {
        return new AlocacaoPagamentoDivisao(entity.getId(), entity.getTransacao().getId(), entity.getPagadorId(),
                ValorMonetario.of(entity.getValor()), entity.getCriadaEm(), entity.getCanceladaEm(),
                entity.getCanceladaPor());
    }
}
