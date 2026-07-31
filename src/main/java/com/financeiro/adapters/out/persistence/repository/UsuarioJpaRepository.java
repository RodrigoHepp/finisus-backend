package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {

	Optional<UsuarioJpaEntity> findByEmail(String email);

	boolean existsByEmail(String email);

	List<UsuarioJpaEntity> findByAtivoTrue();
}
