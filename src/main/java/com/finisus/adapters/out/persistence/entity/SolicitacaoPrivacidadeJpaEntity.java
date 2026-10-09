package com.finisus.adapters.out.persistence.entity;

import com.finisus.domain.model.SolicitacaoPrivacidade;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "solicitacao_privacidade")
@Getter
@Setter
public class SolicitacaoPrivacidadeJpaEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@Column(name = "usuario_id", nullable = false) private Long usuarioId;
	@Enumerated(EnumType.STRING) private SolicitacaoPrivacidade.Tipo tipo;
	@Enumerated(EnumType.STRING) private SolicitacaoPrivacidade.Status status;
	private String motivo;
	@Column(name = "solicitada_em") private LocalDateTime solicitadaEm;
	@Column(name = "concluida_em") private LocalDateTime concluidaEm;
	private String observacao;
	@Version private Long version;
}
