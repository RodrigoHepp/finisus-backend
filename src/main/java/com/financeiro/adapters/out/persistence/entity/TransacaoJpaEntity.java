package com.financeiro.adapters.out.persistence.entity;

import com.financeiro.domain.model.TipoTransacao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transacao")
@Getter
@Setter
public class TransacaoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    private TipoTransacao tipo;

    private BigDecimal valor;

    private LocalDate data;

    private String descricao;

    @Column(name = "conta_id")
    private Long contaId;

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Column(name = "meio_pagamento_id")
    private Long meioPagamentoId;

    @Column(name = "fatura_id")
    private Long faturaId;

    @Column(name = "compra_parcelada_id")
    private Long compraParceladaId;

    @Column(name = "recorrencia_id")
    private Long recorrenciaId;

    @Column(name = "despesa_compartilhada_id")
    private Long despesaCompartilhadaId;

    @Column(name = "estornado_em")
    private LocalDateTime estornadoEm;

    @Version
    private Long version;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "transacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransacaoItemJpaEntity> itens = new ArrayList<>();
}
