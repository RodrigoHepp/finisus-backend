package com.finisus.adapters.out.persistence.entity;

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
import com.finisus.domain.model.StatusSnapshotDivisao;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacao_divisao_compartilhada")
@Getter
@Setter
public class VinculoTransacaoDivisaoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "divisao_compartilhada_id", nullable = false)
    private DivisaoCompartilhadaJpaEntity divisao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transacao_id", nullable = false)
    private TransacaoJpaEntity transacao;

	@Enumerated(EnumType.STRING)
	@Column(name = "status_snapshot", nullable = false)
	private StatusSnapshotDivisao statusSnapshot;

    @Column(name = "base_compartilhada", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseCompartilhada;

    @Column(name = "transacao_ativa_id")
    private Long transacaoAtivaId;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @Column(name = "cancelado_por")
    private Long canceladoPor;
}
