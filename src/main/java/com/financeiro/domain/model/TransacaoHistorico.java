package com.financeiro.domain.model;

import java.time.LocalDateTime;

public class TransacaoHistorico {

	private final Long id;
	private final Long transacaoId;
	private final String campoAlterado;
	private final String valorAnterior;
	private final String valorNovo;
	private final Long alteradoPor;
	private final LocalDateTime alteradoEm;

	private TransacaoHistorico(Long id, Long transacaoId, String campoAlterado, String valorAnterior, String valorNovo,
			Long alteradoPor, LocalDateTime alteradoEm) {
		this.id = id;
		this.transacaoId = transacaoId;
		this.campoAlterado = campoAlterado;
		this.valorAnterior = valorAnterior;
		this.valorNovo = valorNovo;
		this.alteradoPor = alteradoPor;
		this.alteradoEm = alteradoEm;
	}

	public static TransacaoHistorico registrar(Long transacaoId, String campoAlterado, String valorAnterior,
			String valorNovo, Long alteradoPor, LocalDateTime alteradoEm) {
		return new TransacaoHistorico(null, transacaoId, campoAlterado, valorAnterior, valorNovo, alteradoPor,
				alteradoEm);
	}

	public static TransacaoHistorico reconstituir(Long id, Long transacaoId, String campoAlterado, String valorAnterior,
			String valorNovo, Long alteradoPor, LocalDateTime alteradoEm) {
		return new TransacaoHistorico(id, transacaoId, campoAlterado, valorAnterior, valorNovo, alteradoPor,
				alteradoEm);
	}

	public Long getId() {
		return id;
	}

	public Long getTransacaoId() {
		return transacaoId;
	}

	public String getCampoAlterado() {
		return campoAlterado;
	}

	public String getValorAnterior() {
		return valorAnterior;
	}

	public String getValorNovo() {
		return valorNovo;
	}

	public Long getAlteradoPor() {
		return alteradoPor;
	}

	public LocalDateTime getAlteradoEm() {
		return alteradoEm;
	}
}
