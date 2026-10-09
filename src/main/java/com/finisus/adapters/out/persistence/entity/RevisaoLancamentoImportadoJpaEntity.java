package com.finisus.adapters.out.persistence.entity;

import com.finisus.domain.model.DecisaoRevisaoImportacao;
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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "revisao_lancamento_importado")
@Getter
@Setter
public class RevisaoLancamentoImportadoJpaEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@ManyToOne(optional = false) @JoinColumn(name = "lancamento_importado_id")
	private LancamentoImportadoJpaEntity lancamento;
	@Column(name = "revisado_por") private Long revisadoPor;
	@Enumerated(EnumType.STRING) private DecisaoRevisaoImportacao decisao;
	@Column(name = "motivo_incerteza") private String motivoIncerteza;
	private String justificativa;
	@Column(name = "data_anterior") private LocalDate dataAnterior;
	@Column(name = "descricao_anterior") private String descricaoAnterior;
	@Column(name = "valor_anterior") private BigDecimal valorAnterior;
	@Column(name = "tipo_anterior") @Enumerated(EnumType.STRING) private TipoTransacao tipoAnterior;
	@Column(name = "importar_anterior") private boolean importarAnterior;
	@Column(name = "categoria_id_anterior") private Long categoriaIdAnterior;
	@Column(name = "item_id_anterior") private Long itemIdAnterior;
	@Column(name = "transacao_id_anterior") private Long transacaoIdAnterior;
	@Column(name = "obrigacao_id_anterior") private Long obrigacaoIdAnterior;
	@Column(name = "data_nova") private LocalDate dataNova;
	@Column(name = "descricao_nova") private String descricaoNova;
	@Column(name = "valor_novo") private BigDecimal valorNovo;
	@Column(name = "tipo_novo") @Enumerated(EnumType.STRING) private TipoTransacao tipoNovo;
	@Column(name = "importar_novo") private boolean importarNovo;
	@Column(name = "categoria_id_nova") private Long categoriaIdNova;
	@Column(name = "item_id_novo") private Long itemIdNovo;
	@Column(name = "transacao_id_nova") private Long transacaoIdNova;
	@Column(name = "obrigacao_id_nova") private Long obrigacaoIdNova;
	@Column(name = "revisado_em") private LocalDateTime revisadoEm;
}
