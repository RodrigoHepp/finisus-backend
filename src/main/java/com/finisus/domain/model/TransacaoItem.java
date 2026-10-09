package com.finisus.domain.model;

import com.finisus.domain.vo.ValorMonetario;

public class TransacaoItem {

	private final Long id;
	private final Long itemId;
	private final String descricao;
	private final java.math.BigDecimal quantidade;
	private final ValorMonetario valor;
	private final Long categoriaId;

	private TransacaoItem(Long id, Long itemId, String descricao, java.math.BigDecimal quantidade,
			ValorMonetario valor, Long categoriaId) {
		if (descricao == null || descricao.isBlank())
			throw new com.finisus.domain.DomainException("error.transacao.item.descricao.obrigatoria");
		if (quantidade != null && quantidade.signum() <= 0)
			throw new com.finisus.domain.DomainException("error.transacao.item.quantidade.invalida");
		this.id = id;
		this.itemId = itemId;
		this.descricao = descricao.trim();
		this.quantidade = quantidade;
		this.valor = valor;
		this.categoriaId = categoriaId;
	}

	public static TransacaoItem novo(Long itemId, String descricao, ValorMonetario valor, Long categoriaId) {
		return novo(itemId, descricao, null, valor, categoriaId);
	}

	public static TransacaoItem novo(Long itemId, String descricao, java.math.BigDecimal quantidade,
			ValorMonetario valor, Long categoriaId) {
		return new TransacaoItem(null, itemId, descricao, quantidade, valor, categoriaId);
	}

	public static TransacaoItem reconstituir(Long id, Long itemId, String descricao, ValorMonetario valor,
			Long categoriaId) {
		return reconstituir(id, itemId, descricao, null, valor, categoriaId);
	}

	public static TransacaoItem reconstituir(Long id, Long itemId, String descricao, java.math.BigDecimal quantidade,
			ValorMonetario valor, Long categoriaId) {
		return new TransacaoItem(id, itemId, descricao, quantidade, valor, categoriaId);
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

	public java.math.BigDecimal getQuantidade() {
		return quantidade;
	}

	public ValorMonetario getValor() {
		return valor;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}
}
