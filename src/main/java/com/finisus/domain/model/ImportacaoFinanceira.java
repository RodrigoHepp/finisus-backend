package com.finisus.domain.model;

import com.finisus.domain.DomainException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImportacaoFinanceira {
	private final Long id;
	private final Long usuarioId;
	private final Long bancoId;
	private final String nomeArquivo;
	private final String hashArquivo;
	private final String leitor;
	private final TipoDocumentoFinanceiro tipoDocumento;
	private final String identificadorOrigem;
	private final LocalDate periodoInicio;
	private final LocalDate periodoFim;
	private final LocalDate dataVencimento;
	private final BigDecimal saldoInicial;
	private final BigDecimal saldoFinal;
	private final BigDecimal valorTotal;
	private StatusImportacaoFinanceira status;
	private Long contaId;
	private Long faturaId;
	private final long version;
	private final List<LancamentoImportado> lancamentos;

	private ImportacaoFinanceira(Long id, Long usuarioId, Long bancoId, String nomeArquivo, String hashArquivo,
			String leitor, TipoDocumentoFinanceiro tipoDocumento, String identificadorOrigem, LocalDate periodoInicio, LocalDate periodoFim,
			LocalDate dataVencimento, BigDecimal saldoInicial, BigDecimal saldoFinal, BigDecimal valorTotal,
			StatusImportacaoFinanceira status, Long contaId, Long faturaId, long version,
			List<LancamentoImportado> lancamentos) {
		this.id = id; this.usuarioId = usuarioId; this.bancoId = bancoId; this.nomeArquivo = nomeArquivo;
		this.hashArquivo = hashArquivo; this.leitor = leitor; this.tipoDocumento = tipoDocumento; this.identificadorOrigem = identificadorOrigem;
		this.periodoInicio = periodoInicio; this.periodoFim = periodoFim; this.dataVencimento = dataVencimento;
		this.saldoInicial = saldoInicial; this.saldoFinal = saldoFinal; this.valorTotal = valorTotal;
		this.status = status; this.contaId = contaId; this.faturaId = faturaId; this.version = version;
		this.lancamentos = new ArrayList<>(lancamentos);
	}

	public static ImportacaoFinanceira nova(Long usuarioId, Long bancoId, String nomeArquivo, String hashArquivo,
			String leitor, TipoDocumentoFinanceiro tipoDocumento, String identificadorOrigem, LocalDate periodoInicio, LocalDate periodoFim,
			LocalDate dataVencimento, BigDecimal saldoInicial, BigDecimal saldoFinal, BigDecimal valorTotal,
			List<LancamentoImportado> lancamentos) {
		return new ImportacaoFinanceira(null, usuarioId, bancoId, nomeArquivo, hashArquivo, leitor, tipoDocumento, identificadorOrigem,
				periodoInicio, periodoFim, dataVencimento, saldoInicial, saldoFinal, valorTotal,
				StatusImportacaoFinanceira.PENDENTE_REVISAO, null, null, 0, lancamentos);
	}

	public static ImportacaoFinanceira reconstituir(Long id, Long usuarioId, Long bancoId, String nomeArquivo,
			String hashArquivo, String leitor, TipoDocumentoFinanceiro tipoDocumento, String identificadorOrigem, LocalDate periodoInicio,
			LocalDate periodoFim, LocalDate dataVencimento, BigDecimal saldoInicial, BigDecimal saldoFinal,
			BigDecimal valorTotal, StatusImportacaoFinanceira status, Long contaId, Long faturaId, long version,
			List<LancamentoImportado> lancamentos) {
		return new ImportacaoFinanceira(id, usuarioId, bancoId, nomeArquivo, hashArquivo, leitor, tipoDocumento, identificadorOrigem,
				periodoInicio, periodoFim, dataVencimento, saldoInicial, saldoFinal, valorTotal, status, contaId,
				faturaId, version, lancamentos);
	}

	public void revisar(Long contaId, Long faturaId, List<RevisaoLancamento> revisoes) {
		exigirPendente();
		this.contaId = contaId;
		this.faturaId = faturaId;
		for (RevisaoLancamento revisao : revisoes) {
			LancamentoImportado lancamento = lancamentos.stream().filter(l -> l.getId().equals(revisao.lancamentoId()))
					.findFirst().orElseThrow(() -> new DomainException("error.importacao.revisao.invalida"));
			lancamento.revisar(revisao.data(), revisao.descricao(), revisao.valor(), revisao.tipo(),
					revisao.importar(), revisao.categoriaId(), revisao.itemId());
		}
	}

	public void confirmar() {
		exigirPendente();
		if (lancamentos.stream().noneMatch(LancamentoImportado::isImportar))
			throw new DomainException("error.importacao.sem.lancamentos");
		if ((tipoDocumento == TipoDocumentoFinanceiro.FATURA_CARTAO && faturaId == null)
				|| (tipoDocumento != TipoDocumentoFinanceiro.FATURA_CARTAO && contaId == null))
			throw new DomainException("error.importacao.revisao.invalida");
		if (lancamentos.stream().anyMatch(l -> l.isImportar() && l.isPendenteConfirmacao()))
			throw new DomainException("error.importacao.revisao.invalida");
		if (lancamentos.stream().filter(LancamentoImportado::isImportar)
				.anyMatch(l -> l.getData() == null || l.getDescricao() == null || l.getDescricao().isBlank()
						|| l.getValor() == null || l.getValor().signum() <= 0 || l.getTipo() == null))
			throw new DomainException("error.importacao.revisao.invalida");
		this.status = StatusImportacaoFinanceira.CONFIRMADA;
	}

	private void exigirPendente() {
		if (status != StatusImportacaoFinanceira.PENDENTE_REVISAO)
			throw new DomainException("error.importacao.estado.invalido");
	}

	public record RevisaoLancamento(Long lancamentoId, LocalDate data, String descricao, BigDecimal valor,
			TipoTransacao tipo, boolean importar, Long categoriaId, Long itemId) { }
	public Long getId() { return id; } public Long getUsuarioId() { return usuarioId; } public Long getBancoId() { return bancoId; }
	public String getNomeArquivo() { return nomeArquivo; } public String getHashArquivo() { return hashArquivo; }
	public String getLeitor() { return leitor; } public TipoDocumentoFinanceiro getTipoDocumento() { return tipoDocumento; }
	public String getIdentificadorOrigem() { return identificadorOrigem; }
	public LocalDate getPeriodoInicio() { return periodoInicio; } public LocalDate getPeriodoFim() { return periodoFim; }
	public LocalDate getDataVencimento() { return dataVencimento; } public BigDecimal getSaldoInicial() { return saldoInicial; }
	public BigDecimal getSaldoFinal() { return saldoFinal; } public BigDecimal getValorTotal() { return valorTotal; }
	public StatusImportacaoFinanceira getStatus() { return status; } public Long getContaId() { return contaId; }
	public Long getFaturaId() { return faturaId; } public long getVersion() { return version; }
	public List<LancamentoImportado> getLancamentos() { return Collections.unmodifiableList(lancamentos); }
}
