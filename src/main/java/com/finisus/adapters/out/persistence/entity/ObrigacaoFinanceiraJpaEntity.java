package com.finisus.adapters.out.persistence.entity;

import com.finisus.domain.model.StatusObrigacaoFinanceira;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "obrigacao_financeira")
@Getter
@Setter
public class ObrigacaoFinanceiraJpaEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "usuario_id", nullable = false) private Long usuarioId;
	@Column(nullable = false) private String descricao;
	@Column(nullable = false) private String credor;
	@Column(nullable = false) private BigDecimal valor;
	@Column(name = "valor_pago", nullable = false) private BigDecimal valorPago;
	@Column(name = "data_vencimento", nullable = false) private LocalDate dataVencimento;
	@Column(name = "conta_pagamento_id", nullable = false) private Long contaPagamentoId;
	@Column(name = "categoria_id") private Long categoriaId;
	@Enumerated(EnumType.STRING) @Column(nullable = false) private StatusObrigacaoFinanceira status;
	@Column(name = "data_liquidacao") private LocalDate dataLiquidacao;
	@Column(name = "transacao_id") private Long transacaoId;
	@Column(name = "cancelada_em") private LocalDateTime canceladaEm;
	@Version private Long version;
}
