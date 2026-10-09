package com.finisus.adapters.out.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "aplicacao_credito_fatura")
@Getter
@Setter
public class AplicacaoCreditoFaturaJpaEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@Column(name = "pagamento_origem_id", nullable = false) private Long pagamentoOrigemId;
	@Column(name = "fatura_destino_id", nullable = false) private Long faturaDestinoId;
	@Column(nullable = false) private BigDecimal valor;
	@Column(name = "criada_em", nullable = false) private LocalDateTime criadaEm;
}
