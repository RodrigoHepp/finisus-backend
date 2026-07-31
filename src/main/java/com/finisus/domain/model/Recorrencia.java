package com.finisus.domain.model;

import com.finisus.domain.vo.ValorMonetario;

public class Recorrencia {

	private final Long id;
	private final Long usuarioId;
	private final String nome;
	private final TipoTransacao tipo;
	private final ValorMonetario valorEsperado;
	private final int diaDoMes;
	private final Long categoriaId;
	private final Long contaId;
	private final Long meioPagamentoId;
	private final boolean ativo;

	private Recorrencia(Long id, Long usuarioId, String nome, TipoTransacao tipo, ValorMonetario valorEsperado,
			int diaDoMes, Long categoriaId, Long contaId, Long meioPagamentoId, boolean ativo) {
		this.id = id;
		this.usuarioId = usuarioId;
		this.nome = nome;
		this.tipo = tipo;
		this.valorEsperado = valorEsperado;
		this.diaDoMes = diaDoMes;
		this.categoriaId = categoriaId;
		this.contaId = contaId;
		this.meioPagamentoId = meioPagamentoId;
		this.ativo = ativo;
	}

	public static Recorrencia nova(Long usuarioId, String nome, TipoTransacao tipo, ValorMonetario valorEsperado,
			int diaDoMes, Long categoriaId, Long contaId, Long meioPagamentoId) {
		return new Recorrencia(null, usuarioId, nome, tipo, valorEsperado, diaDoMes, categoriaId, contaId,
				meioPagamentoId, true);
	}

	public static Recorrencia reconstituir(Long id, Long usuarioId, String nome, TipoTransacao tipo,
			ValorMonetario valorEsperado, int diaDoMes, Long categoriaId, Long contaId, Long meioPagamentoId,
			boolean ativo) {
		return new Recorrencia(id, usuarioId, nome, tipo, valorEsperado, diaDoMes, categoriaId, contaId,
				meioPagamentoId, ativo);
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

	public TipoTransacao getTipo() {
		return tipo;
	}

	public ValorMonetario getValorEsperado() {
		return valorEsperado;
	}

	public int getDiaDoMes() {
		return diaDoMes;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public Long getContaId() {
		return contaId;
	}

	public Long getMeioPagamentoId() {
		return meioPagamentoId;
	}

	public boolean isAtivo() {
		return ativo;
	}
}
