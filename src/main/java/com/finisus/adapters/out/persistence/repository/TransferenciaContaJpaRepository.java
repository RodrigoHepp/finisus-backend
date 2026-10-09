package com.finisus.adapters.out.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.finisus.adapters.out.persistence.entity.TransferenciaContaJpaEntity;
import jakarta.persistence.LockModeType;

public interface TransferenciaContaJpaRepository extends JpaRepository<TransferenciaContaJpaEntity, Long> {
	Optional<TransferenciaContaJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);
	Optional<TransferenciaContaJpaEntity> findByUsuarioIdAndChaveIdempotencia(Long usuarioId, String chave);
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select t from TransferenciaContaJpaEntity t where t.id = :id and t.usuarioId = :usuarioId")
	Optional<TransferenciaContaJpaEntity> findByIdAndUsuarioIdForUpdate(@Param("id") Long id,
			@Param("usuarioId") Long usuarioId);
}
