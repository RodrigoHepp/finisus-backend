package com.finisus.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pagamento_obrigacao")
@Getter
@Setter
public class PagamentoObrigacaoJpaEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@Column(name = "obrigacao_id", nullable = false) private Long obrigacaoId;
	@Column(name = "usuario_id", nullable = false) private Long usuarioId;
	@Column(name = "transacao_id", nullable = false) private Long transacaoId;
	@Column(nullable = false) private BigDecimal valor;
	@Column(nullable = false) private BigDecimal juros;
	@Column(nullable = false) private BigDecimal encargos;
	@Column(nullable = false) private BigDecimal desconto;
	@Column(name = "data_pagamento", nullable = false) private LocalDate dataPagamento;
	@Column(name = "estornado_em") private LocalDateTime estornadoEm;
	@Version private Long version;
}
