package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.DivisaoCompartilhadaJpaEntity;
import com.finisus.adapters.out.persistence.entity.ParticipanteDivisaoJpaEntity;
import com.finisus.adapters.out.persistence.repository.DivisaoCompartilhadaJpaRepository;
import com.finisus.adapters.out.persistence.repository.HistoricoParticipanteDivisaoJpaRepository;
import com.finisus.adapters.out.persistence.entity.HistoricoParticipanteDivisaoJpaEntity;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.DivisaoCompartilhadaRepositoryPort;
import com.finisus.domain.model.DivisaoCompartilhada;
import com.finisus.domain.model.ParticipanteDivisao;
import com.finisus.domain.model.HistoricoParticipanteDivisao;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;

@Component
@Transactional
public class DivisaoCompartilhadaPersistenceAdapter implements DivisaoCompartilhadaRepositoryPort {
    private final DivisaoCompartilhadaJpaRepository repository;
    private final HistoricoParticipanteDivisaoJpaRepository historicos;

    public DivisaoCompartilhadaPersistenceAdapter(DivisaoCompartilhadaJpaRepository repository,
            HistoricoParticipanteDivisaoJpaRepository historicos) {
        this.repository = repository;
        this.historicos = historicos;
    }

    @Override
    public DivisaoCompartilhada salvar(DivisaoCompartilhada divisao) {
        boolean nova = divisao.getId() == null;
        DivisaoCompartilhadaJpaEntity entity = divisao.getId() == null ? new DivisaoCompartilhadaJpaEntity()
                : repository.findById(divisao.getId()).orElseThrow();
        Map<Long, java.math.BigDecimal> participantesAnteriores = new LinkedHashMap<>();
        entity.getParticipantes().forEach(p -> participantesAnteriores.put(p.getUsuarioId(),
                normalizarPercentual(p.getPercentual())));
        Map<Long, java.math.BigDecimal> participantesAtuais = new LinkedHashMap<>();
        divisao.getParticipantes().forEach(p -> participantesAtuais.put(p.usuarioId(),
                normalizarPercentual(p.percentual())));
        entity.setCriadorId(divisao.getCriadorId());
        entity.setNome(divisao.getNome());
        entity.setStatus(divisao.getStatus());
        entity.getParticipantes().clear();
        divisao.getParticipantes().forEach(participante -> {
            ParticipanteDivisaoJpaEntity participanteEntity = new ParticipanteDivisaoJpaEntity();
            participanteEntity.setDivisao(entity);
            participanteEntity.setUsuarioId(participante.usuarioId());
            participanteEntity.setPercentual(participante.percentual());
            entity.getParticipantes().add(participanteEntity);
        });
        DivisaoCompartilhadaJpaEntity salva = repository.save(entity);
        if (nova || !participantesAnteriores.equals(participantesAtuais)) {
            atualizarHistorico(salva.getId(), divisao.getParticipantes());
        }
        return toDomain(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DivisaoCompartilhada> buscarPorId(Long divisaoId) {
        return repository.findById(divisaoId).map(this::toDomain);
    }

    @Override
    public Optional<DivisaoCompartilhada> buscarPorIdParaAtualizacao(Long divisaoId) {
        return repository.findByIdForUpdate(divisaoId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<DivisaoCompartilhada> listarPorParticipante(Long usuarioId, Paginacao paginacao) {
        return PaginaJpaMapper.map(repository.findDistinctByParticipantesUsuarioId(usuarioId,
                PaginaJpaMapper.pageable(paginacao, Sort.by("id").descending())), this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DivisaoCompartilhada> listarAtivasPorParticipante(Long usuarioId) {
        return repository.findDistinctByParticipantesUsuarioIdAndStatus(usuarioId,
                com.finisus.domain.model.StatusDivisaoCompartilhada.ATIVA).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoricoParticipanteDivisao> listarHistoricoParticipantes(Long divisaoId) {
        return historicos.findByDivisaoIdOrderByVigenteDesdeAscUsuarioIdAsc(divisaoId).stream()
                .map(h -> new HistoricoParticipanteDivisao(h.getUsuarioId(), h.getPercentual(),
                        h.getVigenteDesde(), h.getVigenteAte()))
                .toList();
    }

    private DivisaoCompartilhada toDomain(DivisaoCompartilhadaJpaEntity entity) {
        return DivisaoCompartilhada.reconstituir(entity.getId(), entity.getCriadorId(), entity.getNome(),
                entity.getParticipantes().stream()
                        .map(p -> new ParticipanteDivisao(p.getUsuarioId(), p.getPercentual())).toList(),
                entity.getStatus());
    }

    private void atualizarHistorico(Long divisaoId, List<ParticipanteDivisao> participantes) {
        LocalDateTime alteradoEm = LocalDateTime.now();
        List<HistoricoParticipanteDivisaoJpaEntity> vigentes = historicos
                .findByDivisaoIdAndVigenteAteIsNull(divisaoId);
        vigentes.forEach(historico -> historico.setVigenteAte(alteradoEm));
        historicos.saveAll(vigentes);
        historicos.saveAll(participantes.stream().map(participante -> {
            HistoricoParticipanteDivisaoJpaEntity historico = new HistoricoParticipanteDivisaoJpaEntity();
            historico.setDivisaoId(divisaoId);
            historico.setUsuarioId(participante.usuarioId());
            historico.setPercentual(participante.percentual());
            historico.setVigenteDesde(alteradoEm);
            return historico;
        }).toList());
    }

    private java.math.BigDecimal normalizarPercentual(java.math.BigDecimal percentual) {
        return percentual == null ? null : percentual.stripTrailingZeros();
    }
}
