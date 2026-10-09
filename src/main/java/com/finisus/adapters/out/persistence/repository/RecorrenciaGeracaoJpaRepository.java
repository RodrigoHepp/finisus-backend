package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.RecorrenciaGeracaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface RecorrenciaGeracaoJpaRepository extends JpaRepository<RecorrenciaGeracaoJpaEntity, Long> {

	boolean existsByRecorrenciaIdAndAnoMes(Long recorrenciaId, String anoMes);

	@Query("select r.recorrenciaId, r.anoMes from RecorrenciaGeracaoJpaEntity r "
			+ "where r.recorrenciaId in :recorrenciasIds and r.anoMes between :inicio and :fim")
	List<Object[]> findChavesByRecorrenciaIdInAndAnoMesBetween(@Param("recorrenciasIds") List<Long> recorrenciasIds,
			@Param("inicio") String inicio, @Param("fim") String fim);

	List<RecorrenciaGeracaoJpaEntity> findByUsuarioIdAndAnoMesOrderByVencimentoAscIdAsc(Long usuarioId, String anoMes);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<RecorrenciaGeracaoJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);
}
