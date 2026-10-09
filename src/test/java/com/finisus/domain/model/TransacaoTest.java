package com.finisus.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finisus.domain.DomainException;
import com.finisus.domain.vo.ValorMonetario;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TransacaoTest {
	@Test
	void recusaEstornoGenericoDePagamentoDeFatura() {
		Transacao pagamento = Transacao.pagamentoFatura(1L, ValorMonetario.of(new BigDecimal("200.00")),
				LocalDate.of(2026, 9, 23), "Pagamento", 2L, 3L);

		assertThatThrownBy(pagamento::validarEstornoGenerico)
				.isInstanceOf(DomainException.class)
				.hasMessage("error.transacao.origem.imutavel");
	}
}
