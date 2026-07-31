package com.financeiro.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "transacao_item")
@Getter
@Setter
public class TransacaoItemJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transacao_id", nullable = false)
	private TransacaoJpaEntity transacao;

	@Column(name = "item_id")
	private Long itemId;

	private String descricao;

	private BigDecimal valor;

	@Column(name = "categoria_id")
	private Long categoriaId;
}
