package com.finisus.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "responsabilidade_transacao_divisao")
@Getter
@Setter
public class ResponsabilidadeTransacaoDivisaoJpaEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vinculo_id", nullable = false)
	private VinculoTransacaoDivisaoJpaEntity vinculo;

	@Column(name = "usuario_id", nullable = false)
	private Long usuarioId;

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal percentual;

	@Column(name = "valor_devido", nullable = false, precision = 19, scale = 2)
	private BigDecimal valorDevido;
}
