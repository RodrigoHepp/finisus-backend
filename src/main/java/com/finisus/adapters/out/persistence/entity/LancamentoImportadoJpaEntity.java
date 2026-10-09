package com.finisus.adapters.out.persistence.entity;

import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.EstadoLancamentoImportado;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

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
	@Column(name = "data_original") private LocalDate dataOriginal;
	@Column(name = "descricao_original") private String descricaoOriginal;
	@Column(name = "valor_original") private BigDecimal valorOriginal;
	@Column(name = "tipo_original") @Enumerated(EnumType.STRING) private TipoTransacao tipoOriginal;
	private BigDecimal valor;
	@Enumerated(EnumType.STRING) private TipoTransacao tipo;
	@Column(name = "pendente_confirmacao") private boolean pendenteConfirmacao;
	@Column(name = "motivo_pendencia") private String motivoPendencia;
	@Enumerated(EnumType.STRING) private EstadoLancamentoImportado estado;
	private boolean importar;
	@Column(name = "categoria_id") private Long categoriaId;
	@Column(name = "item_id") private Long itemId;
	@Column(name = "transacao_id") private Long transacaoId;
	@Column(name = "obrigacao_financeira_id") private Long obrigacaoFinanceiraId;
	@OneToMany(mappedBy = "lancamento", cascade = CascadeType.ALL, orphanRemoval = false)
	@BatchSize(size = 100)
	private List<RevisaoLancamentoImportadoJpaEntity> revisoes = new ArrayList<>();
}
