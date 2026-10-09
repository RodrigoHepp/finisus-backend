package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.ObrigacaoFinanceiraJpaEntity;
import com.finisus.adapters.out.persistence.repository.ObrigacaoFinanceiraJpaRepository;
import com.finisus.adapters.out.persistence.repository.PagamentoObrigacaoJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.ObrigacaoFinanceiraUseCase.FiltroListagem;
import com.finisus.application.ports.out.ObrigacaoFinanceiraRepositoryPort;
import com.finisus.domain.model.ObrigacaoFinanceira;
import com.finisus.domain.model.StatusObrigacaoFinanceira;
import com.finisus.domain.vo.ValorMonetario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import com.finisus.domain.ConflitoAtualizacaoException;

@Component
public class ObrigacaoFinanceiraPersistenceAdapter implements ObrigacaoFinanceiraRepositoryPort {
	private final ObrigacaoFinanceiraJpaRepository repository;
	private final PagamentoObrigacaoJpaRepository pagamentos;

	public ObrigacaoFinanceiraPersistenceAdapter(ObrigacaoFinanceiraJpaRepository repository,
			PagamentoObrigacaoJpaRepository pagamentos) {
		this.repository = repository;
		this.pagamentos = pagamentos;
	}

	@Override
	public ObrigacaoFinanceira salvar(ObrigacaoFinanceira obrigacao) {
		ObrigacaoFinanceiraJpaEntity entity = toEntity(obrigacao);
		try {
			return toDomain(repository.saveAndFlush(entity));
		} catch (ObjectOptimisticLockingFailureException exception) {
			throw new ConflitoAtualizacaoException();
		}
	}

	@Override
	public Optional<ObrigacaoFinanceira> buscarPorIdEUsuario(Long obrigacaoId, Long usuarioId) {
		return repository.findByIdAndUsuarioId(obrigacaoId, usuarioId).map(this::toDomain);
	}

	@Override
	public Optional<ObrigacaoFinanceira> buscarPorIdEUsuarioParaAtualizacao(Long obrigacaoId, Long usuarioId) {
		return repository.findByIdAndUsuarioIdForUpdate(obrigacaoId, usuarioId).map(this::toDomain);
	}

	@Override
	public Pagina<ObrigacaoFinanceira> listarPorUsuario(Long usuarioId, FiltroListagem filtro, Paginacao paginacao) {
		return PaginaJpaMapper.map(repository.findByUsuarioComFiltro(usuarioId, filtro.status(), filtro.inicio(),
				filtro.fim(), PaginaJpaMapper.pageable(paginacao,
						Sort.by("dataVencimento").ascending().and(Sort.by("id").ascending()))), this::toDomain);
	}

	@Override
	public List<ObrigacaoFinanceira> listarEmAbertoVencidasAte(LocalDate dataReferencia) {
		return repository.findByStatusAndDataVencimentoBefore(StatusObrigacaoFinanceira.EM_ABERTO, dataReferencia)
				.stream().map(this::toDomain).toList();
	}

	@Override
	public List<ObrigacaoFinanceira> listarPendentesPorUsuarioEPeriodo(Long usuarioId, LocalDate inicio, LocalDate fim) {
		return repository.findPendentesByUsuarioIdAndPeriodo(usuarioId,
				List.of(StatusObrigacaoFinanceira.EM_ABERTO, StatusObrigacaoFinanceira.VENCIDA), inicio, fim).stream()
				.map(this::toDomain).toList();
	}

	@Override
	public List<ObrigacaoFinanceira> buscarCandidatosImportacao(Long usuarioId, Long contaId,
			LocalDate inicio, LocalDate fim, BigDecimal valor) {
		return repository.findCandidatosImportacao(usuarioId, contaId, inicio, fim, valor).stream()
				.map(this::toDomain).toList();
	}

	@Override
	public boolean existePorTransacaoEUsuario(Long transacaoId, Long usuarioId) {
		return repository.existsByTransacaoIdAndUsuarioId(transacaoId, usuarioId)
				|| pagamentos.existsByTransacaoIdAndUsuarioId(transacaoId, usuarioId);
	}

	private ObrigacaoFinanceiraJpaEntity toEntity(ObrigacaoFinanceira o) {
		ObrigacaoFinanceiraJpaEntity entity = new ObrigacaoFinanceiraJpaEntity();
		entity.setId(o.getId());
		entity.setUsuarioId(o.getUsuarioId());
		entity.setDescricao(o.getDescricao());
		entity.setCredor(o.getCredor());
		entity.setValor(o.getValor().valor());
		entity.setValorPago(o.getValorPago().valor());
		entity.setDataVencimento(o.getDataVencimento());
		entity.setContaPagamentoId(o.getContaPagamentoId());
		entity.setCategoriaId(o.getCategoriaId());
		entity.setStatus(o.getStatus());
		entity.setDataLiquidacao(o.getDataLiquidacao());
		entity.setTransacaoId(o.getTransacaoId());
		entity.setCanceladaEm(o.getCanceladaEm());
		entity.setVersion(o.getVersion());
		return entity;
	}

	private ObrigacaoFinanceira toDomain(ObrigacaoFinanceiraJpaEntity e) {
		return ObrigacaoFinanceira.reconstituir(e.getId(), e.getUsuarioId(), e.getDescricao(), e.getCredor(),
				ValorMonetario.of(e.getValor()), ValorMonetario.of(e.getValorPago()), e.getDataVencimento(), e.getContaPagamentoId(), e.getCategoriaId(),
				e.getStatus(), e.getDataLiquidacao(), e.getTransacaoId(), e.getCanceladaEm(),
				e.getVersion() == null ? 0 : e.getVersion());
	}
}
