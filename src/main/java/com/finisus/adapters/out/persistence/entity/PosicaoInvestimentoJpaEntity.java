package com.finisus.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "posicao_investimento")
@Getter
@Setter
public class PosicaoInvestimentoJpaEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "investimento_id", nullable = false) private Long investimentoId;
	@Column(nullable = false) private BigDecimal valor;
	@Column(name = "data_referencia", nullable = false) private LocalDate dataReferencia;
	@Version private Long version;
}
