package com.finisus.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.finisus.domain.DomainException;
import com.finisus.domain.vo.ValorMonetario;

public record PagamentoFatura(Long id, Long faturaId, Long usuarioId, Long transacaoId, Long contaId,
		ValorMonetario valor, ValorMonetario credito, LocalDate dataPagamento, String chaveIdempotencia,
		String hashRequisicao, LocalDateTime estornadoEm, long version) {
	public PagamentoFatura {
		if (faturaId == null || usuarioId == null || transacaoId == null || contaId == null || valor == null
				|| valor.isZero() || credito == null || credito.valor().compareTo(valor.valor()) > 0
				|| dataPagamento == null || chaveIdempotencia == null || chaveIdempotencia.isBlank()
				|| hashRequisicao == null || hashRequisicao.isBlank())
			throw new DomainException("error.fatura.pagamento.invalido");
	}

	public static PagamentoFatura novo(Long faturaId, Long usuarioId, Long transacaoId, Long contaId,
			ValorMonetario valor, ValorMonetario credito, LocalDate dataPagamento, String chaveIdempotencia,
			String hashRequisicao) {
		return new PagamentoFatura(null, faturaId, usuarioId, transacaoId, contaId, valor, credito, dataPagamento,
				chaveIdempotencia, hashRequisicao, null, 0);
	}

	public boolean ativo() { return estornadoEm == null; }

	public PagamentoFatura estornar(LocalDateTime momento) {
		if (!ativo() || momento == null) throw new DomainException("error.fatura.estorno.pagamento.invalido");
		return new PagamentoFatura(id, faturaId, usuarioId, transacaoId, contaId, valor, credito, dataPagamento,
				chaveIdempotencia, hashRequisicao, momento, version);
	}
}
