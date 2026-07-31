package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.TransacaoJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TransacaoJpaRepository extends JpaRepository<TransacaoJpaEntity, Long> {

	Optional<TransacaoJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select t from TransacaoJpaEntity t where t.id = :id and t.usuarioId = :usuarioId")
	Optional<TransacaoJpaEntity> findByIdAndUsuarioIdForUpdate(@Param("id") Long id,
			@Param("usuarioId") Long usuarioId);

	List<TransacaoJpaEntity> findByUsuarioId(Long usuarioId);

	Page<TransacaoJpaEntity> findByUsuarioId(Long usuarioId, Pageable pageable);

	List<TransacaoJpaEntity> findByContaId(Long contaId);

	List<TransacaoJpaEntity> findByFaturaId(Long faturaId);

	List<TransacaoJpaEntity> findByCompraParceladaId(Long compraParceladaId);
}
