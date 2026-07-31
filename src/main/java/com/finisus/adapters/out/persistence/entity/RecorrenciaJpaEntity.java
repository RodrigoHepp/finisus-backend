package com.finisus.adapters.out.persistence.entity;

import com.finisus.domain.model.TipoTransacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "recorrencia")
@Getter
@Setter
public class RecorrenciaJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id")
	private Long usuarioId;

	private String nome;

	@Enumerated(EnumType.STRING)
	private TipoTransacao tipo;

	@Column(name = "valor_esperado")
	private BigDecimal valorEsperado;

	@Column(name = "dia_do_mes")
	private Integer diaDoMes;

	@Column(name = "categoria_id")
	private Long categoriaId;

	@Column(name = "conta_id")
	private Long contaId;

	@Column(name = "meio_pagamento_id")
	private Long meioPagamentoId;

	private boolean ativo;
}
