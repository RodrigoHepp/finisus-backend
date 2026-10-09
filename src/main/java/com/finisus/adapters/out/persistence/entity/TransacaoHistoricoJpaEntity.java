package com.finisus.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transacao_historico")
@Getter
@Setter
public class TransacaoHistoricoJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "transacao_id")
	private Long transacaoId;

	@Column(name = "campo_alterado")
	private String campoAlterado;

	@Column(name = "valor_anterior")
	private String valorAnterior;

	@Column(name = "valor_novo")
	private String valorNovo;

	@Column(name = "alterado_por")
	private Long alteradoPor;

	@Column(name = "alterado_em")
	private LocalDateTime alteradoEm;

	private String motivo;

	@Column(name = "correlacao_id", length = 36)
	private String correlacaoId;

	@Column(name = "snapshot_anterior", columnDefinition = "TEXT")
	private String snapshotAnterior;

	@Column(name = "snapshot_novo", columnDefinition = "TEXT")
	private String snapshotNovo;
}
