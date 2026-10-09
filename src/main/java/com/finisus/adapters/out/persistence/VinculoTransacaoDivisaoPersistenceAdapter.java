package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.DivisaoCompartilhadaJpaEntity;
import com.finisus.adapters.out.persistence.entity.TransacaoJpaEntity;
import com.finisus.adapters.out.persistence.entity.VinculoTransacaoDivisaoJpaEntity;
import com.finisus.adapters.out.persistence.repository.VinculoTransacaoDivisaoJpaRepository;
import com.finisus.adapters.out.persistence.repository.ResponsabilidadeTransacaoDivisaoJpaRepository;
import com.finisus.adapters.out.persistence.entity.ResponsabilidadeTransacaoDivisaoJpaEntity;
import com.finisus.application.ports.out.VinculoTransacaoDivisaoRepositoryPort;
import com.finisus.domain.vo.ValorMonetario;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import com.finisus.domain.model.StatusSnapshotDivisao;

@Component
@Transactional
public class VinculoTransacaoDivisaoPersistenceAdapter implements VinculoTransacaoDivisaoRepositoryPort {
    private final VinculoTransacaoDivisaoJpaRepository repository;
    private final EntityManager entityManager;
    private final ResponsabilidadeTransacaoDivisaoJpaRepository responsabilidades;

    public VinculoTransacaoDivisaoPersistenceAdapter(VinculoTransacaoDivisaoJpaRepository repository,
            EntityManager entityManager, ResponsabilidadeTransacaoDivisaoJpaRepository responsabilidades) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.responsabilidades = responsabilidades;
    }

    @Override
    public void associar(Long divisaoId, Long transacaoId, List<Responsabilidade> snapshots) {
        BigDecimal valorIntegral = entityManager.getReference(TransacaoJpaEntity.class, transacaoId).getValor();
        associar(divisaoId, transacaoId, snapshots, ValorMonetario.of(valorIntegral));
    }

    @Override
    public void associar(Long divisaoId, Long transacaoId, List<Responsabilidade> snapshots,
            ValorMonetario baseCompartilhada) {
        VinculoTransacaoDivisaoJpaEntity entity = new VinculoTransacaoDivisaoJpaEntity();
        entity.setDivisao(entityManager.getReference(DivisaoCompartilhadaJpaEntity.class, divisaoId));
        entity.setTransacao(entityManager.getReference(TransacaoJpaEntity.class, transacaoId));
        entity.setStatusSnapshot(StatusSnapshotDivisao.CONFIRMADO);
        entity.setBaseCompartilhada(baseCompartilhada.valor());
        entity.setTransacaoAtivaId(transacaoId);
        VinculoTransacaoDivisaoJpaEntity salvo = repository.save(entity);
        salvarResponsabilidades(salvo, snapshots);
    }

    private void salvarResponsabilidades(VinculoTransacaoDivisaoJpaEntity vinculo,
            List<Responsabilidade> snapshots) {
        responsabilidades.saveAll(snapshots.stream().map(snapshot -> {
            ResponsabilidadeTransacaoDivisaoJpaEntity responsabilidade = new ResponsabilidadeTransacaoDivisaoJpaEntity();
            responsabilidade.setVinculo(vinculo);
            responsabilidade.setUsuarioId(snapshot.usuarioId());
            responsabilidade.setPercentual(snapshot.percentual());
            responsabilidade.setValorDevido(snapshot.valorDevido().valor());
            return responsabilidade;
        }).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorTransacaoId(Long transacaoId) {
        return repository.existsByTransacaoAtivaId(transacaoId);
    }

    @Override
    public boolean cancelar(Long divisaoId, Long transacaoId, Long canceladoPor) {
        return repository.cancelar(divisaoId, transacaoId, canceladoPor) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LancamentoDivisao> listarLancamentosAtivos(Long divisaoId, LocalDate inicio, LocalDate fim) {
        List<VinculoTransacaoDivisaoJpaEntity> lancamentos = repository.findLancamentosAtivos(divisaoId, inicio, fim);
        Map<Long, List<ResponsabilidadeTransacaoDivisaoJpaEntity>> porVinculo = lancamentos.isEmpty() ? Map.of()
                : responsabilidades.findByVinculoIdIn(lancamentos.stream().map(VinculoTransacaoDivisaoJpaEntity::getId).toList())
                        .stream().collect(Collectors.groupingBy(r -> r.getVinculo().getId()));
        return lancamentos.stream().map(lancamento -> new LancamentoDivisao(
                lancamento.getTransacao().getUsuarioId(), ValorMonetario.of(lancamento.getBaseCompartilhada()),
                porVinculo.getOrDefault(lancamento.getId(), List.of()).stream()
                        .map(r -> new Responsabilidade(r.getUsuarioId(), r.getPercentual(),
                                ValorMonetario.of(r.getValorDevido())))
                        .toList(),
                lancamento.getStatusSnapshot() == StatusSnapshotDivisao.PENDENTE_REVISAO)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VinculoPendente> listarPendentes(Long divisaoId) {
        return repository.findPendentes(divisaoId).stream().map(vinculo -> new VinculoPendente(
                vinculo.getTransacao().getId(), vinculo.getTransacao().getUsuarioId(),
                vinculo.getTransacao().getData(), vinculo.getTransacao().getDescricao(),
                ValorMonetario.of(vinculo.getTransacao().getValor()))).toList();
    }

    @Override
    public Optional<StatusSnapshotDivisao> buscarStatusParaAtualizacao(Long divisaoId, Long transacaoId) {
        return repository.findByDivisaoIdAndTransacaoIdAndCanceladoEmIsNull(divisaoId, transacaoId)
                .map(VinculoTransacaoDivisaoJpaEntity::getStatusSnapshot);
    }

    @Override
    public void substituirResponsabilidades(Long divisaoId, Long transacaoId, List<Responsabilidade> snapshots) {
        VinculoTransacaoDivisaoJpaEntity vinculo = repository
                .findByDivisaoIdAndTransacaoIdAndCanceladoEmIsNull(divisaoId, transacaoId)
                .orElseThrow();
        responsabilidades.deleteByVinculoId(vinculo.getId());
        salvarResponsabilidades(vinculo, snapshots);
        vinculo.setStatusSnapshot(StatusSnapshotDivisao.CONFIRMADO);
        repository.save(vinculo);
    }
}
