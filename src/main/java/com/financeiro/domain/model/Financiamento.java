package com.financeiro.domain.model;

import com.financeiro.domain.vo.ValorMonetario;
import com.financeiro.domain.FinanciamentoInvalidoException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Financiamento {

	private final Long id;
	private final Long usuarioId;
	private final String descricao;
	private final ValorMonetario principal;
	private final BigDecimal taxaJurosMensal;
	private final int numeroParcelas;
	private final LocalDate dataInicio;
	private final Long contaId;
	private final LocalDateTime finalizadoEm;
	private final StatusFinanciamento status;
	private final LocalDateTime canceladaEm;

	private Financiamento(Long id, Long usuarioId, String descricao, ValorMonetario principal,
			BigDecimal taxaJurosMensal, int numeroParcelas, LocalDate dataInicio, Long contaId,
			LocalDateTime finalizadoEm, StatusFinanciamento status, LocalDateTime canceladaEm) {
		validar(usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio, contaId);
		this.id = id;
		this.usuarioId = usuarioId;
		this.descricao = descricao;
		this.principal = principal;
		this.taxaJurosMensal = taxaJurosMensal;
		this.numeroParcelas = numeroParcelas;
		this.dataInicio = dataInicio;
		this.contaId = contaId;
		this.finalizadoEm = finalizadoEm;
		this.status = status == null
				? (finalizadoEm == null ? StatusFinanciamento.ATIVO : StatusFinanciamento.FINALIZADO)
				: status;
		this.canceladaEm = canceladaEm;
	}

	public static Financiamento novo(Long usuarioId, String descricao, ValorMonetario principal,
			BigDecimal taxaJurosMensal, int numeroParcelas, LocalDate dataInicio, Long contaId) {
		return new Financiamento(null, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio,
				contaId, null, StatusFinanciamento.ATIVO, null);
	}

	public static Financiamento reconstituir(Long id, Long usuarioId, String descricao, ValorMonetario principal,
			BigDecimal taxaJurosMensal, int numeroParcelas, LocalDate dataInicio, Long contaId) {
		return new Financiamento(id, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio,
				contaId, null, StatusFinanciamento.ATIVO, null);
	}

	public static Financiamento reconstituir(Long id, Long usuarioId, String descricao, ValorMonetario principal,
			BigDecimal taxaJurosMensal, int numeroParcelas, LocalDate dataInicio, Long contaId,
			LocalDateTime finalizadoEm, StatusFinanciamento status, LocalDateTime canceladaEm) {
		return new Financiamento(id, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio,
				contaId, finalizadoEm, status, canceladaEm);
	}

	public Financiamento finalizar(LocalDateTime dataFinalizacao) {
		if (finalizadoEm != null)
			throw new com.financeiro.domain.DomainException("error.financiamento.finalizado");
		return reconstituir(id, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio, contaId,
				dataFinalizacao, StatusFinanciamento.FINALIZADO, null);
	}

	public Financiamento comNumeroParcelas(int quantidade) {
		return reconstituir(id, usuarioId, descricao, principal, taxaJurosMensal, quantidade, dataInicio, contaId,
				finalizadoEm, status, canceladaEm);
	}

	public Financiamento cancelar(LocalDateTime momento) {
		if (status != StatusFinanciamento.ATIVO)
			throw new com.financeiro.domain.DomainException("error.financiamento.cancelamento.invalido");
		return new Financiamento(id, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio,
				contaId, finalizadoEm, StatusFinanciamento.CANCELADO, momento);
	}

	private static void validar(Long usuarioId, String descricao, ValorMonetario principal, BigDecimal taxaJurosMensal,
			int numeroParcelas, LocalDate dataInicio, Long contaId) {
		if (usuarioId == null || usuarioId <= 0 || descricao == null || descricao.isBlank() || principal == null
				|| principal.isZero() || taxaJurosMensal == null || taxaJurosMensal.signum() < 0 || numeroParcelas <= 0
				|| dataInicio == null || contaId == null || contaId <= 0) {
			throw new FinanciamentoInvalidoException();
		}
	}

	public Long getId() {
		return id;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public String getDescricao() {
		return descricao;
	}

	public ValorMonetario getPrincipal() {
		return principal;
	}

	public BigDecimal getTaxaJurosMensal() {
		return taxaJurosMensal;
	}

	public int getNumeroParcelas() {
		return numeroParcelas;
	}

	public LocalDate getDataInicio() {
		return dataInicio;
	}

	public Long getContaId() {
		return contaId;
	}

	public LocalDateTime getFinalizadoEm() {
		return finalizadoEm;
	}

	public boolean isFinalizado() {
		return status == StatusFinanciamento.FINALIZADO;
	}

	public StatusFinanciamento getStatus() {
		return status;
	}

	public LocalDateTime getCanceladaEm() {
		return canceladaEm;
	}
}
