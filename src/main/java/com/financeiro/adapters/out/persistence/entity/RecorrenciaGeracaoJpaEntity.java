package com.financeiro.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recorrencia_geracao")
@Getter
@Setter
public class RecorrenciaGeracaoJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "recorrencia_id")
	private Long recorrenciaId;

	@Column(name = "ano_mes")
	private String anoMes;

	@Column(name = "transacao_id")
	private Long transacaoId;
}
