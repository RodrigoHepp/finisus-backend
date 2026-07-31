package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {

	Optional<RefreshTokenJpaEntity> findByTokenAndInvalidadoFalseAndExpiraEmAfter(String token, Instant agora);

	@Modifying
	@Query("UPDATE RefreshTokenJpaEntity r SET r.invalidado = true WHERE r.token = :token")
	void invalidarPorToken(@Param("token") String token);

	@Modifying
	@Query("UPDATE RefreshTokenJpaEntity r SET r.invalidado = true WHERE r.usuarioId = :usuarioId")
	void invalidarTodosDoUsuario(@Param("usuarioId") Long usuarioId);
}
