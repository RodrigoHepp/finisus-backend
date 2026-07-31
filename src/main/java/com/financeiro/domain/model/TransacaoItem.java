package com.financeiro.domain.model;

import com.financeiro.domain.vo.ValorMonetario;

public class TransacaoItem {

	private final Long id;
	private final Long itemId;
	private final String descricao;
	private final ValorMonetario valor;
	private final Long categoriaId;

	private TransacaoItem(Long id, Long itemId, String descricao, ValorMonetario valor, Long categoriaId) {
		this.id = id;
		this.itemId = itemId;
		this.descricao = descricao;
		this.valor = valor;
		this.categoriaId = categoriaId;
	}

	public static TransacaoItem novo(Long itemId, String descricao, ValorMonetario valor, Long categoriaId) {
		return new TransacaoItem(null, itemId, descricao, valor, categoriaId);
	}

	public static TransacaoItem reconstituir(Long id, Long itemId, String descricao, ValorMonetario valor,
			Long categoriaId) {
		return new TransacaoItem(id, itemId, descricao, valor, categoriaId);
	}

	public Long getId() {
		return id;
	}

	public Long getItemId() {
		return itemId;
	}

	public String getDescricao() {
		return descricao;
	}

	public ValorMonetario getValor() {
		return valor;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}
}
