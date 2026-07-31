package com.financeiro.domain.model;

public class MeioPagamento {

	private final Long id;
	private final Long usuarioId;
	private final String nome;
	private final boolean ativo;

	private MeioPagamento(Long id, Long usuarioId, String nome, boolean ativo) {
		this.id = id;
		this.usuarioId = usuarioId;
		this.nome = nome;
		this.ativo = ativo;
	}

	public static MeioPagamento novo(Long usuarioId, String nome) {
		return new MeioPagamento(null, usuarioId, nome, true);
	}

	public static MeioPagamento reconstituir(Long id, Long usuarioId, String nome, boolean ativo) {
		return new MeioPagamento(id, usuarioId, nome, ativo);
	}

	public Long getId() {
		return id;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public String getNome() {
		return nome;
	}

	public boolean isAtivo() {
		return ativo;
	}
}
