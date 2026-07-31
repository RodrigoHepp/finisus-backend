package com.financeiro.adapters.out.persistence.repository;

import com.financeiro.adapters.out.persistence.entity.DespesaCompartilhadaJpaEntity;
import com.financeiro.domain.model.TipoAlvoCompartilhamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DespesaCompartilhadaJpaRepository extends JpaRepository<DespesaCompartilhadaJpaEntity, Long> {
	Optional<DespesaCompartilhadaJpaEntity> findByTransacaoId(Long transacaoId);

	boolean existsByTransacaoIdAndTipoAlvo(Long transacaoId, TipoAlvoCompartilhamento tipoAlvo);

	boolean existsByTransacaoIdAndTipoAlvoAndStatus(Long transacaoId, TipoAlvoCompartilhamento tipoAlvo,
			com.financeiro.domain.model.StatusDespesaCompartilhada status);

	boolean existsByTransacaoItemId(Long transacaoItemId);

	boolean existsByTransacaoItemIdAndStatus(Long transacaoItemId,
			com.financeiro.domain.model.StatusDespesaCompartilhada status);

	Optional<DespesaCompartilhadaJpaEntity> findByIdAndCriadorId(Long id, Long criadorId);

	List<DespesaCompartilhadaJpaEntity> findByCriadorId(Long criadorId);

	Page<DespesaCompartilhadaJpaEntity> findByCriadorId(Long criadorId, Pageable pageable);
}
