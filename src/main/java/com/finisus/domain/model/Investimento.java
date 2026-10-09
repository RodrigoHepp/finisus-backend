package com.finisus.domain.model;

public class Investimento {

	private final Long id;
	private final Long usuarioId;
	private final String nome;
	private final TipoInvestimento tipo;
	private final Long contaOrigemId;
	private final Long contaCustodiaId;
	private final boolean ativo;

	private Investimento(Long id, Long usuarioId, String nome, TipoInvestimento tipo, Long contaOrigemId,
			Long contaCustodiaId, boolean ativo) {
		this.id = id;
		this.usuarioId = usuarioId;
		this.nome = nome;
		this.tipo = tipo;
		this.contaOrigemId = contaOrigemId;
		this.contaCustodiaId = contaCustodiaId;
		this.ativo = ativo;
	}

	public static Investimento novo(Long usuarioId, String nome, TipoInvestimento tipo, Long contaOrigemId) {
		return novo(usuarioId, nome, tipo, contaOrigemId, null);
	}

	public static Investimento novo(Long usuarioId, String nome, TipoInvestimento tipo, Long contaOrigemId,
			Long contaCustodiaId) {
		return new Investimento(null, usuarioId, nome, tipo, contaOrigemId, contaCustodiaId, true);
	}

	public static Investimento reconstituir(Long id, Long usuarioId, String nome, TipoInvestimento tipo,
			Long contaOrigemId) {
		return reconstituir(id, usuarioId, nome, tipo, contaOrigemId, true);
	}

	public static Investimento reconstituir(Long id, Long usuarioId, String nome, TipoInvestimento tipo,
			Long contaOrigemId, boolean ativo) {
		return reconstituir(id, usuarioId, nome, tipo, contaOrigemId, null, ativo);
	}

	public static Investimento reconstituir(Long id, Long usuarioId, String nome, TipoInvestimento tipo,
			Long contaOrigemId, Long contaCustodiaId, boolean ativo) {
		return new Investimento(id, usuarioId, nome, tipo, contaOrigemId, contaCustodiaId, ativo);
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

	public TipoInvestimento getTipo() {
		return tipo;
	}

	public Long getContaOrigemId() {
		return contaOrigemId;
	}

	public Long getContaCustodiaId() {
		return contaCustodiaId;
	}

	public boolean isAtivo() {
		return ativo;
	}
}
