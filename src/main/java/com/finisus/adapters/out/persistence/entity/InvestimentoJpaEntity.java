package com.finisus.adapters.out.persistence.entity;

import com.finisus.domain.model.TipoInvestimento;
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

@Entity
@Table(name = "investimento")
@Getter
@Setter
public class InvestimentoJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id")
	private Long usuarioId;

	private String nome;

	@Enumerated(EnumType.STRING)
	private TipoInvestimento tipo;

	@Column(name = "conta_origem_id")
	private Long contaOrigemId;

	@Column(name = "conta_custodia_id")
	private Long contaCustodiaId;

	private boolean ativo;
}
