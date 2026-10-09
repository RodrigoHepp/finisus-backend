package com.finisus.domain.model;

import com.finisus.domain.DomainException;
import com.finisus.domain.vo.ValorMonetario;
import java.time.LocalDate;

public class PosicaoInvestimento {
	private final Long id;
	private final Long investimentoId;
	private final ValorMonetario valor;
	private final LocalDate dataReferencia;
	private final long version;

	private PosicaoInvestimento(Long id, Long investimentoId, ValorMonetario valor, LocalDate dataReferencia,
			long version) {
		if (investimentoId == null || investimentoId <= 0 || valor == null || valor.valor().signum() < 0
				|| dataReferencia == null) {
			throw new DomainException("error.posicao.investimento.invalida");
		}
		this.id = id;
		this.investimentoId = investimentoId;
		this.valor = valor;
		this.dataReferencia = dataReferencia;
		this.version = version;
	}

	public static PosicaoInvestimento nova(Long investimentoId, ValorMonetario valor, LocalDate dataReferencia) {
		return new PosicaoInvestimento(null, investimentoId, valor, dataReferencia, 0);
	}

	public static PosicaoInvestimento reconstituir(Long id, Long investimentoId, ValorMonetario valor,
			LocalDate dataReferencia, long version) {
		return new PosicaoInvestimento(id, investimentoId, valor, dataReferencia, version);
	}

	public Long getId() { return id; }
	public Long getInvestimentoId() { return investimentoId; }
	public ValorMonetario getValor() { return valor; }
	public LocalDate getDataReferencia() { return dataReferencia; }
	public long getVersion() { return version; }
}
