package com.financeiro.adapters.out.persistence.entity;

import com.financeiro.domain.model.TipoMovimentoInvestimento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimento_investimento")
@Getter
@Setter
public class MovimentoInvestimentoJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "investimento_id")
	private Long investimentoId;

	@Enumerated(EnumType.STRING)
	private TipoMovimentoInvestimento tipo;

	private BigDecimal valor;

	private LocalDate data;

	@Column(name = "transacao_id")
	private Long transacaoId;

	@Column(name = "movimento_origem_id")
	private Long movimentoOrigemId;

	@Column(name = "estornado_em")
	private LocalDateTime estornadoEm;
}
