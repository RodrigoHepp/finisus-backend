package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.FaturaJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FaturaJpaRepository extends JpaRepository<FaturaJpaEntity, Long> {

	Optional<FaturaJpaEntity> findByCartaoIdAndAnoMes(Long cartaoId, String anoMes);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select f from FaturaJpaEntity f where f.id = :id")
	Optional<FaturaJpaEntity> findByIdForUpdate(@Param("id") Long id);

	List<FaturaJpaEntity> findByCartaoId(Long cartaoId);

	Page<FaturaJpaEntity> findByCartaoId(Long cartaoId, Pageable pageable);
}
