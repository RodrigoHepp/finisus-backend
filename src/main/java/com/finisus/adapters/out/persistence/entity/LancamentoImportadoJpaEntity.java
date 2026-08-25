package com.finisus.adapters.out.persistence.entity;

import com.finisus.domain.model.TipoTransacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lancamento_importado")
@Getter
@Setter
public class LancamentoImportadoJpaEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(optional = false)
	@JoinColumn(name = "importacao_id")
	private ImportacaoFinanceiraJpaEntity importacao;
	private int ordem;
	private LocalDate data;
	private String descricao;
	@Column(name = "conteudo_original") private String conteudoOriginal;
	private BigDecimal valor;
	@Enumerated(EnumType.STRING) private TipoTransacao tipo;
	@Column(name = "pendente_confirmacao") private boolean pendenteConfirmacao;
	@Column(name = "motivo_pendencia") private String motivoPendencia;
	private boolean importar;
	@Column(name = "categoria_id") private Long categoriaId;
	@Column(name = "item_id") private Long itemId;
	@Column(name = "transacao_id") private Long transacaoId;
}
