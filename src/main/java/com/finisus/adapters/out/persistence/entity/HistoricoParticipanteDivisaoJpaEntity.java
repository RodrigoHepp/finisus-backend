package com.finisus.adapters.out.persistence.entity;

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
@Table(name = "historico_participante_divisao")
@Getter
@Setter
public class HistoricoParticipanteDivisaoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "divisao_compartilhada_id", nullable = false)
    private Long divisaoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentual;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDateTime vigenteDesde;

    @Column(name = "vigente_ate")
    private LocalDateTime vigenteAte;
}
