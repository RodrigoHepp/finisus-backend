package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.HistoricoParticipanteDivisaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoParticipanteDivisaoJpaRepository
        extends JpaRepository<HistoricoParticipanteDivisaoJpaEntity, Long> {
    List<HistoricoParticipanteDivisaoJpaEntity> findByDivisaoIdAndVigenteAteIsNull(Long divisaoId);
    List<HistoricoParticipanteDivisaoJpaEntity> findByDivisaoIdOrderByVigenteDesdeAscUsuarioIdAsc(Long divisaoId);
}
