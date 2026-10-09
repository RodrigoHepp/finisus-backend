package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.CategoriaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaJpaEntity, Long> {

	List<CategoriaJpaEntity> findByUsuarioIdAndAtivoTrue(Long usuarioId);

	Page<CategoriaJpaEntity> findByUsuarioIdAndAtivoTrue(Long usuarioId, Pageable pageable);

	Optional<CategoriaJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from CategoriaJpaEntity c where c.usuarioId = :usuarioId order by c.id")
	List<CategoriaJpaEntity> findAllByUsuarioIdForUpdate(@Param("usuarioId") Long usuarioId);
}
