package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.DivisaoCompartilhadaJpaEntity;
import com.finisus.domain.model.StatusDivisaoCompartilhada;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface DivisaoCompartilhadaJpaRepository extends JpaRepository<DivisaoCompartilhadaJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DivisaoCompartilhadaJpaEntity d where d.id = :id")
    Optional<DivisaoCompartilhadaJpaEntity> findByIdForUpdate(@Param("id") Long id);
    Page<DivisaoCompartilhadaJpaEntity> findDistinctByParticipantesUsuarioId(Long usuarioId, Pageable pageable);
    List<DivisaoCompartilhadaJpaEntity> findDistinctByParticipantesUsuarioIdAndStatus(Long usuarioId,
            StatusDivisaoCompartilhada status);
}
