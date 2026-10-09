package com.finisus.domain.model;

import com.finisus.domain.DomainException;
import com.finisus.domain.vo.ValorMonetario;

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
	private final Long faturaPagamentoId;
	private final Long compraParceladaId;
	private final Long recorrenciaId;
	private final Long transferenciaId;
	private final LocalDateTime estornadoEm;
	private final long version;
	private final List<TransacaoItem> itens;

	private Transacao(Long id, Long usuarioId, TipoTransacao tipo, ValorMonetario valor, LocalDate data,
			String descricao, Long contaId, Long categoriaId, Long meioPagamentoId, Long faturaId,
			Long faturaPagamentoId, Long compraParceladaId, Long recorrenciaId, Long transferenciaId,
			LocalDateTime estornadoEm,
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
		this.faturaPagamentoId = faturaPagamentoId;
		this.compraParceladaId = compraParceladaId;
		this.recorrenciaId = recorrenciaId;
		this.transferenciaId = transferenciaId;
		this.estornadoEm = estornadoEm;
		this.version = version;
		this.itens = itens != null ? new ArrayList<>(itens) : new ArrayList<>();
		validarOrigemExclusiva();
		validarSomaItens();
	}

	public static Transacao nova(Long usuarioId, TipoTransacao tipo, ValorMonetario valor, LocalDate data,
			String descricao, Long contaId, Long categoriaId, Long meioPagamentoId, List<TransacaoItem> itens) {
		return new Transacao(null, usuarioId, tipo, valor, data, descricao, contaId, categoriaId, meioPagamentoId, null,
				null, null, null, null, null, 0, itens);
	}

	public static Transacao geradaPorRecorrencia(Long usuarioId, TipoTransacao tipo, ValorMonetario valor,
			LocalDate data, String descricao, Long contaId, Long categoriaId, Long meioPagamentoId,
			Long recorrenciaId) {
		return new Transacao(null, usuarioId, tipo, valor, data, descricao, contaId, categoriaId, meioPagamentoId, null,
				null, null, recorrenciaId, null, null, 0, List.of());
	}

	public static Transacao geradaPorCompraParcelada(Long usuarioId, ValorMonetario valor, LocalDate data,
			String descricao, Long contaId, Long categoriaId, Long compraParceladaId) {
		return geradaPorCompraParcelada(usuarioId, valor, data, descricao, contaId, categoriaId, compraParceladaId,
				null);
	}

	public static Transacao geradaPorCompraParcelada(Long usuarioId, ValorMonetario valor, LocalDate data,
			String descricao, Long contaId, Long categoriaId, Long compraParceladaId, Long faturaId) {
		return new Transacao(null, usuarioId, TipoTransacao.SAIDA, valor, data, descricao, contaId, categoriaId, null,
				faturaId, null, compraParceladaId, null, null, null, 0, List.of());
	}

	public static Transacao gastoCartao(Long usuarioId, ValorMonetario valor, LocalDate data, String descricao,
			Long contaId, Long categoriaId, Long faturaId, List<TransacaoItem> itens) {
		return new Transacao(null, usuarioId, TipoTransacao.SAIDA, valor, data, descricao, contaId, categoriaId, null,
				faturaId, null, null, null, null, null, 0, itens);
	}

	public static Transacao creditoFatura(Long usuarioId, ValorMonetario valor, LocalDate data, String descricao,
			Long contaId, Long categoriaId, Long faturaId, List<TransacaoItem> itens) {
		return new Transacao(null, usuarioId, TipoTransacao.ENTRADA, valor, data, descricao, contaId, categoriaId, null,
				faturaId, null, null, null, null, null, 0, itens);
	}

	public static Transacao pagamentoFatura(Long usuarioId, ValorMonetario valor, LocalDate data, String descricao,
			Long contaId, Long faturaPagamentoId) {
		return new Transacao(null, usuarioId, TipoTransacao.SAIDA, valor, data, descricao, contaId, null, null,
				null, faturaPagamentoId, null, null, null, null, 0, List.of());
	}

	public static Transacao transferencia(Long usuarioId, TipoTransacao tipo, ValorMonetario valor, LocalDate data,
			String descricao, Long contaId, Long transferenciaId) {
		return new Transacao(null, usuarioId, tipo, valor, data, descricao, contaId, null, null, null, null, null,
				null, transferenciaId, null, 0, List.of());
	}

	public static Transacao reconstituir(Long id, Long usuarioId, TipoTransacao tipo, ValorMonetario valor,
			LocalDate data, String descricao, Long contaId, Long categoriaId, Long meioPagamentoId, Long faturaId,
			Long compraParceladaId, Long recorrenciaId, LocalDateTime estornadoEm,
			long version, List<TransacaoItem> itens) {
		return new Transacao(id, usuarioId, tipo, valor, data, descricao, contaId, categoriaId, meioPagamentoId,
				faturaId, null, compraParceladaId, recorrenciaId, null, estornadoEm, version, itens);
	}

	public static Transacao reconstituir(Long id, Long usuarioId, TipoTransacao tipo, ValorMonetario valor,
			LocalDate data, String descricao, Long contaId, Long categoriaId, Long meioPagamentoId, Long faturaId,
			Long faturaPagamentoId, Long compraParceladaId, Long recorrenciaId,
			LocalDateTime estornadoEm, long version, List<TransacaoItem> itens) {
		return new Transacao(id, usuarioId, tipo, valor, data, descricao, contaId, categoriaId, meioPagamentoId,
				faturaId, faturaPagamentoId, compraParceladaId, recorrenciaId, null,
				estornadoEm, version, itens);
	}

	public static Transacao reconstituirComTransferencia(Long id, Long usuarioId, TipoTransacao tipo,
			ValorMonetario valor, LocalDate data, String descricao, Long contaId, Long categoriaId,
			Long meioPagamentoId, Long faturaId, Long faturaPagamentoId, Long compraParceladaId, Long recorrenciaId,
			Long transferenciaId, LocalDateTime estornadoEm, long version,
			List<TransacaoItem> itens) {
		return new Transacao(id, usuarioId, tipo, valor, data, descricao, contaId, categoriaId, meioPagamentoId,
				faturaId, faturaPagamentoId, compraParceladaId, recorrenciaId, transferenciaId,
				estornadoEm, version, itens);
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
		if (faturaId != null || faturaPagamentoId != null || compraParceladaId != null || recorrenciaId != null
				|| transferenciaId != null) {
			throw new DomainException("error.transacao.origem.imutavel");
		}
		if (isEstornada()) {
			throw new DomainException("error.transacao.estornada");
		}
		return new Transacao(id, usuarioId, novoTipo, novoValor, novaData, novaDescricao, novaContaId, novaCategoriaId,
				novoMeioPagamentoId, faturaId, faturaPagamentoId, compraParceladaId, recorrenciaId,
				transferenciaId, null, version,
				novosItens);
	}

	public Transacao detalhada(List<TransacaoItem> novosItens) {
		if (faturaPagamentoId != null || compraParceladaId != null || transferenciaId != null) {
			throw new DomainException("error.transacao.detalhamento.origem.nao.permitida");
		}
		if (isEstornada()) {
			throw new DomainException("error.transacao.estornada");
		}
		return new Transacao(id, usuarioId, tipo, valor, data, descricao, contaId, categoriaId, meioPagamentoId,
				faturaId, faturaPagamentoId, compraParceladaId, recorrenciaId, transferenciaId,
				estornadoEm, version, novosItens);
	}

	public Transacao estornada(LocalDateTime estornadaEm) {
		if (isEstornada()) {
			throw new DomainException("error.transacao.estornada");
		}
		return new Transacao(id, usuarioId, tipo, valor, data, descricao, contaId, categoriaId, meioPagamentoId,
				faturaId, faturaPagamentoId, compraParceladaId, recorrenciaId, transferenciaId,
				estornadaEm, version, itens);
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
		if (faturaPagamentoId != null)
			count++;
		if (compraParceladaId != null)
			count++;
		if (recorrenciaId != null)
			count++;
		if (transferenciaId != null)
			count++;
		boolean parcelaDeFatura = faturaId != null && compraParceladaId != null && count == 2;
		if (count > 1 && !parcelaDeFatura) {
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

	public void validarEstornoGenerico() {
		if (faturaPagamentoId != null || transferenciaId != null) {
			throw new DomainException("error.transacao.origem.imutavel");
		}
	}

	public Long getFaturaPagamentoId() {
		return faturaPagamentoId;
	}

	public Long getCompraParceladaId() {
		return compraParceladaId;
	}

	public Long getRecorrenciaId() {
		return recorrenciaId;
	}

	public Long getTransferenciaId() { return transferenciaId; }

	public LocalDateTime getEstornadoEm() {
		return estornadoEm;
	}

	public long getVersion() {
		return version;
	}
}
