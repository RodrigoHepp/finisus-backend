package com.finisus.adapters.out.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.finisus.domain.model.StatusTransferencia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transferencia_conta")
@Getter @Setter
public class TransferenciaContaJpaEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "usuario_id") private Long usuarioId;
	@Column(name = "conta_origem_id") private Long contaOrigemId;
	@Column(name = "conta_destino_id") private Long contaDestinoId;
	private BigDecimal valor;
	@Column(name = "data_transferencia") private LocalDate data;
	private String descricao;
	@Column(name = "chave_idempotencia") private String chaveIdempotencia;
	@Column(name = "hash_requisicao") private String hashRequisicao;
	@Enumerated(EnumType.STRING) private StatusTransferencia status;
	@Column(name = "estornada_em") private LocalDateTime estornadaEm;
	@Version private Long version;
}
