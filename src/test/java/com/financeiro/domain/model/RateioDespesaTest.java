package com.financeiro.domain.model;

import com.financeiro.domain.RateioInvalidoException;
import com.financeiro.domain.vo.Email;
import com.financeiro.domain.vo.ValorMonetario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateioDespesaTest {
    @Test
    void rejeitaBaseDeCalculoAmbigua() {
        assertThrows(RateioInvalidoException.class, () -> RateioDespesa.interno(
                1L, 2L, ValorMonetario.of(BigDecimal.TEN), BigDecimal.TEN));
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
}
