package com.finisus.domain.model;

import com.finisus.domain.FinanciamentoInvalidoException;
import com.finisus.domain.vo.ValorMonetario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinanciamentoEParcelaTest {
	@Test
	void rejeitaTaxaNegativaEValorPrincipalNaoPositivo() {
		assertThrows(FinanciamentoInvalidoException.class, () -> Financiamento.novo(1L, "Veículo",
				ValorMonetario.zero(), BigDecimal.ZERO, 12, LocalDate.of(2026, 1, 1), 2L));
		assertThrows(FinanciamentoInvalidoException.class, () -> Financiamento.novo(1L, "Veículo",
				ValorMonetario.of(BigDecimal.ONE), new BigDecimal("-0.01"), 12, LocalDate.of(2026, 1, 1), 2L));
	}

	@Test
	void marcaAtrasoSomenteContraDataDeReferenciaRecebida() {
		ParcelaFinanciamento parcela = ParcelaFinanciamento.nova(1L, 1, ValorMonetario.of(BigDecimal.TEN),
				LocalDate.of(2026, 1, 10));

		parcela.marcarAtrasada(LocalDate.of(2026, 1, 10));
		assertEquals(StatusParcelaFinanciamento.PENDENTE, parcela.getStatus());

		parcela.marcarAtrasada(LocalDate.of(2026, 1, 11));
		assertEquals(StatusParcelaFinanciamento.ATRASADA, parcela.getStatus());
	}

	@Test
	void permiteCancelarSomenteFinanciamentoAtivo() {
		Financiamento ativo = Financiamento.novo(1L, "Veículo", ValorMonetario.of(BigDecimal.TEN), BigDecimal.ZERO, 12,
				LocalDate.of(2026, 1, 1), 2L);
		Financiamento cancelado = ativo.cancelar(LocalDateTime.of(2026, 7, 31, 10, 0));

		assertEquals(StatusFinanciamento.CANCELADO, cancelado.getStatus());
		assertThrows(com.finisus.domain.DomainException.class,
				() -> cancelado.cancelar(LocalDateTime.of(2026, 7, 31, 10, 1)));
	}
}
