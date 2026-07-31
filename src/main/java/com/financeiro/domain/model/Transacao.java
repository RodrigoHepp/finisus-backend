package com.financeiro.domain.model;

import com.financeiro.domain.DomainException;
import com.financeiro.domain.vo.ValorMonetario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Transacao {

	private final Long id;
	private final Long usuarioId;
	private final TipoTransacao tipo;
	private final ValorMonetario valor;
	private final LocalDate data;
	private final String descricao;
	private final Long contaId;
	private final Long categoriaId;
	private final Long meioPagamentoId;
	private final Long faturaId;
	private final Long compraParceladaId;
	private final Long recorrenciaId;
	private final Long despesaCompartilhadaId;
	private final LocalDateTime estornadoEm;
	private final long version;
	private final List<TransacaoItem> itens;

	private Transacao(Long id, Long usuarioId, TipoTransacao tipo, ValorMonetario valor, LocalDate data,
			String descricao, Long contaId, Long categoriaId, Long meioPagamentoId, Long faturaId,
			Long compraParceladaId, Long recorrenciaId, Long despesaCompartilhadaId, LocalDateTime estornadoEm,
			long version, List<TransacaoItem> itens) {
		this.id = id;
		this.usuarioId = usuarioId;
		this.tipo = tipo;
		this.valor = valor;
		this.data = data;
		this.descricao = descricao;
		this.contaId = contaId;
		this.categoriaId = categoriaId;
		this.meioPagamentoId = meioPagamentoId;
		this.faturaId = faturaId;
		this.compraParceladaId = compraParceladaId;
		this.recorrenciaId = recorrenciaId;
		this.despesaCompartilhadaId = despesaCompartilhadaId;
		this.estornadoEm = estornadoEm;
		this.version = version;
		this.itens = itens != null ? new ArrayList<>(itens) : new ArrayList<>();
		validarOrigemExclusiva();
		validarSomaItens();
	}

	public static Transacao nova(Long usuarioId, TipoTransacao tipo, ValorMonetario valor, LocalDate data,
			String descricao, Long contaId, Long categoriaId, Long meioPagamentoId, List<TransacaoItem> itens) {
		return new Transacao(null, usuarioId, tipo, valor, data, descricao, contaId, categoriaId, meioPagamentoId, null,
				null, null, null, null, 0, itens);
	}

	public static Transacao geradaPorRecorrencia(Long usuarioId, TipoTransacao tipo, ValorMonetario valor,
			LocalDate data, String descricao, Long contaId, Long categoriaId, Long meioPagamentoId,
			Long recorrenciaId) {
		return new Transacao(null, usuarioId, tipo, valor, data, descricao, contaId, categoriaId, meioPagamentoId, null,
				null, recorrenciaId, null, null, 0, List.of());
	}

	public static Transacao geradaPorCompraParcelada(Long usuarioId, ValorMonetario valor, LocalDate data,
			String descricao, Long contaId, Long categoriaId, Long compraParceladaId) {
		return new Transacao(null, usuarioId, TipoTransacao.SAIDA, valor, data, descricao, contaId, categoriaId, null,
				null, compraParceladaId, null, null, null, 0, List.of());
	}

	public static Transacao gastoCartao(Long usuarioId, ValorMonetario valor, LocalDate data, String descricao,
			Long contaId, Long categoriaId, Long faturaId, List<TransacaoItem> itens) {
		return new Transacao(null, usuarioId, TipoTransacao.SAIDA, valor, data, descricao, contaId, categoriaId, null,
				faturaId, null, null, null, null, 0, itens);
	}

	public static Transacao reconstituir(Long id, Long usuarioId, TipoTransacao tipo, ValorMonetario valor,
			LocalDate data, String descricao, Long contaId, Long categoriaId, Long meioPagamentoId, Long faturaId,
			Long compraParceladaId, Long recorrenciaId, Long despesaCompartilhadaId, LocalDateTime estornadoEm,
			long version, List<TransacaoItem> itens) {
		return new Transacao(id, usuarioId, tipo, valor, data, descricao, contaId, categoriaId, meioPagamentoId,
				faturaId, compraParceladaId, recorrenciaId, despesaCompartilhadaId, estornadoEm, version, itens);
	}

	public List<TransacaoItem> getItens() {
		return Collections.unmodifiableList(itens);
	}

	public boolean isEstornada() {
		return estornadoEm != null;
	}

	public boolean impactaSaldoDaConta() {
		return faturaId == null && compraParceladaId == null;
	}

	public Transacao corrigida(TipoTransacao novoTipo, ValorMonetario novoValor, LocalDate novaData,
			String novaDescricao, Long novaContaId, Long novaCategoriaId, Long novoMeioPagamentoId,
			List<TransacaoItem> novosItens) {
		if (faturaId != null || compraParceladaId != null || recorrenciaId != null) {
			throw new DomainException("error.transacao.origem.imutavel");
		}
		if (isEstornada()) {
			throw new DomainException("error.transacao.estornada");
		}
		return new Transacao(id, usuarioId, novoTipo, novoValor, novaData, novaDescricao, novaContaId, novaCategoriaId,
				novoMeioPagamentoId, faturaId, compraParceladaId, recorrenciaId, despesaCompartilhadaId, null, version,
				novosItens);
	}

	public Transacao estornada(LocalDateTime estornadaEm) {
		if (isEstornada()) {
			throw new DomainException("error.transacao.estornada");
		}
		return new Transacao(id, usuarioId, tipo, valor, data, descricao, contaId, categoriaId, meioPagamentoId,
				faturaId, compraParceladaId, recorrenciaId, despesaCompartilhadaId, estornadaEm, version, itens);
	}

	public Transacao associadaADespesaCompartilhada(Long despesaId) {
		if (faturaId != null || compraParceladaId != null || recorrenciaId != null || despesaCompartilhadaId != null) {
			throw new DomainException("error.transacao.origem.imutavel");
		}
		return new Transacao(id, usuarioId, tipo, valor, data, descricao, contaId, categoriaId, meioPagamentoId, null,
				null, null, despesaId, estornadoEm, version, itens);
	}

	public void validarAlteracaoSemCompartilhamentoAtivo(boolean possuiCompartilhamentoAtivo) {
		if (possuiCompartilhamentoAtivo) {
			throw new DomainException("error.transacao.compartilhamento.ativo");
		}
	}

	private void validarOrigemExclusiva() {
		int count = 0;
		if (faturaId != null)
			count++;
		if (compraParceladaId != null)
			count++;
		if (recorrenciaId != null)
			count++;
		if (despesaCompartilhadaId != null)
			count++;
		if (count > 1) {
			throw new DomainException("error.transacao.origem.exclusiva");
		}
	}

	private void validarSomaItens() {
		if (itens.isEmpty()) {
			return;
		}
		BigDecimal soma = itens.stream().map(i -> i.getValor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add);
		if (soma.compareTo(valor.valor()) != 0) {
			throw new DomainException("error.transacao.itens.soma.invalida");
		}
	}

	public Long getId() {
		return id;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public TipoTransacao getTipo() {
		return tipo;
	}

	public ValorMonetario getValor() {
		return valor;
	}

	public LocalDate getData() {
		return data;
	}

	public String getDescricao() {
		return descricao;
	}

	public Long getContaId() {
		return contaId;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public Long getMeioPagamentoId() {
		return meioPagamentoId;
	}

	public Long getFaturaId() {
		return faturaId;
	}

	public Long getCompraParceladaId() {
		return compraParceladaId;
	}

	public Long getRecorrenciaId() {
		return recorrenciaId;
	}

	public Long getDespesaCompartilhadaId() {
		return despesaCompartilhadaId;
	}

	public LocalDateTime getEstornadoEm() {
		return estornadoEm;
	}

	public long getVersion() {
		return version;
	}
}
