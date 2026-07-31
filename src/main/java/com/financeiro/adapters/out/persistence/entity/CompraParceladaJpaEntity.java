package com.financeiro.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "compra_parcelada")
@Getter
@Setter
public class CompraParceladaJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id")
	private Long usuarioId;

	private String descricao;

	@Column(name = "valor_total")
	private BigDecimal valorTotal;

	@Column(name = "numero_parcelas")
	private Integer numeroParcelas;

	@Column(name = "data_compra")
	private LocalDate dataCompra;

	@Column(name = "categoria_id")
	private Long categoriaId;

	@Column(name = "conta_id")
	private Long contaId;

	@Column(name = "cancelada_em")
	private LocalDateTime canceladaEm;
}
