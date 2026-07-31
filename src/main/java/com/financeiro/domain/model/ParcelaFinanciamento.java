package com.financeiro.domain.model;

import com.financeiro.domain.vo.ValorMonetario;

import java.time.LocalDate;

public class ParcelaFinanciamento {

	private final Long id;
	private final Long financiamentoId;
	private final int numero;
	private final ValorMonetario valor;
	private final LocalDate dataVencimento;
	private StatusParcelaFinanciamento status;

	private ParcelaFinanciamento(Long id, Long financiamentoId, int numero, ValorMonetario valor,
			LocalDate dataVencimento, StatusParcelaFinanciamento status) {
		this.id = id;
		this.financiamentoId = financiamentoId;
		this.numero = numero;
		this.valor = valor;
		this.dataVencimento = dataVencimento;
		this.status = status;
	}

	public static ParcelaFinanciamento nova(Long financiamentoId, int numero, ValorMonetario valor,
			LocalDate dataVencimento) {
		return new ParcelaFinanciamento(null, financiamentoId, numero, valor, dataVencimento,
				StatusParcelaFinanciamento.PENDENTE);
	}

	public static ParcelaFinanciamento reconstituir(Long id, Long financiamentoId, int numero, ValorMonetario valor,
			LocalDate dataVencimento, StatusParcelaFinanciamento status) {
		return new ParcelaFinanciamento(id, financiamentoId, numero, valor, dataVencimento, status);
	}

	public void pagar() {
		if (status == StatusParcelaFinanciamento.PAGA)
			throw new com.financeiro.domain.DomainException("error.parcela.transicao.invalida");
		this.status = StatusParcelaFinanciamento.PAGA;
	}

	public void marcarAtrasada(LocalDate dataReferencia) {
		if (status == StatusParcelaFinanciamento.PENDENTE && dataVencimento.isBefore(dataReferencia)) {
			this.status = StatusParcelaFinanciamento.ATRASADA;
		}
	}

	public ParcelaFinanciamento replanejada(int novoNumero, ValorMonetario novoValor, LocalDate novoVencimento) {
		if (status == StatusParcelaFinanciamento.PAGA)
			throw new com.financeiro.domain.DomainException("error.parcela.transicao.invalida");
		return reconstituir(id, financiamentoId, novoNumero, novoValor, novoVencimento,
				StatusParcelaFinanciamento.PENDENTE);
	}

	public Long getId() {
		return id;
	}

	public Long getFinanciamentoId() {
		return financiamentoId;
	}

	public int getNumero() {
		return numero;
	}

	public ValorMonetario getValor() {
		return valor;
	}

	public LocalDate getDataVencimento() {
		return dataVencimento;
	}

	public StatusParcelaFinanciamento getStatus() {
		return status;
	}
}
