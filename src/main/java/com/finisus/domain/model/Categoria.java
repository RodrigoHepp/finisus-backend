package com.finisus.domain.model;

public class Categoria {

	private final Long id;
	private final Long usuarioId;
	private final String nome;
	private final Long categoriaPaiId;
	private final boolean ativo;

	private Categoria(Long id, Long usuarioId, String nome, Long categoriaPaiId, boolean ativo) {
		this.id = id;
		this.usuarioId = usuarioId;
		this.nome = nome;
		this.categoriaPaiId = categoriaPaiId;
		this.ativo = ativo;
	}

	public static Categoria nova(Long usuarioId, String nome, Long categoriaPaiId) {
		return new Categoria(null, usuarioId, nome, categoriaPaiId, true);
	}

	public static Categoria reconstituir(Long id, Long usuarioId, String nome, Long categoriaPaiId, boolean ativo) {
		return new Categoria(id, usuarioId, nome, categoriaPaiId, ativo);
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

	public Long getCategoriaPaiId() {
		return categoriaPaiId;
	}

	public boolean isAtivo() {
		return ativo;
	}
}
