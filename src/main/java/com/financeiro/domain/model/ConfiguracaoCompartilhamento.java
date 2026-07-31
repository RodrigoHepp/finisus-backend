package com.financeiro.domain.model;

public class ConfiguracaoCompartilhamento {

	private final Long id;
	private final Long usuarioId;
	private boolean aceitaCompartilhamento;

	private ConfiguracaoCompartilhamento(Long id, Long usuarioId, boolean aceitaCompartilhamento) {
		this.id = id;
		this.usuarioId = usuarioId;
		this.aceitaCompartilhamento = aceitaCompartilhamento;
	}

	public static ConfiguracaoCompartilhamento padrao(Long usuarioId) {
		return new ConfiguracaoCompartilhamento(null, usuarioId, false);
	}

	public static ConfiguracaoCompartilhamento reconstituir(Long id, Long usuarioId, boolean aceitaCompartilhamento) {
		return new ConfiguracaoCompartilhamento(id, usuarioId, aceitaCompartilhamento);
	}

	public void atualizarOptIn(boolean aceita) {
		this.aceitaCompartilhamento = aceita;
	}

	public Long getId() {
		return id;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public boolean isAceitaCompartilhamento() {
		return aceitaCompartilhamento;
	}
}
