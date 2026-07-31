package com.financeiro.adapters.out.persistence.entity;

import com.financeiro.domain.model.TipoRateio;
import com.financeiro.domain.model.TipoAlvoCompartilhamento;
import com.financeiro.domain.model.StatusDespesaCompartilhada;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "despesa_compartilhada")
@Getter
@Setter
public class DespesaCompartilhadaJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "transacao_id")
	private Long transacaoId;

	@Column(name = "transacao_item_id")
	private Long transacaoItemId;

	@Column(name = "criador_id")
	private Long criadorId;

	@Column(name = "tipo_rateio")
	@Enumerated(EnumType.STRING)
	private TipoRateio tipoRateio;

	@Column(name = "tipo_alvo")
	@Enumerated(EnumType.STRING)
	private TipoAlvoCompartilhamento tipoAlvo;

	@Enumerated(EnumType.STRING)
	private StatusDespesaCompartilhada status;

	@Column(name = "cancelada_em")
	private LocalDateTime canceladaEm;

	@OneToMany(mappedBy = "despesaCompartilhada", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RateioDespesaJpaEntity> rateios = new ArrayList<>();
}
