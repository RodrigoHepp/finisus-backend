package com.finisus.domain.model;

import com.finisus.domain.DomainException;
import com.finisus.domain.vo.ValorMonetario;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ObrigacaoFinanceira {
	private final Long id;
	private final Long usuarioId;
	private final String descricao;
	private final String credor;
	private final ValorMonetario valor;
	private final ValorMonetario valorPago;
	private final LocalDate dataVencimento;
	private final Long contaPagamentoId;
	private final Long categoriaId;
	private final StatusObrigacaoFinanceira status;
	private final LocalDate dataLiquidacao;
	private final Long transacaoId;
	private final LocalDateTime canceladaEm;
	private final long version;

	private ObrigacaoFinanceira(Long id, Long usuarioId, String descricao, String credor, ValorMonetario valor,
			ValorMonetario valorPago, LocalDate dataVencimento, Long contaPagamentoId, Long categoriaId, StatusObrigacaoFinanceira status,
			LocalDate dataLiquidacao, Long transacaoId, LocalDateTime canceladaEm, long version) {
		if (usuarioId == null || usuarioId <= 0 || descricao == null || descricao.isBlank() || credor == null
				|| credor.isBlank() || valor == null || valor.isZero() || dataVencimento == null || contaPagamentoId == null
				|| contaPagamentoId <= 0 || status == null) {
			throw new DomainException("error.obrigacao.invalida");
		}
		if (valorPago == null || valorPago.valor().compareTo(valor.valor()) > 0) {
			throw new DomainException("error.obrigacao.liquidacao.invalida");
		}
		if (status == StatusObrigacaoFinanceira.PAGA && (dataLiquidacao == null || valorPago.valor().compareTo(valor.valor()) != 0)) {
			throw new DomainException("error.obrigacao.liquidacao.invalida");
		}
		if (status != StatusObrigacaoFinanceira.PAGA && dataLiquidacao != null) {
			throw new DomainException("error.obrigacao.liquidacao.invalida");
		}
		if (status == StatusObrigacaoFinanceira.CANCELADA && canceladaEm == null) {
			throw new DomainException("error.obrigacao.cancelamento.invalido");
		}
		this.id = id;
		this.usuarioId = usuarioId;
		this.descricao = descricao;
		this.credor = credor;
		this.valor = valor;
		this.valorPago = valorPago;
		this.dataVencimento = dataVencimento;
		this.contaPagamentoId = contaPagamentoId;
		this.categoriaId = categoriaId;
		this.status = status;
		this.dataLiquidacao = dataLiquidacao;
		this.transacaoId = transacaoId;
		this.canceladaEm = canceladaEm;
		this.version = version;
	}

	public static ObrigacaoFinanceira nova(Long usuarioId, String descricao, String credor, ValorMonetario valor,
			LocalDate dataVencimento, Long contaPagamentoId, Long categoriaId) {
		return new ObrigacaoFinanceira(null, usuarioId, descricao, credor, valor, ValorMonetario.zero(), dataVencimento, contaPagamentoId,
				categoriaId, StatusObrigacaoFinanceira.EM_ABERTO, null, null, null, 0);
	}

	public static ObrigacaoFinanceira reconstituir(Long id, Long usuarioId, String descricao, String credor,
			ValorMonetario valor, LocalDate dataVencimento, Long contaPagamentoId, Long categoriaId,
			StatusObrigacaoFinanceira status, LocalDate dataLiquidacao, Long transacaoId, LocalDateTime canceladaEm,
			long version) {
		ValorMonetario valorPago = status == StatusObrigacaoFinanceira.PAGA ? valor : ValorMonetario.zero();
		return new ObrigacaoFinanceira(id, usuarioId, descricao, credor, valor, valorPago, dataVencimento, contaPagamentoId,
				categoriaId, status, dataLiquidacao, transacaoId, canceladaEm, version);
	}

	public static ObrigacaoFinanceira reconstituir(Long id, Long usuarioId, String descricao, String credor,
			ValorMonetario valor, ValorMonetario valorPago, LocalDate dataVencimento, Long contaPagamentoId,
			Long categoriaId, StatusObrigacaoFinanceira status, LocalDate dataLiquidacao, Long transacaoId,
			LocalDateTime canceladaEm, long version) {
		return new ObrigacaoFinanceira(id, usuarioId, descricao, credor, valor, valorPago, dataVencimento,
				contaPagamentoId, categoriaId, status, dataLiquidacao, transacaoId, canceladaEm, version);
	}

	public ObrigacaoFinanceira liquidar(LocalDate dataLiquidacao, Long transacaoId) {
		if (status != StatusObrigacaoFinanceira.EM_ABERTO && status != StatusObrigacaoFinanceira.VENCIDA) {
			throw new DomainException("error.obrigacao.pagamento.invalido");
		}
		if (dataLiquidacao == null || transacaoId == null || transacaoId <= 0) {
			throw new DomainException("error.obrigacao.liquidacao.invalida");
		}
		return registrarPagamento(valor, dataLiquidacao, transacaoId);
	}

	public ObrigacaoFinanceira registrarPagamento(ValorMonetario pagamento, LocalDate dataPagamento, Long transacaoId) {
		if ((status != StatusObrigacaoFinanceira.EM_ABERTO && status != StatusObrigacaoFinanceira.VENCIDA)
				|| pagamento == null || pagamento.isZero() || dataPagamento == null || transacaoId == null || transacaoId <= 0
				|| valorPago.somar(pagamento).valor().compareTo(valor.valor()) > 0) {
			throw new DomainException("error.obrigacao.pagamento.invalido");
		}
		ValorMonetario novoValorPago = valorPago.somar(pagamento);
		boolean quitada = novoValorPago.valor().compareTo(valor.valor()) == 0;
		return new ObrigacaoFinanceira(id, usuarioId, descricao, credor, valor, novoValorPago, dataVencimento,
				contaPagamentoId, categoriaId, quitada ? StatusObrigacaoFinanceira.PAGA : status,
				quitada ? dataPagamento : null, transacaoId, null, version);
	}

	public ObrigacaoFinanceira estornarPagamento(ValorMonetario pagamento, LocalDate referencia) {
		if (pagamento == null || pagamento.isZero() || referencia == null
				|| pagamento.valor().compareTo(valorPago.valor()) > 0) {
			throw new DomainException("error.obrigacao.estorno.pagamento.invalido");
		}
		ValorMonetario novoValorPago = valorPago.subtrair(pagamento);
		StatusObrigacaoFinanceira novoStatus = dataVencimento.isBefore(referencia)
				? StatusObrigacaoFinanceira.VENCIDA : StatusObrigacaoFinanceira.EM_ABERTO;
		return new ObrigacaoFinanceira(id, usuarioId, descricao, credor, valor, novoValorPago, dataVencimento,
				contaPagamentoId, categoriaId, novoStatus, null, null, null, version);
	}

	public ObrigacaoFinanceira estornarPagamento(LocalDate referencia) {
		if (status != StatusObrigacaoFinanceira.PAGA || transacaoId == null || referencia == null) {
			throw new DomainException("error.obrigacao.estorno.pagamento.invalido");
		}
		return estornarPagamento(valorPago, referencia);
	}

	public ObrigacaoFinanceira cancelar(LocalDateTime momento) {
		if ((status != StatusObrigacaoFinanceira.EM_ABERTO && status != StatusObrigacaoFinanceira.VENCIDA)
				|| !valorPago.isZero()) {
			throw new DomainException("error.obrigacao.cancelamento.invalido");
		}
		if (momento == null) throw new DomainException("error.obrigacao.cancelamento.invalido");
		return new ObrigacaoFinanceira(id, usuarioId, descricao, credor, valor, valorPago, dataVencimento, contaPagamentoId,
				categoriaId, StatusObrigacaoFinanceira.CANCELADA, null, null, momento, version);
	}

	public ObrigacaoFinanceira marcarVencida(LocalDate referencia) {
		if (status != StatusObrigacaoFinanceira.EM_ABERTO || referencia == null || !dataVencimento.isBefore(referencia)) {
			return this;
		}
		return new ObrigacaoFinanceira(id, usuarioId, descricao, credor, valor, valorPago, dataVencimento, contaPagamentoId,
				categoriaId, StatusObrigacaoFinanceira.VENCIDA, null, null, null, version);
	}

	public Long getId() { return id; }
	public Long getUsuarioId() { return usuarioId; }
	public String getDescricao() { return descricao; }
	public String getCredor() { return credor; }
	public ValorMonetario getValor() { return valor; }
	public ValorMonetario getValorPago() { return valorPago; }
	public ValorMonetario getSaldoPendente() { return valor.subtrair(valorPago); }
	public LocalDate getDataVencimento() { return dataVencimento; }
	public Long getContaPagamentoId() { return contaPagamentoId; }
	public Long getCategoriaId() { return categoriaId; }
	public StatusObrigacaoFinanceira getStatus() { return status; }
	public LocalDate getDataLiquidacao() { return dataLiquidacao; }
	public Long getTransacaoId() { return transacaoId; }
	public LocalDateTime getCanceladaEm() { return canceladaEm; }
	public long getVersion() { return version; }
}
