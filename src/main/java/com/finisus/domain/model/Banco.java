package com.finisus.domain.model;


public class Banco {

	private final Long id;
	private final String nome;
	private final String codigo;
	private final boolean ativo;
	private final Long usuarioId;

	private Banco(Long id, String nome, String codigo, boolean ativo, Long usuarioId) {
		this.nome = nome;
		this.codigo = codigo;
		this.ativo = ativo;
		this.usuarioId = usuarioId;
		this.id = id;
	}

	public static Banco novoSistema(String nome, String codigo) {
		return new Banco(null, nome, codigo, true, null);
	}

	public static Banco novoUsuario(String nome, String codigo, Long usuarioId) {
		return new Banco(null, nome, codigo, true, usuarioId);
	}

	public static Banco reconstituir(Long id, String nome, String codigo, boolean ativo, Long usuarioId) {
		return new Banco(id, nome, codigo, ativo, usuarioId);
	}

	public boolean isSistema() {
		return usuarioId == null;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getCodigo() {
		return codigo;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}
}
