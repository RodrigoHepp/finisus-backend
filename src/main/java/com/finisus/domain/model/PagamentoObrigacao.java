package com.finisus.domain.model;

import com.finisus.domain.DomainException;
import com.finisus.domain.vo.ValorMonetario;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PagamentoObrigacao(Long id, Long obrigacaoId, Long usuarioId, Long transacaoId,
		ValorMonetario valor, ValorMonetario juros, ValorMonetario encargos, ValorMonetario desconto,
		LocalDate dataPagamento, LocalDateTime estornadoEm, long version) {
	public PagamentoObrigacao {
		if (obrigacaoId == null || obrigacaoId <= 0 || usuarioId == null || usuarioId <= 0 || transacaoId == null
				|| transacaoId <= 0 || valor == null || juros == null || encargos == null || desconto == null
				|| dataPagamento == null || valor.somar(desconto).isZero()) {
			throw new DomainException("error.obrigacao.pagamento.invalido");
		}
	}
	public PagamentoObrigacao(Long id, Long obrigacaoId, Long usuarioId, Long transacaoId,
			ValorMonetario valor, LocalDate dataPagamento, LocalDateTime estornadoEm, long version) {
		this(id, obrigacaoId, usuarioId, transacaoId, valor, ValorMonetario.zero(), ValorMonetario.zero(),
				ValorMonetario.zero(), dataPagamento, estornadoEm, version);
	}

	public static PagamentoObrigacao novo(Long obrigacaoId, Long usuarioId, Long transacaoId,
			ValorMonetario valor, ValorMonetario juros, ValorMonetario encargos, ValorMonetario desconto,
			LocalDate dataPagamento) {
		return new PagamentoObrigacao(null, obrigacaoId, usuarioId, transacaoId, valor, juros, encargos, desconto,
				dataPagamento, null, 0);
	}

	public PagamentoObrigacao estornar(LocalDateTime momento) {
		if (estornadoEm != null || momento == null) throw new DomainException("error.obrigacao.estorno.pagamento.invalido");
		return new PagamentoObrigacao(id, obrigacaoId, usuarioId, transacaoId, valor, juros, encargos, desconto,
				dataPagamento, momento, version);
	}

	public boolean ativo() { return estornadoEm == null; }
	public ValorMonetario valorAbatido() { return valor.somar(desconto); }
	public ValorMonetario valorCaixa() { return valor.somar(juros).somar(encargos); }
}
