package com.finisus.adapters.out.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ajuste_saldo_conta")
@Getter
@Setter
public class AjusteSaldoContaJpaEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "usuario_id") private Long usuarioId;
	@Column(name = "conta_id") private Long contaId;
	@Column(name = "saldo_anterior") private BigDecimal saldoAnterior;
	@Column(name = "saldo_calculado_anterior") private BigDecimal saldoCalculadoAnterior;
	@Column(name = "saldo_informado") private BigDecimal saldoInformado;
	@Column(name = "valor_ajuste") private BigDecimal valorAjuste;
	private String motivo;
	@Column(name = "data_ajuste") private LocalDate dataAjuste;
	@Column(name = "chave_idempotencia") private String chaveIdempotencia;
	@Column(name = "hash_requisicao") private String hashRequisicao;
	@Column(name = "criado_em", insertable = false, updatable = false) private LocalDateTime criadoEm;
	@Version private Long version;
}
