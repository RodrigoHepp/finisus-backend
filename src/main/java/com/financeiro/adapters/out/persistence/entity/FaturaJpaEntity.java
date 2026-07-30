package com.financeiro.adapters.out.persistence.entity;

import com.financeiro.domain.model.StatusFatura;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "fatura")
@Getter
@Setter
public class FaturaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cartao_id")
    private Long cartaoId;

    @Column(name = "ano_mes")
    private String anoMes;

    @Column(name = "data_fechamento")
    private LocalDate dataFechamento;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    private StatusFatura status;

    @Column(name = "conta_pagamento_id")
    private Long contaPagamentoId;

    @Version
    private Long version;
}
