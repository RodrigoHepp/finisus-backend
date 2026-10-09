package com.finisus.adapters.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.finisus.domain.model.StatusFinanciamento;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "financiamento")
@Getter
@Setter
public class FinanciamentoJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id")
	private Long usuarioId;

	private String descricao;

	private BigDecimal principal;

	@Column(name = "taxa_juros_mensal")
	private BigDecimal taxaJurosMensal;

	@Column(name = "numero_parcelas")
	private Integer numeroParcelas;

	@Column(name = "data_inicio")
	private LocalDate dataInicio;

	@Column(name = "conta_id")
	private Long contaId;

	@Column(name = "finalizado_em")
	private LocalDateTime finalizadoEm;

	@Enumerated(EnumType.STRING)
	private StatusFinanciamento status;

	@Column(name = "cancelada_em")
	private LocalDateTime canceladaEm;

	@Column(name = "cronograma_versao", nullable = false)
	private Integer cronogramaVersao;

	@Column(name = "financiamento_origem_id")
	private Long financiamentoOrigemId;

	@OneToMany(mappedBy = "financiamento", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ParcelaFinanciamentoJpaEntity> parcelas = new ArrayList<>();
}
