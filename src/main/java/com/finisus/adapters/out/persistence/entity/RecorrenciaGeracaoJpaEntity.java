package com.finisus.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.finisus.domain.model.StatusOcorrenciaRecorrencia;
import com.finisus.domain.model.TipoTransacao;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recorrencia_geracao")
@Getter
@Setter
public class RecorrenciaGeracaoJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "recorrencia_id")
	private Long recorrenciaId;

	@Column(name = "usuario_id")
	private Long usuarioId;

	@Column(name = "ano_mes")
	private String anoMes;

	private LocalDate vencimento;

	@Enumerated(EnumType.STRING)
	private TipoTransacao tipo;

	private BigDecimal valor;

	private String descricao;

	@Column(name = "conta_id")
	private Long contaId;

	@Column(name = "categoria_id")
	private Long categoriaId;

	@Column(name = "meio_pagamento_id")
	private Long meioPagamentoId;

	@Enumerated(EnumType.STRING)
	private StatusOcorrenciaRecorrencia status;

	@Column(name = "transacao_id")
	private Long transacaoId;
}
