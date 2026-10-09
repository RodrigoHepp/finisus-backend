package com.finisus.adapters.out.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

import com.finisus.adapters.out.persistence.entity.FinanciamentoJpaEntity;
import com.finisus.adapters.out.persistence.entity.ParcelaFinanciamentoJpaEntity;
import com.finisus.adapters.out.persistence.repository.FinanciamentoJpaRepository;
import com.finisus.adapters.out.persistence.repository.ParcelaFinanciamentoJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.ParcelaFinanciamentoRepositoryPort;
import com.finisus.domain.ConflitoAtualizacaoException;
import com.finisus.domain.model.ParcelaFinanciamento;
import com.finisus.domain.model.StatusParcelaFinanciamento;
import com.finisus.domain.vo.AnoMes;
import com.finisus.domain.vo.ValorMonetario;

@Component
public class ParcelaFinanciamentoPersistenceAdapter implements ParcelaFinanciamentoRepositoryPort {
	private final ParcelaFinanciamentoJpaRepository repository;
	private final FinanciamentoJpaRepository financiamentos;
	private final EntityManager entityManager;

	public ParcelaFinanciamentoPersistenceAdapter(ParcelaFinanciamentoJpaRepository repository,
			FinanciamentoJpaRepository financiamentos, EntityManager entityManager) {
		this.repository = repository;
		this.financiamentos = financiamentos;
		this.entityManager = entityManager;
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
		entity.setPrincipal(toBigDecimal(parcela.getPrincipal()));
		entity.setJuros(toBigDecimal(parcela.getJuros()));
		entity.setEncargos(toBigDecimal(parcela.getEncargos()));
		entity.setSaldoDevedorInicial(toBigDecimal(parcela.getSaldoDevedorInicial()));
		entity.setSaldoDevedorFinal(toBigDecimal(parcela.getSaldoDevedorFinal()));
		entity.setDataVencimento(parcela.getDataVencimento());
		entity.setStatus(parcela.getStatus());
		entity.setTransacaoId(parcela.getTransacaoId());
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
	public List<ParcelaFinanciamento> listarPendentesPorUsuario(Long usuarioId) {
		return repository.findPendentesByUsuarioId(usuarioId, StatusParcelaFinanciamento.PAGA).stream()
				.map(this::toDomain).toList();
	}

	@Override
	public boolean existePagaPorFinanciamento(Long financiamentoId) {
		return repository.existsByFinanciamentoIdAndStatus(financiamentoId, StatusParcelaFinanciamento.PAGA);
	}

	@Override
	public boolean existePorTransacao(Long transacaoId) {
		return repository.existsByTransacaoId(transacaoId);
	}

	@Override
	public void registrarSnapshotCronograma(Long financiamentoId, int cronogramaVersao) {
		entityManager.createNativeQuery("""
				insert into parcela_financiamento_historico
				(financiamento_id, cronograma_versao, parcela_id_origem, numero, valor, valor_principal, juros,
				 encargos, saldo_devedor_inicial, saldo_devedor_final, data_vencimento, status, transacao_id)
				select financiamento_id, :versao, id, numero, valor, valor_principal, juros, encargos,
				       saldo_devedor_inicial, saldo_devedor_final, data_vencimento, status, transacao_id
				from parcela_financiamento where financiamento_id = :financiamentoId
				""").setParameter("versao", cronogramaVersao).setParameter("financiamentoId", financiamentoId)
				.executeUpdate();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ParcelaHistorica> listarCronogramaHistorico(Long financiamentoId, int cronogramaVersao) {
		List<Object[]> linhas = entityManager.createNativeQuery("""
				select parcela_id_origem, numero, valor, valor_principal, juros, encargos,
				       saldo_devedor_inicial, saldo_devedor_final, data_vencimento, status, transacao_id
				from parcela_financiamento_historico
				where financiamento_id = :financiamentoId and cronograma_versao = :versao
				order by numero
				""").setParameter("financiamentoId", financiamentoId).setParameter("versao", cronogramaVersao)
				.getResultList();
		return linhas.stream().map(this::toHistorico).toList();
	}

	private ParcelaHistorica toHistorico(Object[] linha) {
		return new ParcelaHistorica(((Number) linha[0]).longValue(), ((Number) linha[1]).intValue(),
				(java.math.BigDecimal) linha[2], (java.math.BigDecimal) linha[3], (java.math.BigDecimal) linha[4],
				(java.math.BigDecimal) linha[5], (java.math.BigDecimal) linha[6], (java.math.BigDecimal) linha[7],
				toLocalDate(linha[8]), StatusParcelaFinanciamento.valueOf(linha[9].toString()),
				linha[10] == null ? null : ((Number) linha[10]).longValue());
	}

	private static LocalDate toLocalDate(Object valor) {
		return valor instanceof LocalDate data ? data : ((java.sql.Date) valor).toLocalDate();
	}

	private ParcelaFinanciamentoJpaEntity novaEntidade(FinanciamentoJpaEntity financiamento,
			ParcelaFinanciamento parcela) {
		ParcelaFinanciamentoJpaEntity entity = new ParcelaFinanciamentoJpaEntity();
		entity.setFinanciamento(financiamento);
		entity.setNumero(parcela.getNumero());
		entity.setValor(parcela.getValor().valor());
		entity.setPrincipal(toBigDecimal(parcela.getPrincipal()));
		entity.setJuros(toBigDecimal(parcela.getJuros()));
		entity.setEncargos(toBigDecimal(parcela.getEncargos()));
		entity.setSaldoDevedorInicial(toBigDecimal(parcela.getSaldoDevedorInicial()));
		entity.setSaldoDevedorFinal(toBigDecimal(parcela.getSaldoDevedorFinal()));
		entity.setDataVencimento(parcela.getDataVencimento());
		entity.setStatus(parcela.getStatus());
		entity.setTransacaoId(parcela.getTransacaoId());
		return entity;
	}

	private ParcelaFinanciamento toDomain(ParcelaFinanciamentoJpaEntity entity) {
		return ParcelaFinanciamento.reconstituir(entity.getId(), entity.getFinanciamento().getId(), entity.getNumero(),
				ValorMonetario.of(entity.getValor()), toValorMonetario(entity.getPrincipal()),
				toValorMonetario(entity.getJuros()), toValorMonetario(entity.getEncargos()),
				toValorMonetario(entity.getSaldoDevedorInicial()), toValorMonetario(entity.getSaldoDevedorFinal()),
				entity.getDataVencimento(), entity.getStatus(), entity.getTransacaoId());
	}

	private static java.math.BigDecimal toBigDecimal(ValorMonetario valor) {
		return valor == null ? null : valor.valor();
	}

	private static ValorMonetario toValorMonetario(java.math.BigDecimal valor) {
		return valor == null ? null : ValorMonetario.of(valor);
	}
}
