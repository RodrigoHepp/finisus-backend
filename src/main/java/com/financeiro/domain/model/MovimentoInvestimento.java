package com.financeiro.domain.model;

import com.financeiro.domain.vo.ValorMonetario;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MovimentoInvestimento {

	private final Long id;
	private final Long investimentoId;
	private final TipoMovimentoInvestimento tipo;
	private final ValorMonetario valor;
	private final LocalDate data;
	private final Long transacaoId;
	private final Long movimentoOrigemId;
	private final LocalDateTime estornadoEm;

	private MovimentoInvestimento(Long id, Long investimentoId, TipoMovimentoInvestimento tipo, ValorMonetario valor,
			LocalDate data, Long transacaoId, Long movimentoOrigemId, LocalDateTime estornadoEm) {
		this.id = id;
		this.investimentoId = investimentoId;
		this.tipo = tipo;
		this.valor = valor;
		this.data = data;
		this.transacaoId = transacaoId;
		this.movimentoOrigemId = movimentoOrigemId;
		this.estornadoEm = estornadoEm;
	}

	public static MovimentoInvestimento novo(Long investimentoId, TipoMovimentoInvestimento tipo, ValorMonetario valor,
			LocalDate data) {
		return new MovimentoInvestimento(null, investimentoId, tipo, valor, data, null, null, null);
	}

	public static MovimentoInvestimento reconstituir(Long id, Long investimentoId, TipoMovimentoInvestimento tipo,
			ValorMonetario valor, LocalDate data, Long transacaoId, Long movimentoOrigemId, LocalDateTime estornadoEm) {
		return new MovimentoInvestimento(id, investimentoId, tipo, valor, data, transacaoId, movimentoOrigemId,
				estornadoEm);
	}

	public MovimentoInvestimento marcarEstornado(LocalDateTime momento) {
		if (estornadoEm != null || movimentoOrigemId != null)
			throw new com.financeiro.domain.DomainException("error.movimento.investimento.ja.estornado");
		return new MovimentoInvestimento(id, investimentoId, tipo, valor, data, transacaoId, null, momento);
	}

	public MovimentoInvestimento compensar(Long transacaoCompensacaoId, LocalDateTime momento) {
		if (id == null)
			throw new com.financeiro.domain.DomainException("error.movimento.investimento.ja.estornado");
		TipoMovimentoInvestimento oposto = tipo == TipoMovimentoInvestimento.APORTE ? TipoMovimentoInvestimento.RESGATE
				: TipoMovimentoInvestimento.APORTE;
		return new MovimentoInvestimento(null, investimentoId, oposto, valor, data, transacaoCompensacaoId, id,
				momento);
	}

	public Long getId() {
		return id;
	}

	public Long getInvestimentoId() {
		return investimentoId;
	}

	public TipoMovimentoInvestimento getTipo() {
		return tipo;
	}

	public ValorMonetario getValor() {
		return valor;
	}

	public LocalDate getData() {
		return data;
	}

	public Long getTransacaoId() {
		return transacaoId;
	}

	public Long getMovimentoOrigemId() {
		return movimentoOrigemId;
	}

	public LocalDateTime getEstornadoEm() {
		return estornadoEm;
	}
}
