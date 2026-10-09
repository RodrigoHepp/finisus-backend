package com.finisus.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "participante_divisao_compartilhada")
@Getter
@Setter
public class ParticipanteDivisaoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "divisao_compartilhada_id", nullable = false)
    private DivisaoCompartilhadaJpaEntity divisao;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentual;
}
