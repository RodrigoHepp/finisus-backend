package com.finisus.domain.model;

public class Item {

	private final Long id;
	private final Long usuarioId;
	private final String nome;
	private final Long categoriaPadraoId;
	private final boolean ativo;

	private Item(Long id, Long usuarioId, String nome, Long categoriaPadraoId, boolean ativo) {
		this.id = id;
		this.usuarioId = usuarioId;
		this.nome = nome;
		this.categoriaPadraoId = categoriaPadraoId;
		this.ativo = ativo;
	}

	public static Item novo(Long usuarioId, String nome, Long categoriaPadraoId) {
		return new Item(null, usuarioId, nome, categoriaPadraoId, true);
	}

	public static Item reconstituir(Long id, Long usuarioId, String nome, Long categoriaPadraoId, boolean ativo) {
		return new Item(id, usuarioId, nome, categoriaPadraoId, ativo);
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

	public Long getCategoriaPadraoId() {
		return categoriaPadraoId;
	}

	public boolean isAtivo() {
		return ativo;
	}
}
