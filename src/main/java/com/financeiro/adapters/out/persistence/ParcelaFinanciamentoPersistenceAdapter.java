package com.financeiro.adapters.out.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import com.financeiro.adapters.out.persistence.entity.FinanciamentoJpaEntity;
import com.financeiro.adapters.out.persistence.entity.ParcelaFinanciamentoJpaEntity;
import com.financeiro.adapters.out.persistence.repository.FinanciamentoJpaRepository;
import com.financeiro.adapters.out.persistence.repository.ParcelaFinanciamentoJpaRepository;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.out.ParcelaFinanciamentoRepositoryPort;
import com.financeiro.domain.ConflitoAtualizacaoException;
import com.financeiro.domain.model.ParcelaFinanciamento;
import com.financeiro.domain.model.StatusParcelaFinanciamento;
import com.financeiro.domain.vo.AnoMes;
import com.financeiro.domain.vo.ValorMonetario;

@Component
public class ParcelaFinanciamentoPersistenceAdapter implements ParcelaFinanciamentoRepositoryPort {
	private final ParcelaFinanciamentoJpaRepository repository;
	private final FinanciamentoJpaRepository financiamentos;

	public ParcelaFinanciamentoPersistenceAdapter(ParcelaFinanciamentoJpaRepository repository,
			FinanciamentoJpaRepository financiamentos) {
		this.repository = repository;
		this.financiamentos = financiamentos;
	}

	@Override
	public void salvarNovas(Long financiamentoId, List<ParcelaFinanciamento> parcelas) {
		FinanciamentoJpaEntity financiamento = financiamentos.findById(financiamentoId).orElseThrow();
		repository.saveAll(parcelas.stream().map(parcela -> novaEntidade(financiamento, parcela)).toList());
	}

	@Override
	public List<ParcelaFinanciamento> listarPorFinanciamento(Long financiamentoId) {
		return repository.findByFinanciamentoIdOrderByNumero(financiamentoId).stream().map(this::toDomain).toList();
	}

	@Override
	public Pagina<ParcelaFinanciamento> listarPorFinanciamento(Long financiamentoId, Paginacao paginacao) {
		return PaginaJpaMapper.map(repository.findByFinanciamentoId(financiamentoId,
				PaginaJpaMapper.pageable(paginacao, Sort.by("numero").ascending().and(Sort.by("id").ascending()))),
				this::toDomain);
	}

	@Override
	public ParcelaFinanciamento salvar(ParcelaFinanciamento parcela) {
		ParcelaFinanciamentoJpaEntity entity = repository.findById(parcela.getId()).orElseThrow();
		entity.setNumero(parcela.getNumero());
		entity.setValor(parcela.getValor().valor());
		entity.setDataVencimento(parcela.getDataVencimento());
		entity.setStatus(parcela.getStatus());
		try {
			return toDomain(repository.saveAndFlush(entity));
		} catch (ObjectOptimisticLockingFailureException exception) {
			throw new ConflitoAtualizacaoException();
		}
	}

	@Override
	public void excluir(Long parcelaId) {
		repository.deleteById(parcelaId);
	}

	@Override
	public int excluirPendentesAPartirDe(Long financiamentoId, int numeroParcela) {
		List<ParcelaFinanciamentoJpaEntity> futuras = repository
				.findByFinanciamentoIdAndNumeroGreaterThanEqualAndStatusNot(financiamentoId, numeroParcela,
						StatusParcelaFinanciamento.PAGA);
		repository.deleteAll(futuras);
		return futuras.size();
	}

	@Override
	public List<ParcelaFinanciamento> listarVencidas(LocalDate dataReferencia) {
		return repository.findByStatusAndDataVencimentoBefore(StatusParcelaFinanciamento.PENDENTE, dataReferencia)
				.stream().map(this::toDomain).toList();
	}

	@Override
	public List<ParcelaFinanciamento> listarPorUsuarioEPeriodo(Long usuarioId, AnoMes inicio, AnoMes fim) {
		return repository.findRelevantByUsuarioIdAndPeriodo(usuarioId, inicio.primeiroDia(),
				fim.proximo().primeiroDia(), StatusParcelaFinanciamento.PAGA).stream().map(this::toDomain).toList();
	}

	@Override
	public boolean existePagaPorFinanciamento(Long financiamentoId) {
		return repository.existsByFinanciamentoIdAndStatus(financiamentoId, StatusParcelaFinanciamento.PAGA);
	}

	private ParcelaFinanciamentoJpaEntity novaEntidade(FinanciamentoJpaEntity financiamento,
			ParcelaFinanciamento parcela) {
		ParcelaFinanciamentoJpaEntity entity = new ParcelaFinanciamentoJpaEntity();
		entity.setFinanciamento(financiamento);
		entity.setNumero(parcela.getNumero());
		entity.setValor(parcela.getValor().valor());
		entity.setDataVencimento(parcela.getDataVencimento());
		entity.setStatus(parcela.getStatus());
		return entity;
	}

	private ParcelaFinanciamento toDomain(ParcelaFinanciamentoJpaEntity entity) {
		return ParcelaFinanciamento.reconstituir(entity.getId(), entity.getFinanciamento().getId(), entity.getNumero(),
				ValorMonetario.of(entity.getValor()), entity.getDataVencimento(), entity.getStatus());
	}
}
