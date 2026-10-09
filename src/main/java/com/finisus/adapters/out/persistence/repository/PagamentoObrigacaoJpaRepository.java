package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.PagamentoObrigacaoJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoObrigacaoJpaRepository extends JpaRepository<PagamentoObrigacaoJpaEntity, Long> {
	Optional<PagamentoObrigacaoJpaEntity> findByIdAndObrigacaoIdAndUsuarioId(Long id, Long obrigacaoId, Long usuarioId);
	List<PagamentoObrigacaoJpaEntity> findByObrigacaoIdAndUsuarioIdOrderByDataPagamentoAscIdAsc(Long obrigacaoId,
			Long usuarioId);
	boolean existsByTransacaoIdAndUsuarioId(Long transacaoId, Long usuarioId);
}
