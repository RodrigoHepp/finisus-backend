package com.finisus.domain.model;

import com.finisus.domain.vo.ValorMonetario;
import com.finisus.domain.FinanciamentoInvalidoException;

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
	private final int cronogramaVersao;
	private final Long financiamentoOrigemId;

	private Financiamento(Long id, Long usuarioId, String descricao, ValorMonetario principal,
			BigDecimal taxaJurosMensal, int numeroParcelas, LocalDate dataInicio, Long contaId,
			LocalDateTime finalizadoEm, StatusFinanciamento status, LocalDateTime canceladaEm, int cronogramaVersao,
			Long financiamentoOrigemId) {
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
		if (cronogramaVersao < 1)
			throw new FinanciamentoInvalidoException();
		this.cronogramaVersao = cronogramaVersao;
		this.financiamentoOrigemId = financiamentoOrigemId;
	}

	public static Financiamento novo(Long usuarioId, String descricao, ValorMonetario principal,
			BigDecimal taxaJurosMensal, int numeroParcelas, LocalDate dataInicio, Long contaId) {
		return new Financiamento(null, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio,
				contaId, null, StatusFinanciamento.ATIVO, null, 1, null);
	}

	public static Financiamento novoRefinanciado(Long usuarioId, String descricao, ValorMonetario principal,
			BigDecimal taxaJurosMensal, int numeroParcelas, LocalDate dataInicio, Long contaId,
			Long financiamentoOrigemId) {
		if (financiamentoOrigemId == null || financiamentoOrigemId <= 0)
			throw new FinanciamentoInvalidoException();
		return new Financiamento(null, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio,
				contaId, null, StatusFinanciamento.ATIVO, null, 1, financiamentoOrigemId);
	}

	public static Financiamento reconstituir(Long id, Long usuarioId, String descricao, ValorMonetario principal,
			BigDecimal taxaJurosMensal, int numeroParcelas, LocalDate dataInicio, Long contaId) {
		return new Financiamento(id, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio,
				contaId, null, StatusFinanciamento.ATIVO, null, 1, null);
	}

	public static Financiamento reconstituir(Long id, Long usuarioId, String descricao, ValorMonetario principal,
			BigDecimal taxaJurosMensal, int numeroParcelas, LocalDate dataInicio, Long contaId,
			LocalDateTime finalizadoEm, StatusFinanciamento status, LocalDateTime canceladaEm) {
		return reconstituir(id, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio, contaId,
				finalizadoEm, status, canceladaEm, 1, null);
	}

	public static Financiamento reconstituir(Long id, Long usuarioId, String descricao, ValorMonetario principal,
			BigDecimal taxaJurosMensal, int numeroParcelas, LocalDate dataInicio, Long contaId,
			LocalDateTime finalizadoEm, StatusFinanciamento status, LocalDateTime canceladaEm, int cronogramaVersao) {
		return reconstituir(id, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio, contaId,
				finalizadoEm, status, canceladaEm, cronogramaVersao, null);
	}

	public static Financiamento reconstituir(Long id, Long usuarioId, String descricao, ValorMonetario principal,
			BigDecimal taxaJurosMensal, int numeroParcelas, LocalDate dataInicio, Long contaId,
			LocalDateTime finalizadoEm, StatusFinanciamento status, LocalDateTime canceladaEm, int cronogramaVersao,
			Long financiamentoOrigemId) {
		return new Financiamento(id, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio,
				contaId, finalizadoEm, status, canceladaEm, cronogramaVersao, financiamentoOrigemId);
	}

	public Financiamento finalizar(LocalDateTime dataFinalizacao) {
		if (finalizadoEm != null)
			throw new com.finisus.domain.DomainException("error.financiamento.finalizado");
		return reconstituir(id, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio, contaId,
				dataFinalizacao, StatusFinanciamento.FINALIZADO, null, cronogramaVersao, financiamentoOrigemId);
	}

	public Financiamento comNumeroParcelas(int quantidade) {
		return reconstituir(id, usuarioId, descricao, principal, taxaJurosMensal, quantidade, dataInicio, contaId,
				finalizadoEm, status, canceladaEm, cronogramaVersao, financiamentoOrigemId);
	}

	public Financiamento comNovoCronograma(int quantidade) {
		return reconstituir(id, usuarioId, descricao, principal, taxaJurosMensal, quantidade, dataInicio, contaId,
				finalizadoEm, status, canceladaEm, cronogramaVersao + 1, financiamentoOrigemId);
	}

	public Financiamento cancelar(LocalDateTime momento) {
		if (status != StatusFinanciamento.ATIVO)
			throw new com.finisus.domain.DomainException("error.financiamento.cancelamento.invalido");
		return new Financiamento(id, usuarioId, descricao, principal, taxaJurosMensal, numeroParcelas, dataInicio,
				contaId, finalizadoEm, StatusFinanciamento.CANCELADO, momento, cronogramaVersao,
				financiamentoOrigemId);
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

	public int getCronogramaVersao() { return cronogramaVersao; }
	public Long getFinanciamentoOrigemId() { return financiamentoOrigemId; }
}
