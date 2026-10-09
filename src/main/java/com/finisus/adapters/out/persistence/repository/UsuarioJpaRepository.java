package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.UsuarioJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from UsuarioJpaEntity u where u.id = :id")
	Optional<UsuarioJpaEntity> findByIdForUpdate(@Param("id") Long id);

	Optional<UsuarioJpaEntity> findByEmail(String email);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from UsuarioJpaEntity u where u.email = :email")
	Optional<UsuarioJpaEntity> findByEmailForUpdate(@Param("email") String email);

	List<UsuarioJpaEntity> findByIdIn(Collection<Long> ids);

	boolean existsByEmail(String email);

	List<UsuarioJpaEntity> findByAtivoTrue();
}
