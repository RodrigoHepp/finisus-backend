package com.financeiro.adapters.out.persistence.entity;

import com.financeiro.domain.model.TipoConta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "conta")
@Getter
@Setter
public class ContaJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id")
	private Long usuarioId;

	private String nome;

	@Enumerated(EnumType.STRING)
	private TipoConta tipo;

	@Column(name = "banco_id")
	private Long bancoId;

	private BigDecimal saldo;

	private boolean ativo;

	@Version
	private Long version;

	@Column(name = "criado_em", insertable = false, updatable = false)
	private LocalDateTime criadoEm;
}
