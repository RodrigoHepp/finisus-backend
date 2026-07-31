package com.financeiro.domain.model;

import com.financeiro.domain.DomainException;

import java.time.LocalDateTime;

public class DespesaCompartilhada {
	private final Long id;
	private final Long transacaoId;
	private final Long transacaoItemId;
	private final Long criadorId;
	private final TipoRateio tipoRateio;
	private final TipoAlvoCompartilhamento tipoAlvo;
	private StatusDespesaCompartilhada status;
	private LocalDateTime canceladaEm;

	private DespesaCompartilhada(Long id, Long transacaoId, Long transacaoItemId, Long criadorId, TipoRateio tipoRateio,
			TipoAlvoCompartilhamento tipoAlvo, StatusDespesaCompartilhada status, LocalDateTime canceladaEm) {
		if (transacaoId == null || criadorId == null || tipoRateio == null || tipoAlvo == null
				|| (tipoAlvo == TipoAlvoCompartilhamento.TRANSACAO && transacaoItemId != null)
				|| (tipoAlvo == TipoAlvoCompartilhamento.ITEM_TRANSACAO && transacaoItemId == null)) {
			throw new DomainException("error.compartilhamento.alvo.invalido");
		}
		this.id = id;
		this.transacaoId = transacaoId;
		this.transacaoItemId = transacaoItemId;
		this.criadorId = criadorId;
		this.tipoRateio = tipoRateio;
		this.tipoAlvo = tipoAlvo;
		this.status = status == null ? StatusDespesaCompartilhada.ATIVA : status;
		this.canceladaEm = canceladaEm;
	}

	public static DespesaCompartilhada nova(Long transacaoId, Long criadorId, TipoRateio tipoRateio) {
		return new DespesaCompartilhada(null, transacaoId, null, criadorId, tipoRateio,
				TipoAlvoCompartilhamento.TRANSACAO, StatusDespesaCompartilhada.ATIVA, null);
	}

	public static DespesaCompartilhada novoItem(Long transacaoId, Long transacaoItemId, Long criadorId,
			TipoRateio tipoRateio) {
		return new DespesaCompartilhada(null, transacaoId, transacaoItemId, criadorId, tipoRateio,
				TipoAlvoCompartilhamento.ITEM_TRANSACAO, StatusDespesaCompartilhada.ATIVA, null);
	}

	public static DespesaCompartilhada reconstituir(Long id, Long transacaoId, Long transacaoItemId, Long criadorId,
			TipoRateio tipoRateio, TipoAlvoCompartilhamento tipoAlvo, StatusDespesaCompartilhada status,
			LocalDateTime canceladaEm) {
		return new DespesaCompartilhada(id, transacaoId, transacaoItemId, criadorId, tipoRateio, tipoAlvo, status,
				canceladaEm);
	}

	public static DespesaCompartilhada reconstituir(Long id, Long transacaoId, Long transacaoItemId, Long criadorId,
			TipoRateio tipoRateio, TipoAlvoCompartilhamento tipoAlvo) {
		return reconstituir(id, transacaoId, transacaoItemId, criadorId, tipoRateio, tipoAlvo,
				StatusDespesaCompartilhada.ATIVA, null);
	}

	public Long getId() {
		return id;
	}

	public Long getTransacaoId() {
		return transacaoId;
	}

	public Long getTransacaoItemId() {
		return transacaoItemId;
	}

	public Long getCriadorId() {
		return criadorId;
	}

	public TipoRateio getTipoRateio() {
		return tipoRateio;
	}

	public TipoAlvoCompartilhamento getTipoAlvo() {
		return tipoAlvo;
	}

	public StatusDespesaCompartilhada getStatus() {
		return status;
	}

	public LocalDateTime getCanceladaEm() {
		return canceladaEm;
	}

	public boolean isAtiva() {
		return status == StatusDespesaCompartilhada.ATIVA;
	}

	public void cancelar(LocalDateTime momento) {
		if (!isAtiva())
			throw new DomainException("error.compartilhamento.cancelamento.invalido");
		status = StatusDespesaCompartilhada.CANCELADA;
		canceladaEm = momento;
	}
}
