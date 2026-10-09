package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.PagamentoObrigacaoJpaEntity;
import com.finisus.adapters.out.persistence.repository.PagamentoObrigacaoJpaRepository;
import com.finisus.application.ports.out.PagamentoObrigacaoRepositoryPort;
import com.finisus.domain.model.PagamentoObrigacao;
import com.finisus.domain.vo.ValorMonetario;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PagamentoObrigacaoPersistenceAdapter implements PagamentoObrigacaoRepositoryPort {
	private final PagamentoObrigacaoJpaRepository repository;
	public PagamentoObrigacaoPersistenceAdapter(PagamentoObrigacaoJpaRepository repository) { this.repository = repository; }

	@Override public PagamentoObrigacao salvar(PagamentoObrigacao p) { return toDomain(repository.saveAndFlush(toEntity(p))); }
	@Override public Optional<PagamentoObrigacao> buscarPorIdObrigacaoEUsuario(Long id, Long obrigacaoId, Long usuarioId) {
		return repository.findByIdAndObrigacaoIdAndUsuarioId(id, obrigacaoId, usuarioId).map(this::toDomain);
	}
	@Override public List<PagamentoObrigacao> listarPorObrigacaoEUsuario(Long obrigacaoId, Long usuarioId) {
		return repository.findByObrigacaoIdAndUsuarioIdOrderByDataPagamentoAscIdAsc(obrigacaoId, usuarioId)
				.stream().map(this::toDomain).toList();
	}
	private PagamentoObrigacaoJpaEntity toEntity(PagamentoObrigacao p) {
		var e = new PagamentoObrigacaoJpaEntity(); e.setId(p.id()); e.setObrigacaoId(p.obrigacaoId());
		e.setUsuarioId(p.usuarioId()); e.setTransacaoId(p.transacaoId()); e.setValor(p.valor().valor());
		e.setJuros(p.juros().valor()); e.setEncargos(p.encargos().valor()); e.setDesconto(p.desconto().valor());
		e.setDataPagamento(p.dataPagamento()); e.setEstornadoEm(p.estornadoEm()); e.setVersion(p.version()); return e;
	}
	private PagamentoObrigacao toDomain(PagamentoObrigacaoJpaEntity e) {
		return new PagamentoObrigacao(e.getId(), e.getObrigacaoId(), e.getUsuarioId(), e.getTransacaoId(),
				ValorMonetario.of(e.getValor()), ValorMonetario.of(e.getJuros()), ValorMonetario.of(e.getEncargos()),
				ValorMonetario.of(e.getDesconto()), e.getDataPagamento(), e.getEstornadoEm(),
				e.getVersion() == null ? 0 : e.getVersion());
	}
}
