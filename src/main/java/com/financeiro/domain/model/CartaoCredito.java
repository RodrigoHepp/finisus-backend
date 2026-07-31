package com.financeiro.domain.model;

import com.financeiro.domain.vo.ValorMonetario;

public class CartaoCredito {

	private final Long id;
	private final Long usuarioId;
	private final String nome;
	private final ValorMonetario limite;
	private final int diaFechamento;
	private final int diaVencimento;
	private final boolean ativo;

	private CartaoCredito(Long id, Long usuarioId, String nome, ValorMonetario limite, int diaFechamento,
			int diaVencimento, boolean ativo) {
		this.id = id;
		this.usuarioId = usuarioId;
		this.nome = nome;
		this.limite = limite;
		this.diaFechamento = diaFechamento;
		this.diaVencimento = diaVencimento;
		this.ativo = ativo;
	}

	public static CartaoCredito novo(Long usuarioId, String nome, ValorMonetario limite, int diaFechamento,
			int diaVencimento) {
		return new CartaoCredito(null, usuarioId, nome, limite, diaFechamento, diaVencimento, true);
	}

	public static CartaoCredito reconstituir(Long id, Long usuarioId, String nome, ValorMonetario limite,
			int diaFechamento, int diaVencimento) {
		return reconstituir(id, usuarioId, nome, limite, diaFechamento, diaVencimento, true);
	}

	public static CartaoCredito reconstituir(Long id, Long usuarioId, String nome, ValorMonetario limite,
			int diaFechamento, int diaVencimento, boolean ativo) {
		return new CartaoCredito(id, usuarioId, nome, limite, diaFechamento, diaVencimento, ativo);
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

	public ValorMonetario getLimite() {
		return limite;
	}

	public int getDiaFechamento() {
		return diaFechamento;
	}

	public int getDiaVencimento() {
		return diaVencimento;
	}

	public boolean isAtivo() {
		return ativo;
	}
}
