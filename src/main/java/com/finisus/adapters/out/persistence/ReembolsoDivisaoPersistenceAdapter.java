package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.*;
import com.finisus.adapters.out.persistence.repository.ReembolsoDivisaoJpaRepository;
import com.finisus.application.ports.out.ReembolsoDivisaoRepositoryPort;
import com.finisus.domain.model.ReembolsoDivisao;
import com.finisus.domain.vo.ValorMonetario;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Component @Transactional
public class ReembolsoDivisaoPersistenceAdapter implements ReembolsoDivisaoRepositoryPort {
    private final ReembolsoDivisaoJpaRepository repository;
    private final EntityManager entityManager;
    public ReembolsoDivisaoPersistenceAdapter(ReembolsoDivisaoJpaRepository repository, EntityManager entityManager) {
        this.repository = repository; this.entityManager = entityManager;
    }
    public ReembolsoDivisao salvar(Long divisaoId, Long transacaoId, Long pagadorId, Long recebedorId,
            ValorMonetario valor) {
        var e = new ReembolsoDivisaoJpaEntity();
        e.setDivisao(entityManager.getReference(DivisaoCompartilhadaJpaEntity.class, divisaoId));
        e.setTransacao(entityManager.getReference(TransacaoJpaEntity.class, transacaoId));
        e.setPagadorId(pagadorId); e.setRecebedorId(recebedorId); e.setValor(valor.valor());
        e.setCriadoEm(LocalDateTime.now()); e.setTransacaoAtivaId(transacaoId);
        return domain(repository.save(e));
    }
    @Transactional(readOnly = true) public boolean existeAtivoPorTransacao(Long id) { return repository.existsByTransacaoAtivaId(id); }
    public boolean cancelar(Long divisaoId, Long reembolsoId, Long canceladoPor) {
        return repository.cancelarAtivo(divisaoId, reembolsoId, LocalDateTime.now(), canceladoPor) > 0;
    }
    @Transactional(readOnly = true) public List<ReembolsoDivisao> listar(Long id) { return repository.findByDivisaoIdOrderByCriadoEmAscIdAsc(id).stream().map(this::domain).toList(); }
    @Transactional(readOnly = true) public List<ReembolsoDivisao> listarAtivos(Long id, LocalDate i, LocalDate f) { return repository.findAtivos(id, i, f).stream().map(this::domain).toList(); }
    private ReembolsoDivisao domain(ReembolsoDivisaoJpaEntity e) { return new ReembolsoDivisao(e.getId(), e.getTransacao().getId(), e.getPagadorId(), e.getRecebedorId(), ValorMonetario.of(e.getValor()), e.getTransacao().getData(), e.getCriadoEm(), e.getCanceladoEm(), e.getCanceladoPor()); }
}
