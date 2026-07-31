package com.finisus.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "cartao_credito")
@Getter
@Setter
public class CartaoCreditoJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id")
	private Long usuarioId;

	private String nome;

	private BigDecimal limite;

	@Column(name = "dia_fechamento")
	private Integer diaFechamento;

	@Column(name = "dia_vencimento")
	private Integer diaVencimento;

	private boolean ativo;
}
