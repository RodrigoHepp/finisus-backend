package com.finisus.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LancamentoImportado {
	private final Long id;
	private final int ordem;
	private LocalDate data;
	private String descricao;
	private final String conteudoOriginal;
	private BigDecimal valor;
	private TipoTransacao tipo;
	private boolean pendenteConfirmacao;
	private String motivoPendencia;
	private boolean importar;
	private Long categoriaId;
	private Long itemId;
	private Long transacaoId;

	private LancamentoImportado(Long id, int ordem, LocalDate data, String descricao, String conteudoOriginal,
			BigDecimal valor, TipoTransacao tipo, boolean pendenteConfirmacao, String motivoPendencia, boolean importar,
			Long categoriaId, Long itemId, Long transacaoId) {
		this.id = id;
		this.ordem = ordem;
		this.data = data;
		this.descricao = descricao;
		this.conteudoOriginal = conteudoOriginal;
		this.valor = valor;
		this.tipo = tipo;
		this.pendenteConfirmacao = pendenteConfirmacao;
		this.motivoPendencia = motivoPendencia;
		this.importar = importar;
		this.categoriaId = categoriaId;
		this.itemId = itemId;
		this.transacaoId = transacaoId;
	}

	public static LancamentoImportado novo(int ordem, LocalDate data, String descricao, String conteudoOriginal,
			BigDecimal valor, TipoTransacao tipo, boolean pendenteConfirmacao, String motivoPendencia, boolean importar) {
		return new LancamentoImportado(null, ordem, data, descricao, conteudoOriginal, valor, tipo,
				pendenteConfirmacao, motivoPendencia, importar, null, null, null);
	}

	public static LancamentoImportado reconstituir(Long id, int ordem, LocalDate data, String descricao,
			String conteudoOriginal, BigDecimal valor, TipoTransacao tipo, boolean pendenteConfirmacao,
			String motivoPendencia, boolean importar, Long categoriaId, Long itemId, Long transacaoId) {
		return new LancamentoImportado(id, ordem, data, descricao, conteudoOriginal, valor, tipo,
				pendenteConfirmacao, motivoPendencia, importar, categoriaId, itemId, transacaoId);
	}

	public void revisar(LocalDate data, String descricao, BigDecimal valor, TipoTransacao tipo, boolean importar,
			Long categoriaId, Long itemId) {
		this.data = data;
		this.descricao = descricao;
		this.valor = valor;
		this.tipo = tipo;
		this.importar = importar;
		this.categoriaId = categoriaId;
		this.itemId = itemId;
		this.pendenteConfirmacao = false;
		this.motivoPendencia = null;
	}

	public void vincularTransacao(Long transacaoId) {
		this.transacaoId = transacaoId;
	}

	public Long getId() { return id; }
	public int getOrdem() { return ordem; }
	public LocalDate getData() { return data; }
	public String getDescricao() { return descricao; }
	public String getConteudoOriginal() { return conteudoOriginal; }
	public BigDecimal getValor() { return valor; }
	public TipoTransacao getTipo() { return tipo; }
	public boolean isPendenteConfirmacao() { return pendenteConfirmacao; }
	public String getMotivoPendencia() { return motivoPendencia; }
	public boolean isImportar() { return importar; }
	public Long getCategoriaId() { return categoriaId; }
	public Long getItemId() { return itemId; }
	public Long getTransacaoId() { return transacaoId; }
}
