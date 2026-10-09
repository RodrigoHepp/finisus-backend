package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.ResponsabilidadeTransacaoDivisaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface ResponsabilidadeTransacaoDivisaoJpaRepository
		extends JpaRepository<ResponsabilidadeTransacaoDivisaoJpaEntity, Long> {
	List<ResponsabilidadeTransacaoDivisaoJpaEntity> findByVinculoIdIn(Collection<Long> vinculoIds);
	long deleteByVinculoId(Long vinculoId);
}
