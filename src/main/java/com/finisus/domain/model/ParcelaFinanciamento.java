package com.finisus.domain.model;

import com.finisus.domain.DomainException;
import com.finisus.domain.vo.ValorMonetario;

import java.time.LocalDate;

public class ParcelaFinanciamento {
	private final Long id;
	private final Long financiamentoId;
	private final int numero;
	private final ValorMonetario valor;
	private final ValorMonetario principal;
	private final ValorMonetario juros;
	private final ValorMonetario encargos;
	private final ValorMonetario saldoDevedorInicial;
	private final ValorMonetario saldoDevedorFinal;
	private final LocalDate dataVencimento;
	private StatusParcelaFinanciamento status;
	private final Long transacaoId;

	private ParcelaFinanciamento(Long id, Long financiamentoId, int numero, ValorMonetario valor,
			ValorMonetario principal, ValorMonetario juros, ValorMonetario encargos,
			ValorMonetario saldoDevedorInicial, ValorMonetario saldoDevedorFinal, LocalDate dataVencimento,
			StatusParcelaFinanciamento status, Long transacaoId) {
		validarComposicao(valor, principal, juros, encargos, saldoDevedorInicial, saldoDevedorFinal);
		this.id = id;
		this.financiamentoId = financiamentoId;
		this.numero = numero;
		this.valor = valor;
		this.principal = principal;
		this.juros = juros;
		this.encargos = encargos;
		this.saldoDevedorInicial = saldoDevedorInicial;
		this.saldoDevedorFinal = saldoDevedorFinal;
		this.dataVencimento = dataVencimento;
		this.status = status;
		this.transacaoId = transacaoId;
	}

	public static ParcelaFinanciamento nova(Long financiamentoId, int numero, ValorMonetario valor,
			LocalDate dataVencimento) {
		return new ParcelaFinanciamento(null, financiamentoId, numero, valor, null, null, null, null, null,
				dataVencimento, StatusParcelaFinanciamento.PENDENTE, null);
	}

	public static ParcelaFinanciamento nova(Long financiamentoId, int numero, ValorMonetario valor,
			ValorMonetario principal, ValorMonetario juros, ValorMonetario encargos,
			ValorMonetario saldoDevedorInicial, ValorMonetario saldoDevedorFinal, LocalDate dataVencimento) {
		return new ParcelaFinanciamento(null, financiamentoId, numero, valor, principal, juros, encargos,
				saldoDevedorInicial, saldoDevedorFinal, dataVencimento, StatusParcelaFinanciamento.PENDENTE, null);
	}

	public static ParcelaFinanciamento reconstituir(Long id, Long financiamentoId, int numero, ValorMonetario valor,
			LocalDate dataVencimento, StatusParcelaFinanciamento status) {
		return reconstituir(id, financiamentoId, numero, valor, dataVencimento, status, null);
	}

	public static ParcelaFinanciamento reconstituir(Long id, Long financiamentoId, int numero, ValorMonetario valor,
			LocalDate dataVencimento, StatusParcelaFinanciamento status, Long transacaoId) {
		return new ParcelaFinanciamento(id, financiamentoId, numero, valor, null, null, null, null, null,
				dataVencimento, status, transacaoId);
	}

	public static ParcelaFinanciamento reconstituir(Long id, Long financiamentoId, int numero, ValorMonetario valor,
			ValorMonetario principal, ValorMonetario juros, ValorMonetario encargos,
			ValorMonetario saldoDevedorInicial, ValorMonetario saldoDevedorFinal, LocalDate dataVencimento,
			StatusParcelaFinanciamento status, Long transacaoId) {
		return new ParcelaFinanciamento(id, financiamentoId, numero, valor, principal, juros, encargos,
				saldoDevedorInicial, saldoDevedorFinal, dataVencimento, status, transacaoId);
	}

	public void pagar(Long transacaoId) {
		if (status == StatusParcelaFinanciamento.PAGA)
			throw new DomainException("error.parcela.transicao.invalida");
		if (transacaoId == null || transacaoId <= 0)
			throw new DomainException("error.parcela.pagamento.invalido");
		this.status = StatusParcelaFinanciamento.PAGA;
	}

	public ParcelaFinanciamento pagaCom(Long transacaoId) {
		pagar(transacaoId);
		return reconstituir(id, financiamentoId, numero, valor, principal, juros, encargos, saldoDevedorInicial,
				saldoDevedorFinal, dataVencimento, status, transacaoId);
	}

	public ParcelaFinanciamento estornarPagamento(LocalDate referencia) {
		if (status != StatusParcelaFinanciamento.PAGA || transacaoId == null || referencia == null)
			throw new DomainException("error.parcela.estorno.pagamento.invalido");
		StatusParcelaFinanciamento novoStatus = dataVencimento.isBefore(referencia)
				? StatusParcelaFinanciamento.ATRASADA : StatusParcelaFinanciamento.PENDENTE;
		return reconstituir(id, financiamentoId, numero, valor, principal, juros, encargos, saldoDevedorInicial,
				saldoDevedorFinal, dataVencimento, novoStatus, null);
	}

	public void marcarAtrasada(LocalDate dataReferencia) {
		if (status == StatusParcelaFinanciamento.PENDENTE && dataVencimento.isBefore(dataReferencia))
			this.status = StatusParcelaFinanciamento.ATRASADA;
	}

	public ParcelaFinanciamento replanejada(int novoNumero, ValorMonetario novoValor, LocalDate novoVencimento) {
		if (status == StatusParcelaFinanciamento.PAGA)
			throw new DomainException("error.parcela.transicao.invalida");
		return reconstituir(id, financiamentoId, novoNumero, novoValor, novoVencimento,
				StatusParcelaFinanciamento.PENDENTE, null);
	}

	public ParcelaFinanciamento replanejada(int novoNumero, ValorMonetario novoValor, ValorMonetario novoPrincipal,
			ValorMonetario novosJuros, ValorMonetario novosEncargos, ValorMonetario novoSaldoInicial,
			ValorMonetario novoSaldoFinal, LocalDate novoVencimento) {
		if (status == StatusParcelaFinanciamento.PAGA)
			throw new DomainException("error.parcela.transicao.invalida");
		return reconstituir(id, financiamentoId, novoNumero, novoValor, novoPrincipal, novosJuros, novosEncargos,
				novoSaldoInicial, novoSaldoFinal, novoVencimento, StatusParcelaFinanciamento.PENDENTE, null);
	}

	private static void validarComposicao(ValorMonetario valor, ValorMonetario principal, ValorMonetario juros,
			ValorMonetario encargos, ValorMonetario saldoInicial, ValorMonetario saldoFinal) {
		boolean legado = principal == null && juros == null && encargos == null && saldoInicial == null
				&& saldoFinal == null;
		if (legado)
			return;
		if (principal == null || juros == null || encargos == null || saldoInicial == null || saldoFinal == null
				|| valor.valor().compareTo(principal.valor().add(juros.valor()).add(encargos.valor())) != 0
				|| saldoInicial.valor().subtract(principal.valor()).compareTo(saldoFinal.valor()) != 0)
			throw new DomainException("error.parcela.composicao.invalida");
	}

	public Long getId() { return id; }
	public Long getFinanciamentoId() { return financiamentoId; }
	public int getNumero() { return numero; }
	public ValorMonetario getValor() { return valor; }
	public ValorMonetario getPrincipal() { return principal; }
	public ValorMonetario getJuros() { return juros; }
	public ValorMonetario getEncargos() { return encargos; }
	public ValorMonetario getSaldoDevedorInicial() { return saldoDevedorInicial; }
	public ValorMonetario getSaldoDevedorFinal() { return saldoDevedorFinal; }
	public LocalDate getDataVencimento() { return dataVencimento; }
	public StatusParcelaFinanciamento getStatus() { return status; }
	public Long getTransacaoId() { return transacaoId; }
}
