package com.finisus.adapters.out.persistence.entity;

import com.finisus.domain.model.StatusImportacaoFinanceira;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "importacao_financeira")
@Getter
@Setter
public class ImportacaoFinanceiraJpaEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "usuario_id") private Long usuarioId;
	@Column(name = "banco_id") private Long bancoId;
	@Column(name = "nome_arquivo") private String nomeArquivo;
	@Column(name = "hash_arquivo") private String hashArquivo;
	private String leitor;
	@Column(name = "tipo_documento") @Enumerated(EnumType.STRING) private TipoDocumentoFinanceiro tipoDocumento;
	@Column(name = "tipo_documento_pretendido") @Enumerated(EnumType.STRING)
	private TipoDocumentoFinanceiro tipoDocumentoPretendido;
	@Column(name = "identificador_origem") private String identificadorOrigem;
	@Column(name = "periodo_inicio") private LocalDate periodoInicio;
	@Column(name = "periodo_fim") private LocalDate periodoFim;
	@Column(name = "data_vencimento") private LocalDate dataVencimento;
	@Column(name = "saldo_inicial") private BigDecimal saldoInicial;
	@Column(name = "saldo_final") private BigDecimal saldoFinal;
	@Column(name = "valor_total") private BigDecimal valorTotal;
	@Enumerated(EnumType.STRING) private StatusImportacaoFinanceira status;
	@Column(name = "conta_id") private Long contaId;
	@Column(name = "fatura_id") private Long faturaId;
	@Version private Long version;
	@OneToMany(mappedBy = "importacao", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LancamentoImportadoJpaEntity> lancamentos = new ArrayList<>();
}
