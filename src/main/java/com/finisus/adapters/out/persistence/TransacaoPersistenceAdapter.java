package com.finisus.adapters.out.persistence;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.finisus.adapters.out.persistence.entity.TransacaoHistoricoJpaEntity;
import com.finisus.adapters.out.persistence.entity.TransacaoItemJpaEntity;
import com.finisus.adapters.out.persistence.entity.TransacaoJpaEntity;
import com.finisus.adapters.out.persistence.repository.TransacaoHistoricoJpaRepository;
import com.finisus.adapters.out.persistence.repository.TransacaoJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.in.TransacaoUseCase.FiltroListagem;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TransacaoHistorico;
import com.finisus.domain.model.TransacaoItem;
import com.finisus.domain.vo.ValorMonetario;

@Component
@Transactional
public class TransacaoPersistenceAdapter implements TransacaoRepositoryPort {
	private final TransacaoJpaRepository repository;
	private final TransacaoHistoricoJpaRepository historicoRepository;

	public TransacaoPersistenceAdapter(TransacaoJpaRepository repository,
			TransacaoHistoricoJpaRepository historicoRepository) {
		this.repository = repository;
		this.historicoRepository = historicoRepository;
	}

	@Override
	public Transacao salvar(Transacao transacao) {
		TransacaoJpaEntity e = new TransacaoJpaEntity();
		e.setId(transacao.getId());
		e.setUsuarioId(transacao.getUsuarioId());
		e.setTipo(transacao.getTipo());
		e.setValor(transacao.getValor().valor());
		e.setData(transacao.getData());
		e.setDescricao(transacao.getDescricao());
		e.setContaId(transacao.getContaId());
		e.setCategoriaId(transacao.getCategoriaId());
		e.setMeioPagamentoId(transacao.getMeioPagamentoId());
		e.setFaturaId(transacao.getFaturaId());
		e.setFaturaPagamentoId(transacao.getFaturaPagamentoId());
		e.setCompraParceladaId(transacao.getCompraParceladaId());
		e.setRecorrenciaId(transacao.getRecorrenciaId());
		e.setTransferenciaId(transacao.getTransferenciaId());
		e.setEstornadoEm(transacao.getEstornadoEm());
		e.setVersion(transacao.getVersion());
		for (TransacaoItem item : transacao.getItens()) {
			TransacaoItemJpaEntity itemEntity = new TransacaoItemJpaEntity();
			itemEntity.setId(item.getId());
			itemEntity.setTransacao(e);
			itemEntity.setItemId(item.getItemId());
			itemEntity.setDescricao(item.getDescricao());
			itemEntity.setQuantidade(item.getQuantidade());
			itemEntity.setValor(item.getValor().valor());
			itemEntity.setCategoriaId(item.getCategoriaId());
			e.getItens().add(itemEntity);
		}
		return toDomain(repository.save(e));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Transacao> buscarPorId(Long id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Transacao> buscarPorIdParaAtualizacao(Long id) {
		return repository.findByIdForUpdate(id).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Transacao> buscarPorIdEUsuario(Long id, Long usuarioId) {
		return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain);
	}

	@Override
	public Optional<Transacao> buscarPorIdEUsuarioParaAtualizacao(Long id, Long usuarioId) {
		return repository.findByIdAndUsuarioIdForUpdate(id, usuarioId).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Transacao> listarPorUsuario(Long usuarioId) {
		return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<Transacao> listarPorUsuario(Long usuarioId, Paginacao paginacao) {
		return PaginaJpaMapper.map(repository.findByUsuarioId(usuarioId,
				PaginaJpaMapper.pageable(paginacao, Sort.by("data").descending().and(Sort.by("id").descending()))),
				this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<Transacao> listarPorUsuario(Long usuarioId, Paginacao paginacao, FiltroListagem filtro) {
		Specification<TransacaoJpaEntity> especificacao = (root, query, builder) -> builder.equal(root.get("usuarioId"), usuarioId);
		if (filtro.mes() != null) {
			especificacao = especificacao.and((root, query, builder) -> builder.and(
					builder.greaterThanOrEqualTo(root.get("data"), filtro.mes().primeiroDia()),
					builder.lessThan(root.get("data"), filtro.mes().proximo().primeiroDia())));
		}
		if (filtro.tipo() != null) {
			especificacao = especificacao.and((root, query, builder) -> builder.equal(root.get("tipo"), filtro.tipo()));
		}
		if (filtro.categoriaId() != null) {
			especificacao = especificacao
					.and((root, query, builder) -> builder.equal(root.get("categoriaId"), filtro.categoriaId()));
		}
		return PaginaJpaMapper.map(repository.findAll(especificacao,
				PaginaJpaMapper.pageable(paginacao, Sort.by("data").descending().and(Sort.by("id").descending()))), this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Transacao> listarPorFatura(Long faturaId) {
		return repository.findByFaturaId(faturaId).stream().map(this::toDomain).toList();
	}

	@Override
	public java.util.Map<Long, List<Transacao>> listarPorFaturas(List<Long> faturasIds) {
		if (faturasIds.isEmpty()) return java.util.Map.of();
		return repository.findByFaturaIdIn(faturasIds).stream().map(this::toDomain)
				.collect(java.util.stream.Collectors.groupingBy(Transacao::getFaturaId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Transacao> listarPagamentosPorFatura(Long faturaId) {
		return repository.findByFaturaPagamentoId(faturaId).stream().map(this::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Transacao> listarPorCompraParcelada(Long compraParceladaId) {
		return repository.findByCompraParceladaId(compraParceladaId).stream().map(this::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Transacao> listarPorConta(Long contaId) {
		return repository.findByContaId(contaId).stream().map(this::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Transacao> listarPorTransferencia(Long transferenciaId) {
		return repository.findByTransferenciaId(transferenciaId).stream().map(this::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Transacao> listarPorUsuarioEPeriodo(Long usuarioId, LocalDate inicio, LocalDate fim) {
		return repository.findByUsuarioIdAndDataGreaterThanEqualAndDataLessThan(usuarioId, inicio, fim).stream()
				.map(this::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Transacao> buscarCandidatosImportacaoPorConta(Long usuarioId, Long contaId,
			com.finisus.domain.model.TipoTransacao tipo, java.math.BigDecimal valor, LocalDate inicio, LocalDate fim) {
		return repository.findCandidatosImportacaoPorConta(usuarioId, contaId, tipo, valor, inicio, fim).stream()
				.map(this::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Transacao> buscarCandidatosImportacaoPorFatura(Long usuarioId, Long faturaId,
			com.finisus.domain.model.TipoTransacao tipo, java.math.BigDecimal valor, LocalDate inicio, LocalDate fim) {
		return repository.findCandidatosImportacaoPorFatura(usuarioId, faturaId, tipo, valor, inicio, fim).stream()
				.map(this::toDomain).toList();
	}

	@Override
	public TransacaoHistorico salvarHistorico(TransacaoHistorico historico) {
		TransacaoHistoricoJpaEntity e = new TransacaoHistoricoJpaEntity();
		e.setId(historico.getId());
		e.setTransacaoId(historico.getTransacaoId());
		e.setCampoAlterado(historico.getCampoAlterado());
		e.setValorAnterior(historico.getValorAnterior());
		e.setValorNovo(historico.getValorNovo());
		e.setAlteradoPor(historico.getAlteradoPor());
		e.setAlteradoEm(historico.getAlteradoEm());
		e.setMotivo(historico.getMotivo());
		e.setCorrelacaoId(historico.getCorrelacaoId());
		e.setSnapshotAnterior(historico.getSnapshotAnterior());
		e.setSnapshotNovo(historico.getSnapshotNovo());
		return toDomain(historicoRepository.save(e));
	}

	@Override
	@Transactional(readOnly = true)
	public List<TransacaoHistorico> listarHistoricoPorTransacao(Long transacaoId) {
		return historicoRepository.findByTransacaoIdOrderByAlteradoEmDesc(transacaoId).stream().map(this::toDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<TransacaoHistorico> listarHistoricoPorTransacao(Long transacaoId, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				historicoRepository
						.findByTransacaoId(transacaoId,
								PaginaJpaMapper.pageable(paginacao,
										Sort.by("alteradoEm").descending().and(Sort.by("id").descending()))),
				this::toDomain);
	}

	private Transacao toDomain(TransacaoJpaEntity e) {
		List<TransacaoItem> itens = e.getItens().stream().map(i -> TransacaoItem.reconstituir(i.getId(), i.getItemId(),
				i.getDescricao(), i.getQuantidade(), ValorMonetario.of(i.getValor()), i.getCategoriaId())).toList();
		return Transacao.reconstituirComTransferencia(e.getId(), e.getUsuarioId(), e.getTipo(), ValorMonetario.of(e.getValor()),
				e.getData(), e.getDescricao(), e.getContaId(), e.getCategoriaId(), e.getMeioPagamentoId(),
				e.getFaturaId(), e.getFaturaPagamentoId(), e.getCompraParceladaId(), e.getRecorrenciaId(),
				e.getTransferenciaId(), e.getEstornadoEm(), e.getVersion() == null ? 0 : e.getVersion(), itens);
	}

	private TransacaoHistorico toDomain(TransacaoHistoricoJpaEntity e) {
		return TransacaoHistorico.reconstituir(e.getId(), e.getTransacaoId(), e.getCampoAlterado(),
				e.getValorAnterior(), e.getValorNovo(), e.getAlteradoPor(), e.getAlteradoEm(), e.getMotivo(),
				e.getCorrelacaoId(), e.getSnapshotAnterior(), e.getSnapshotNovo());
	}
}
