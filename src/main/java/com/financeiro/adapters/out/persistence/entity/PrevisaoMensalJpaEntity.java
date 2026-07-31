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
import java.time.LocalDateTime;

@Entity
@Table(name = "previsao_mensal")
@Getter
@Setter
public class PrevisaoMensalJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id")
	private Long usuarioId;

	@Column(name = "ano_mes")
	private String anoMes;

	@Column(name = "categoria_id")
	private Long categoriaId;

	@Column(name = "valor_projetado_entrada")
	private BigDecimal valorProjetadoEntrada;

	@Column(name = "valor_projetado_saida")
	private BigDecimal valorProjetadoSaida;

	@Column(name = "calculado_em")
	private LocalDateTime calculadoEm;
}
