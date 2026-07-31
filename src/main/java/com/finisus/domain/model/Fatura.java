package com.finisus.domain.model;

import com.finisus.domain.vo.AnoMes;

import java.time.LocalDate;

public class Fatura {

	private final Long id;
	private final Long cartaoId;
	private final AnoMes mesReferencia;
	private final LocalDate dataFechamento;
	private final LocalDate dataVencimento;
	private StatusFatura status;
	private final Long contaPagamentoId;
	private final long version;
	private final LocalDate canceladaEm;

	private Fatura(Long id, Long cartaoId, AnoMes mesReferencia, LocalDate dataFechamento, LocalDate dataVencimento,
			StatusFatura status, Long contaPagamentoId, long version, LocalDate canceladaEm) {
		this.id = id;
		this.cartaoId = cartaoId;
		this.mesReferencia = mesReferencia;
		this.dataFechamento = dataFechamento;
		this.dataVencimento = dataVencimento;
		this.status = status;
		this.contaPagamentoId = contaPagamentoId;
		this.version = version;
		this.canceladaEm = canceladaEm;
	}

	public static Fatura nova(Long cartaoId, AnoMes mesReferencia, LocalDate dataFechamento, LocalDate dataVencimento,
			Long contaPagamentoId) {
		return new Fatura(null, cartaoId, mesReferencia, dataFechamento, dataVencimento, StatusFatura.ABERTA,
				contaPagamentoId, 0, null);
	}

	public static Fatura reconstituir(Long id, Long cartaoId, AnoMes mesReferencia, LocalDate dataFechamento,
			LocalDate dataVencimento, StatusFatura status, Long contaPagamentoId, long version, LocalDate canceladaEm) {
		return new Fatura(id, cartaoId, mesReferencia, dataFechamento, dataVencimento, status, contaPagamentoId,
				version, canceladaEm);
	}

	public void fechar() {
		if (status != StatusFatura.ABERTA) {
			throw new com.finisus.domain.DomainException("error.fatura.transicao.invalida");
		}
		this.status = StatusFatura.FECHADA;
	}

	public void pagar() {
		if (status != StatusFatura.FECHADA) {
			throw new com.finisus.domain.DomainException("error.fatura.transicao.invalida");
		}
		this.status = StatusFatura.PAGA;
	}

	public static Fatura reconstituir(Long id, Long cartaoId, AnoMes mesReferencia, LocalDate dataFechamento,
			LocalDate dataVencimento, StatusFatura status, Long contaPagamentoId, long version) {
		return reconstituir(id, cartaoId, mesReferencia, dataFechamento, dataVencimento, status, contaPagamentoId,
				version, null);
	}

	public Fatura atualizar(LocalDate novoFechamento, LocalDate novoVencimento, Long novaContaPagamentoId) {
		if (status != StatusFatura.ABERTA)
			throw new com.finisus.domain.DomainException("error.fatura.cancelamento.invalido");
		return new Fatura(id, cartaoId, mesReferencia, novoFechamento, novoVencimento, status, novaContaPagamentoId,
				version, null);
	}

	public Fatura cancelar(LocalDate dataCancelamento) {
		if (status != StatusFatura.ABERTA)
			throw new com.finisus.domain.DomainException("error.fatura.cancelamento.invalido");
		return new Fatura(id, cartaoId, mesReferencia, dataFechamento, dataVencimento, StatusFatura.CANCELADA,
				contaPagamentoId, version, dataCancelamento);
	}

	public Long getId() {
		return id;
	}

	public Long getCartaoId() {
		return cartaoId;
	}

	public AnoMes getMesReferencia() {
		return mesReferencia;
	}

	public LocalDate getDataFechamento() {
		return dataFechamento;
	}

	public LocalDate getDataVencimento() {
		return dataVencimento;
	}

	public StatusFatura getStatus() {
		return status;
	}

	public Long getContaPagamentoId() {
		return contaPagamentoId;
	}

	public long getVersion() {
		return version;
	}

	public LocalDate getCanceladaEm() {
		return canceladaEm;
	}
}
