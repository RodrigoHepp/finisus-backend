package com.financeiro.adapters.out.persistence.entity;

import com.financeiro.domain.model.StatusRateio;
import com.financeiro.domain.model.TipoParticipante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "rateio_despesa")
@Getter
@Setter
public class RateioDespesaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despesa_compartilhada_id", nullable = false)
    private DespesaCompartilhadaJpaEntity despesaCompartilhada;

    @Column(name = "tipo_participante")
    @Enumerated(EnumType.STRING)
    private TipoParticipante tipoParticipante;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "nome_externo")
    private String nomeExterno;

    @Column(name = "email_externo")
    private String emailExterno;

    @Column(name = "valor_fixo")
    private BigDecimal valorFixo;

    private BigDecimal percentual;

    @Enumerated(EnumType.STRING)
    private StatusRateio status;
}
