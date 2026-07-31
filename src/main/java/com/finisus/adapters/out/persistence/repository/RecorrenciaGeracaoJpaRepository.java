package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.RecorrenciaGeracaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecorrenciaGeracaoJpaRepository extends JpaRepository<RecorrenciaGeracaoJpaEntity, Long> {

	boolean existsByRecorrenciaIdAndAnoMes(Long recorrenciaId, String anoMes);
}
