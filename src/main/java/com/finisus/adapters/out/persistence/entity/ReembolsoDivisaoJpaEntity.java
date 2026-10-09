package com.finisus.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reembolso_divisao")
@Getter @Setter
public class ReembolsoDivisaoJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "divisao_id", nullable = false)
    private DivisaoCompartilhadaJpaEntity divisao;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "transacao_id", nullable = false)
    private TransacaoJpaEntity transacao;
    @Column(name = "pagador_id", nullable = false) private Long pagadorId;
    @Column(name = "recebedor_id", nullable = false) private Long recebedorId;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal valor;
    @Column(name = "criado_em", nullable = false) private LocalDateTime criadoEm;
    @Column(name = "cancelado_em") private LocalDateTime canceladoEm;
    @Column(name = "cancelado_por") private Long canceladoPor;
    @Column(name = "transacao_ativa_id") private Long transacaoAtivaId;
}
