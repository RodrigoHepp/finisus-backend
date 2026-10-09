package com.finisus.adapters.out.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pagamento_fatura")
@Getter
@Setter
public class PagamentoFaturaJpaEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@Column(name = "fatura_id", nullable = false) private Long faturaId;
	@Column(name = "usuario_id", nullable = false) private Long usuarioId;
	@Column(name = "transacao_id", nullable = false) private Long transacaoId;
	@Column(name = "conta_id", nullable = false) private Long contaId;
	@Column(nullable = false) private BigDecimal valor;
	@Column(nullable = false) private BigDecimal credito;
	@Column(name = "data_pagamento", nullable = false) private LocalDate dataPagamento;
	@Column(name = "chave_idempotencia", nullable = false) private String chaveIdempotencia;
	@Column(name = "hash_requisicao", nullable = false) private String hashRequisicao;
	@Column(name = "estornado_em") private LocalDateTime estornadoEm;
	@Version private Long version;
}
