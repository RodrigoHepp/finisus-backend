package com.finisus.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finisus.domain.DomainException;
import com.finisus.domain.vo.ValorMonetario;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PosicaoInvestimentoTest {
	@Test
	void rejeitaInvestimentoOuValorInvalidos() {
		assertThatThrownBy(() -> PosicaoInvestimento.nova(0L, ValorMonetario.of(BigDecimal.ZERO),
				LocalDate.of(2026, 9, 1))).isInstanceOf(DomainException.class)
				.hasMessage("error.posicao.investimento.invalida");
	}
}
