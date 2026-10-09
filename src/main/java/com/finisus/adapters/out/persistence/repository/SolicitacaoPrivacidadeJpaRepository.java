package com.finisus.adapters.out.persistence.repository;

import com.finisus.adapters.out.persistence.entity.SolicitacaoPrivacidadeJpaEntity;
import com.finisus.domain.model.SolicitacaoPrivacidade;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoPrivacidadeJpaRepository extends JpaRepository<SolicitacaoPrivacidadeJpaEntity, Long> {
	Optional<SolicitacaoPrivacidadeJpaEntity> findFirstByUsuarioIdAndTipoAndStatusInOrderBySolicitadaEmDescIdDesc(
			Long usuarioId, SolicitacaoPrivacidade.Tipo tipo, Collection<SolicitacaoPrivacidade.Status> status);
	List<SolicitacaoPrivacidadeJpaEntity> findByUsuarioIdOrderBySolicitadaEmDescIdDesc(Long usuarioId);
}
