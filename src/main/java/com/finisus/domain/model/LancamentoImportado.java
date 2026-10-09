package com.finisus.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LancamentoImportado {
	private final Long id;
	private final int ordem;
	private LocalDate data;
	private String descricao;
	private final String conteudoOriginal;
	private final LocalDate dataOriginal;
	private final String descricaoOriginal;
	private final BigDecimal valorOriginal;
	private final TipoTransacao tipoOriginal;
	private BigDecimal valor;
	private TipoTransacao tipo;
	private boolean pendenteConfirmacao;
	private String motivoPendencia;
	private EstadoLancamentoImportado estado;
	private boolean importar;
	private Long categoriaId;
	private Long itemId;
	private Long transacaoId;
	private Long obrigacaoFinanceiraId;
	private final List<RevisaoLancamentoImportado> revisoes;

	private LancamentoImportado(Long id, int ordem, LocalDate data, String descricao, String conteudoOriginal,
			BigDecimal valor, TipoTransacao tipo, boolean pendenteConfirmacao, String motivoPendencia, boolean importar,
			Long categoriaId, Long itemId, Long transacaoId, Long obrigacaoFinanceiraId,
			LocalDate dataOriginal, String descricaoOriginal, BigDecimal valorOriginal, TipoTransacao tipoOriginal,
			List<RevisaoLancamentoImportado> revisoes, EstadoLancamentoImportado estado) {
		this.id = id;
		this.ordem = ordem;
		this.data = data;
		this.descricao = descricao;
		this.conteudoOriginal = conteudoOriginal;
		this.dataOriginal = dataOriginal;
		this.descricaoOriginal = descricaoOriginal;
		this.valorOriginal = valorOriginal;
		this.tipoOriginal = tipoOriginal;
		this.valor = valor;
		this.tipo = tipo;
		this.pendenteConfirmacao = pendenteConfirmacao;
		this.motivoPendencia = motivoPendencia;
		this.estado = estado;
		this.importar = importar;
		this.categoriaId = categoriaId;
		this.itemId = itemId;
		this.transacaoId = transacaoId;
		this.obrigacaoFinanceiraId = obrigacaoFinanceiraId;
		this.revisoes = new ArrayList<>(revisoes);
	}

	public static LancamentoImportado novo(int ordem, LocalDate data, String descricao, String conteudoOriginal,
			BigDecimal valor, TipoTransacao tipo, boolean pendenteConfirmacao, String motivoPendencia, boolean importar) {
		return new LancamentoImportado(null, ordem, data, descricao, conteudoOriginal, valor, tipo,
			pendenteConfirmacao, motivoPendencia, importar, null, null, null, null,
			data, descricao, valor, tipo, List.of(), EstadoLancamentoImportado.PENDENTE);
	}

	public static LancamentoImportado reconstituir(Long id, int ordem, LocalDate data, String descricao,
			String conteudoOriginal, BigDecimal valor, TipoTransacao tipo, boolean pendenteConfirmacao,
			String motivoPendencia, boolean importar, Long categoriaId, Long itemId, Long transacaoId) {
		return reconstituir(id, ordem, data, descricao, conteudoOriginal, valor, tipo, pendenteConfirmacao,
				motivoPendencia, importar, categoriaId, itemId, transacaoId, null);
	}

	public static LancamentoImportado reconstituir(Long id, int ordem, LocalDate data, String descricao,
			String conteudoOriginal, BigDecimal valor, TipoTransacao tipo, boolean pendenteConfirmacao,
			String motivoPendencia, boolean importar, Long categoriaId, Long itemId, Long transacaoId,
			Long obrigacaoFinanceiraId) {
		return new LancamentoImportado(id, ordem, data, descricao, conteudoOriginal, valor, tipo,
				pendenteConfirmacao, motivoPendencia, importar, categoriaId, itemId, transacaoId, obrigacaoFinanceiraId,
				data, descricao, valor, tipo, List.of(), estadoLegado(importar, transacaoId, obrigacaoFinanceiraId));
	}

	public static LancamentoImportado reconstituir(Long id, int ordem, LocalDate data, String descricao,
			String conteudoOriginal, BigDecimal valor, TipoTransacao tipo, boolean pendenteConfirmacao,
			String motivoPendencia, boolean importar, Long categoriaId, Long itemId, Long transacaoId,
			Long obrigacaoFinanceiraId, LocalDate dataOriginal, String descricaoOriginal, BigDecimal valorOriginal,
			TipoTransacao tipoOriginal, List<RevisaoLancamentoImportado> revisoes) {
		return reconstituir(id, ordem, data, descricao, conteudoOriginal, valor, tipo, pendenteConfirmacao,
				motivoPendencia, importar, categoriaId, itemId, transacaoId, obrigacaoFinanceiraId, dataOriginal,
				descricaoOriginal, valorOriginal, tipoOriginal, revisoes,
				estadoLegado(importar, transacaoId, obrigacaoFinanceiraId));
	}

	public static LancamentoImportado reconstituir(Long id, int ordem, LocalDate data, String descricao,
			String conteudoOriginal, BigDecimal valor, TipoTransacao tipo, boolean pendenteConfirmacao,
			String motivoPendencia, boolean importar, Long categoriaId, Long itemId, Long transacaoId,
			Long obrigacaoFinanceiraId, LocalDate dataOriginal, String descricaoOriginal, BigDecimal valorOriginal,
			TipoTransacao tipoOriginal, List<RevisaoLancamentoImportado> revisoes, EstadoLancamentoImportado estado) {
		return new LancamentoImportado(id, ordem, data, descricao, conteudoOriginal, valor, tipo,
				pendenteConfirmacao, motivoPendencia, importar, categoriaId, itemId, transacaoId, obrigacaoFinanceiraId,
				dataOriginal, descricaoOriginal, valorOriginal, tipoOriginal, revisoes, estado);
	}

	private static EstadoLancamentoImportado estadoLegado(boolean importar, Long transacaoId,
			Long obrigacaoFinanceiraId) {
		if (!importar) return EstadoLancamentoImportado.IGNORADA;
		if (transacaoId != null || obrigacaoFinanceiraId != null) return EstadoLancamentoImportado.ASSOCIADA;
		return EstadoLancamentoImportado.PENDENTE;
	}

	public void revisar(Long revisadoPor, LocalDate data, String descricao, BigDecimal valor, TipoTransacao tipo,
			boolean importar, Long categoriaId, Long itemId, Long transacaoId, Long obrigacaoFinanceiraId,
			String justificativa, LocalDateTime revisadoEm) {
		if ((!importar && (transacaoId != null || obrigacaoFinanceiraId != null))
				|| (transacaoId != null && obrigacaoFinanceiraId != null)) {
			throw new com.finisus.domain.DomainException("error.importacao.revisao.invalida");
		}
		if (justificativa == null || justificativa.isBlank()) {
			throw new com.finisus.domain.DomainException("error.importacao.justificativa.obrigatoria");
		}
		var anterior = estadoAtual();
		String motivoIncerteza = this.motivoPendencia;
		this.data = data;
		this.descricao = descricao;
		this.valor = valor;
		this.tipo = tipo;
		this.importar = importar;
		this.categoriaId = categoriaId;
		this.itemId = itemId;
		this.transacaoId = transacaoId;
		this.obrigacaoFinanceiraId = obrigacaoFinanceiraId;
		this.pendenteConfirmacao = false;
		this.motivoPendencia = null;
		this.estado = decisao(importar, transacaoId, obrigacaoFinanceiraId) == DecisaoRevisaoImportacao.IGNORAR
				? EstadoLancamentoImportado.IGNORADA
				: (transacaoId != null || obrigacaoFinanceiraId != null
						? EstadoLancamentoImportado.ASSOCIADA : EstadoLancamentoImportado.PENDENTE);
		revisoes.add(new RevisaoLancamentoImportado(null, revisadoPor, decisao(importar, transacaoId,
				obrigacaoFinanceiraId), motivoIncerteza, justificativa.trim(), anterior, estadoAtual(), revisadoEm));
	}

	private RevisaoLancamentoImportado.Estado estadoAtual() {
		return new RevisaoLancamentoImportado.Estado(data, descricao, valor, tipo, importar, categoriaId, itemId,
				transacaoId, obrigacaoFinanceiraId);
	}

	private DecisaoRevisaoImportacao decisao(boolean importar, Long transacaoId, Long obrigacaoFinanceiraId) {
		if (!importar) return DecisaoRevisaoImportacao.IGNORAR;
		if (transacaoId != null) return DecisaoRevisaoImportacao.ASSOCIAR_TRANSACAO;
		if (obrigacaoFinanceiraId != null) return DecisaoRevisaoImportacao.ASSOCIAR_OBRIGACAO;
		return DecisaoRevisaoImportacao.CRIAR;
	}

	public void marcarCriadaComTransacao(Long transacaoId) {
		this.transacaoId = transacaoId;
		this.estado = EstadoLancamentoImportado.CRIADA;
	}

	public void marcarCriadaComObrigacaoFinanceira(Long obrigacaoFinanceiraId) {
		this.obrigacaoFinanceiraId = obrigacaoFinanceiraId;
		this.estado = EstadoLancamentoImportado.CRIADA;
	}

	public Long getId() { return id; }
	public int getOrdem() { return ordem; }
	public LocalDate getData() { return data; }
	public String getDescricao() { return descricao; }
	public String getConteudoOriginal() { return conteudoOriginal; }
	public LocalDate getDataOriginal() { return dataOriginal; }
	public String getDescricaoOriginal() { return descricaoOriginal; }
	public BigDecimal getValorOriginal() { return valorOriginal; }
	public TipoTransacao getTipoOriginal() { return tipoOriginal; }
	public BigDecimal getValor() { return valor; }
	public TipoTransacao getTipo() { return tipo; }
	public boolean isPendenteConfirmacao() { return pendenteConfirmacao; }
	public String getMotivoPendencia() { return motivoPendencia; }
	public EstadoLancamentoImportado getEstado() { return estado; }
	public boolean isImportar() { return importar; }
	public Long getCategoriaId() { return categoriaId; }
	public Long getItemId() { return itemId; }
	public Long getTransacaoId() { return transacaoId; }
	public Long getObrigacaoFinanceiraId() { return obrigacaoFinanceiraId; }
	public List<RevisaoLancamentoImportado> getRevisoes() { return Collections.unmodifiableList(revisoes); }
}
