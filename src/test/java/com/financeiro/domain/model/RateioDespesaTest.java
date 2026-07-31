package com.financeiro.domain.model;

import com.financeiro.domain.RateioInvalidoException;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.vo.Email;
import com.financeiro.domain.vo.ValorMonetario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateioDespesaTest {
	@Test
	void rejeitaBaseDeCalculoAmbigua() {
		assertThrows(RateioInvalidoException.class,
				() -> RateioDespesa.interno(1L, 2L, ValorMonetario.of(BigDecimal.TEN), BigDecimal.TEN));
	}

	@Test
	void normalizaEmailIndependenteDoLocalePadrao() {
		Locale anterior = Locale.getDefault();
		try {
			Locale.setDefault(Locale.forLanguageTag("tr-TR"));
			assertEquals("info@example.com", new Email("INFO@EXAMPLE.COM").valor());
		} finally {
			Locale.setDefault(anterior);
		}
	}

	@Test
	void rejeitaAlvoDeItemSemItemDaTransacao() {
		DomainException exception = assertThrows(DomainException.class, () -> DespesaCompartilhada.reconstituir(1L, 2L,
				null, 3L, TipoRateio.VALOR_FIXO, TipoAlvoCompartilhamento.ITEM_TRANSACAO));
		assertEquals("error.compartilhamento.alvo.invalido", exception.getMessageKey());
	}

	@Test
	void cancelamentoDaDespesaEfeitoNosRateiosNaoPagos() {
		DespesaCompartilhada despesa = DespesaCompartilhada.reconstituir(1L, 2L, null, 3L, TipoRateio.VALOR_FIXO,
				TipoAlvoCompartilhamento.TRANSACAO);
		RateioDespesa pendente = RateioDespesa.interno(1L, 4L, ValorMonetario.of(BigDecimal.TEN), null);

		despesa.cancelar(LocalDateTime.of(2026, 7, 31, 10, 0));
		pendente.cancelar();

		assertEquals(StatusDespesaCompartilhada.CANCELADA, despesa.getStatus());
		assertEquals(StatusRateio.CANCELADO, pendente.getStatus());
	}
}
