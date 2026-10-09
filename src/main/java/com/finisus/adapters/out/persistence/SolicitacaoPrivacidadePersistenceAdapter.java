package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.SolicitacaoPrivacidadeJpaEntity;
import com.finisus.adapters.out.persistence.repository.SolicitacaoPrivacidadeJpaRepository;
import com.finisus.application.ports.out.SolicitacaoPrivacidadeRepositoryPort;
import com.finisus.domain.model.SolicitacaoPrivacidade;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SolicitacaoPrivacidadePersistenceAdapter implements SolicitacaoPrivacidadeRepositoryPort {
	private final SolicitacaoPrivacidadeJpaRepository repository;
	public SolicitacaoPrivacidadePersistenceAdapter(SolicitacaoPrivacidadeJpaRepository repository) { this.repository = repository; }
	@Override public SolicitacaoPrivacidade salvar(SolicitacaoPrivacidade s) { return toDomain(repository.save(toEntity(s))); }
	@Override public Optional<SolicitacaoPrivacidade> buscarAberta(Long usuarioId, SolicitacaoPrivacidade.Tipo tipo) {
		return repository.findFirstByUsuarioIdAndTipoAndStatusInOrderBySolicitadaEmDescIdDesc(usuarioId, tipo,
				List.of(SolicitacaoPrivacidade.Status.SOLICITADA, SolicitacaoPrivacidade.Status.EM_ANALISE)).map(this::toDomain);
	}
	@Override public List<SolicitacaoPrivacidade> listarPorUsuario(Long usuarioId) {
		return repository.findByUsuarioIdOrderBySolicitadaEmDescIdDesc(usuarioId).stream().map(this::toDomain).toList();
	}
	private SolicitacaoPrivacidadeJpaEntity toEntity(SolicitacaoPrivacidade s) {
		var e = new SolicitacaoPrivacidadeJpaEntity(); e.setId(s.id()); e.setUsuarioId(s.usuarioId()); e.setTipo(s.tipo());
		e.setStatus(s.status()); e.setMotivo(s.motivo()); e.setSolicitadaEm(s.solicitadaEm()); e.setConcluidaEm(s.concluidaEm());
		e.setObservacao(s.observacao()); e.setVersion(s.version()); return e;
	}
	private SolicitacaoPrivacidade toDomain(SolicitacaoPrivacidadeJpaEntity e) {
		return new SolicitacaoPrivacidade(e.getId(), e.getUsuarioId(), e.getTipo(), e.getStatus(), e.getMotivo(),
				e.getSolicitadaEm(), e.getConcluidaEm(), e.getObservacao(), e.getVersion() == null ? 0 : e.getVersion());
	}
}
