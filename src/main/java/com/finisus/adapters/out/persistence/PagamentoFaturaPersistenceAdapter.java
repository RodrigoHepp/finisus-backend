package com.finisus.adapters.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.finisus.adapters.out.persistence.entity.PagamentoFaturaJpaEntity;
import com.finisus.adapters.out.persistence.repository.PagamentoFaturaJpaRepository;
import com.finisus.application.ports.out.PagamentoFaturaRepositoryPort;
import com.finisus.domain.model.PagamentoFatura;
import com.finisus.domain.vo.ValorMonetario;

@Component
public class PagamentoFaturaPersistenceAdapter implements PagamentoFaturaRepositoryPort {
	private final PagamentoFaturaJpaRepository repository;
	public PagamentoFaturaPersistenceAdapter(PagamentoFaturaJpaRepository repository) { this.repository = repository; }
	@Override public PagamentoFatura salvar(PagamentoFatura p) { return toDomain(repository.saveAndFlush(toEntity(p))); }
	@Override public Optional<PagamentoFatura> buscarPorUsuarioEChave(Long usuarioId, String chave) {
		return repository.findByUsuarioIdAndChaveIdempotencia(usuarioId, chave).map(this::toDomain);
	}
	@Override public List<PagamentoFatura> listarPorFaturaEUsuario(Long faturaId, Long usuarioId) {
		return repository.findByFaturaIdAndUsuarioIdOrderByDataPagamentoAscIdAsc(faturaId, usuarioId).stream()
				.map(this::toDomain).toList();
	}
	@Override public Map<Long, List<PagamentoFatura>> listarPorFaturasEUsuario(List<Long> faturasIds, Long usuarioId) {
		if (faturasIds.isEmpty()) return Map.of();
		return repository.findByFaturaIdInAndUsuarioId(faturasIds, usuarioId).stream().map(this::toDomain)
				.collect(Collectors.groupingBy(PagamentoFatura::faturaId));
	}
	private PagamentoFaturaJpaEntity toEntity(PagamentoFatura p) {
		var e = new PagamentoFaturaJpaEntity(); e.setId(p.id()); e.setFaturaId(p.faturaId());
		e.setUsuarioId(p.usuarioId()); e.setTransacaoId(p.transacaoId()); e.setContaId(p.contaId());
		e.setValor(p.valor().valor()); e.setCredito(p.credito().valor()); e.setDataPagamento(p.dataPagamento());
		e.setChaveIdempotencia(p.chaveIdempotencia()); e.setHashRequisicao(p.hashRequisicao());
		e.setEstornadoEm(p.estornadoEm()); e.setVersion(p.version()); return e;
	}
	private PagamentoFatura toDomain(PagamentoFaturaJpaEntity e) {
		return new PagamentoFatura(e.getId(), e.getFaturaId(), e.getUsuarioId(), e.getTransacaoId(), e.getContaId(),
				ValorMonetario.of(e.getValor()), ValorMonetario.of(e.getCredito()), e.getDataPagamento(),
				e.getChaveIdempotencia(), e.getHashRequisicao(), e.getEstornadoEm(), e.getVersion() == null ? 0 : e.getVersion());
	}
}
