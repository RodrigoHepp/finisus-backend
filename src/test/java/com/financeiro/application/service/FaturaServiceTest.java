package com.financeiro.application.service;

import com.financeiro.application.ports.in.CartaoCreditoUseCase;
import com.financeiro.application.ports.out.CategoriaRepositoryPort;
import com.financeiro.application.ports.out.ContaRepositoryPort;
import com.financeiro.application.ports.out.FaturaRepositoryPort;
import com.financeiro.application.ports.out.ItemRepositoryPort;
import com.financeiro.application.ports.out.ObterDataAtualPort;
import com.financeiro.application.ports.out.TransacaoRepositoryPort;
import com.financeiro.domain.model.Fatura;
import com.financeiro.domain.model.StatusFatura;
import com.financeiro.domain.model.Transacao;
import com.financeiro.domain.vo.AnoMes;
import com.financeiro.domain.vo.ValorMonetario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FaturaServiceTest {

	@Test
	void detalhaFaturaComTransacoesEExcluiEstornosDoTotal() {
		FaturaRepositoryPort faturas = mock(FaturaRepositoryPort.class);
		CartaoCreditoUseCase cartoes = mock(CartaoCreditoUseCase.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		Fatura fatura = Fatura.reconstituir(10L, 20L, AnoMes.parse("2026-07"), LocalDate.of(2026, 7, 20),
				LocalDate.of(2026, 7, 30), StatusFatura.ABERTA, 30L, 0);
		Transacao gastoAtivo = Transacao.gastoCartao(1L, ValorMonetario.of(new BigDecimal("80.00")),
				LocalDate.of(2026, 7, 10), "Mercado", 30L, null, 10L, List.of());
		Transacao gastoEstornado = Transacao.gastoCartao(1L, ValorMonetario.of(new BigDecimal("20.00")),
				LocalDate.of(2026, 7, 11), "Farmácia", 30L, null, 10L, List.of())
				.estornada(java.time.LocalDateTime.of(2026, 7, 12, 10, 0));
		when(faturas.buscarPorId(10L)).thenReturn(Optional.of(fatura));
		when(transacoes.listarPorFatura(10L)).thenReturn(List.of(gastoAtivo, gastoEstornado));

		FaturaService service = new FaturaService(faturas, cartoes, mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), mock(ItemRepositoryPort.class), transacoes,
				mock(ObterDataAtualPort.class));

		var detalhe = service.buscarDetalhe(1L, 10L);

		assertThat(detalhe.transacoes()).containsExactly(gastoAtivo, gastoEstornado);
		assertThat(detalhe.valorTotal()).isEqualByComparingTo("80.00");
	}
}
