package com.finisus.adapters.out.persistence;

import java.util.Optional;
import org.springframework.stereotype.Component;
import com.finisus.adapters.out.persistence.entity.TransferenciaContaJpaEntity;
import com.finisus.adapters.out.persistence.repository.TransferenciaContaJpaRepository;
import com.finisus.application.ports.out.TransferenciaContaRepositoryPort;
import com.finisus.domain.model.TransferenciaConta;
import com.finisus.domain.vo.ValorMonetario;

@Component
public class TransferenciaContaPersistenceAdapter implements TransferenciaContaRepositoryPort {
	private final TransferenciaContaJpaRepository repository;
	public TransferenciaContaPersistenceAdapter(TransferenciaContaJpaRepository repository) { this.repository = repository; }
	public TransferenciaConta salvar(TransferenciaConta t) {
		var e = new TransferenciaContaJpaEntity();
		e.setId(t.id()); e.setUsuarioId(t.usuarioId()); e.setContaOrigemId(t.contaOrigemId());
		e.setContaDestinoId(t.contaDestinoId()); e.setValor(t.valor().valor()); e.setData(t.data());
		e.setDescricao(t.descricao()); e.setChaveIdempotencia(t.chaveIdempotencia());
		e.setHashRequisicao(t.hashRequisicao()); e.setStatus(t.status()); e.setEstornadaEm(t.estornadaEm());
		e.setVersion(t.version()); return domain(repository.save(e));
	}
	public Optional<TransferenciaConta> buscarPorIdEUsuario(Long id, Long u) { return repository.findByIdAndUsuarioId(id, u).map(this::domain); }
	public Optional<TransferenciaConta> buscarPorIdEUsuarioParaAtualizacao(Long id, Long u) { return repository.findByIdAndUsuarioIdForUpdate(id, u).map(this::domain); }
	public Optional<TransferenciaConta> buscarPorUsuarioEChave(Long u, String c) { return repository.findByUsuarioIdAndChaveIdempotencia(u, c).map(this::domain); }
	private TransferenciaConta domain(TransferenciaContaJpaEntity e) {
		return new TransferenciaConta(e.getId(), e.getUsuarioId(), e.getContaOrigemId(), e.getContaDestinoId(),
				ValorMonetario.of(e.getValor()), e.getData(), e.getDescricao(), e.getChaveIdempotencia(),
				e.getHashRequisicao(), e.getStatus(), e.getEstornadaEm(), e.getVersion() == null ? 0 : e.getVersion());
	}
}
