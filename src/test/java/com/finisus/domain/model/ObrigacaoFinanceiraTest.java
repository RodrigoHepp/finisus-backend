package com.finisus.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finisus.domain.DomainException;
import com.finisus.domain.vo.ValorMonetario;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ObrigacaoFinanceiraTest {
	@Test
	void liquidaObrigacaoAbertaSemAnteciparMovimentacaoDeCaixa() {
		ObrigacaoFinanceira obrigacao = ObrigacaoFinanceira.nova(1L, "Internet", "Provedor",
				ValorMonetario.of(new BigDecimal("120.00")), LocalDate.of(2026, 9, 10), 2L, 3L);

		ObrigacaoFinanceira paga = obrigacao.liquidar(LocalDate.of(2026, 9, 10), 99L);

		assertThat(obrigacao.getStatus()).isEqualTo(StatusObrigacaoFinanceira.EM_ABERTO);
		assertThat(paga.getStatus()).isEqualTo(StatusObrigacaoFinanceira.PAGA);
		assertThat(paga.getTransacaoId()).isEqualTo(99L);
		assertThatThrownBy(() -> paga.cancelar(java.time.LocalDateTime.now())).isInstanceOf(DomainException.class)
				.hasMessage("error.obrigacao.cancelamento.invalido");
	}

	@Test
	void estornoDePagamentoReabreObrigacaoVencidaELimpaLiquidacao() {
		ObrigacaoFinanceira paga = ObrigacaoFinanceira.nova(1L, "Internet", "Provedor",
				ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 9, 10), 2L, null)
				.liquidar(LocalDate.of(2026, 9, 9), 99L);

		ObrigacaoFinanceira reaberta = paga.estornarPagamento(LocalDate.of(2026, 9, 23));

		assertThat(reaberta.getStatus()).isEqualTo(StatusObrigacaoFinanceira.VENCIDA);
		assertThat(reaberta.getTransacaoId()).isNull();
		assertThat(reaberta.getDataLiquidacao()).isNull();
	}

	@Test
	void aceitaPagamentosParciaisSemPermitirExcessoOuCancelamentoComCaixaRealizado() {
		ObrigacaoFinanceira aberta = ObrigacaoFinanceira.nova(1L, "Internet", "Provedor",
				ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 9, 30), 2L, null);

		ObrigacaoFinanceira parcial = aberta.registrarPagamento(ValorMonetario.of(new BigDecimal("35.00")),
				LocalDate.of(2026, 9, 20), 90L);

		assertThat(parcial.getStatus()).isEqualTo(StatusObrigacaoFinanceira.EM_ABERTO);
		assertThat(parcial.getSaldoPendente().valor()).isEqualByComparingTo("65.00");
		assertThatThrownBy(() -> parcial.registrarPagamento(ValorMonetario.of(new BigDecimal("65.01")),
				LocalDate.of(2026, 9, 21), 91L)).hasMessage("error.obrigacao.pagamento.invalido");
		assertThatThrownBy(() -> parcial.cancelar(java.time.LocalDateTime.now()))
				.hasMessage("error.obrigacao.cancelamento.invalido");
	}

	@Test
	void pagamentoDetalhadoDistingueAbatimentoEValorDeCaixa() {
		PagamentoObrigacao pagamento = PagamentoObrigacao.novo(10L, 1L, 20L,
				ValorMonetario.of(new BigDecimal("90.00")), ValorMonetario.of(new BigDecimal("5.00")),
				ValorMonetario.of(new BigDecimal("2.00")), ValorMonetario.of(new BigDecimal("10.00")),
				LocalDate.of(2026, 9, 20));

		assertThat(pagamento.valorAbatido().valor()).isEqualByComparingTo("100.00");
		assertThat(pagamento.valorCaixa().valor()).isEqualByComparingTo("97.00");
	}
}
