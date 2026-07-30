package com.financeiro.adapters.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "financiamento")
@Getter
@Setter
public class FinanciamentoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    private String descricao;

    private BigDecimal principal;

    @Column(name = "taxa_juros_mensal")
    private BigDecimal taxaJurosMensal;

    @Column(name = "numero_parcelas")
    private Integer numeroParcelas;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "conta_id")
    private Long contaId;

    @OneToMany(mappedBy = "financiamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParcelaFinanciamentoJpaEntity> parcelas = new ArrayList<>();
}
