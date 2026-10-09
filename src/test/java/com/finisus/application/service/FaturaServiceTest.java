package com.finisus.application.service;

import com.finisus.application.ports.in.CartaoCreditoUseCase;
import com.finisus.application.ports.in.EstornarTransacaoVinculadaUseCase;
import com.finisus.application.ports.in.FaturaUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.AplicacaoCreditoFaturaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.FaturaRepositoryPort;
import com.finisus.application.ports.out.ItemRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.PagamentoFaturaRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.domain.model.Fatura;
import com.finisus.domain.model.StatusFatura;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.vo.AnoMes;
import com.finisus.domain.vo.ValorMonetario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

class FaturaServiceTest {

	@Test
	void calculaValoresEmAbertoEmLoteSemConsultarCadaFaturaIndividualmente() {
		FaturaRepositoryPort faturas = mock(FaturaRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		PagamentoFaturaRepositoryPort pagamentos = mock(PagamentoFaturaRepositoryPort.class);
		AplicacaoCreditoFaturaRepositoryPort aplicacoes = mock(AplicacaoCreditoFaturaRepositoryPort.class);
		Fatura primeira = Fatura.reconstituir(10L, 20L, AnoMes.parse("2026-07"), LocalDate.of(2026, 7, 20),
				LocalDate.of(2026, 7, 30), StatusFatura.ABERTA, 30L, 0);
		Fatura segunda = Fatura.reconstituir(11L, 20L, AnoMes.parse("2026-08"), LocalDate.of(2026, 8, 20),
				LocalDate.of(2026, 8, 30), StatusFatura.ABERTA, 30L, 0);
		Transacao gastoPrimeira = Transacao.gastoCartao(1L, ValorMonetario.of(new BigDecimal("100.00")),
				LocalDate.of(2026, 7, 10), "Compra", 30L, null, 10L, List.of());
		Transacao creditoPrimeira = Transacao.creditoFatura(1L, ValorMonetario.of(new BigDecimal("20.00")),
				LocalDate.of(2026, 7, 11), "Crédito", 30L, null, 10L, List.of());
		Transacao gastoSegunda = Transacao.gastoCartao(1L, ValorMonetario.of(new BigDecimal("80.00")),
				LocalDate.of(2026, 8, 10), "Compra", 30L, null, 11L, List.of());
		var pagamento = new com.finisus.domain.model.PagamentoFatura(1L, 10L, 1L, 90L, 30L,
				ValorMonetario.of(new BigDecimal("30.00")), ValorMonetario.zero(), LocalDate.of(2026, 7, 30),
				"pagamento-julho", "hash", null, 0);
		when(faturas.listarEmAbertoPorUsuario(1L)).thenReturn(List.of(primeira, segunda));
		when(transacoes.listarPorFaturas(List.of(10L, 11L)))
				.thenReturn(Map.of(10L, List.of(gastoPrimeira, creditoPrimeira), 11L, List.of(gastoSegunda)));
		when(pagamentos.listarPorFaturasEUsuario(List.of(10L, 11L), 1L)).thenReturn(Map.of(10L, List.of(pagamento)));
		when(aplicacoes.somarPorFaturasDestino(List.of(10L, 11L)))
				.thenReturn(Map.of(10L, new BigDecimal("10.00"), 11L, new BigDecimal("10.00")));
		FaturaService service = new FaturaService(faturas, mock(CartaoCreditoUseCase.class),
				mock(ContaRepositoryPort.class), mock(CategoriaRepositoryPort.class), mock(ItemRepositoryPort.class),
				transacoes, mock(ObterDataAtualPort.class), mock(EstornarTransacaoVinculadaUseCase.class), pagamentos,
				aplicacoes);

		Map<Long, BigDecimal> valores = service.buscarValoresEmAberto(1L, List.of(10L, 11L));

		assertThat(valores).containsEntry(10L, new BigDecimal("40.00"))
				.containsEntry(11L, new BigDecimal("70.00"));
		verify(transacoes, never()).listarPorFatura(anyLong());
		verify(pagamentos, never()).listarPorFaturaEUsuario(anyLong(), anyLong());
		verify(aplicacoes, never()).somarPorFaturaDestino(anyLong());
	}

	@Test
	void aplicaCreditoAnteriorAoFecharFaturaSemNovoMovimentoDeCaixa() {
		FaturaRepositoryPort faturas = mock(FaturaRepositoryPort.class);
		CartaoCreditoUseCase cartoes = mock(CartaoCreditoUseCase.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		PagamentoFaturaRepositoryPort pagamentos = mock(PagamentoFaturaRepositoryPort.class);
		AplicacaoCreditoFaturaRepositoryPort aplicacoes = mock(AplicacaoCreditoFaturaRepositoryPort.class);
		ObterDataAtualPort relogio = mock(ObterDataAtualPort.class);
		Fatura anterior = Fatura.reconstituir(9L, 20L, AnoMes.parse("2026-06"), LocalDate.of(2026, 6, 20),
				LocalDate.of(2026, 6, 30), StatusFatura.PAGA, 30L, 0);
		Fatura atual = Fatura.reconstituir(10L, 20L, AnoMes.parse("2026-07"), LocalDate.of(2026, 7, 20),
				LocalDate.of(2026, 7, 30), StatusFatura.ABERTA, 30L, 0);
		var credito = new com.finisus.domain.model.PagamentoFatura(1L, 9L, 1L, 90L, 30L,
				ValorMonetario.of(new BigDecimal("130.00")), ValorMonetario.of(new BigDecimal("30.00")),
				LocalDate.of(2026, 6, 30), "credito-junho", "hash", null, 0);
		when(faturas.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(atual));
		when(faturas.listarPorCartao(20L)).thenReturn(List.of(anterior, atual));
		when(transacoes.listarPorFatura(10L)).thenReturn(List.of(Transacao.gastoCartao(1L,
				ValorMonetario.of(new BigDecimal("20.00")), LocalDate.of(2026, 7, 5), "Compra", 30L, null, 10L,
				List.of())));
		when(pagamentos.listarPorFaturaEUsuario(9L, 1L)).thenReturn(List.of(credito));
		when(aplicacoes.somarPorFaturaDestino(10L)).thenReturn(BigDecimal.ZERO);
		when(aplicacoes.somarPorPagamentoOrigem(1L)).thenReturn(BigDecimal.ZERO);
		when(relogio.obterDataHora()).thenReturn(java.time.LocalDateTime.of(2026, 7, 20, 10, 0));
		when(faturas.salvar(atual)).thenReturn(atual);
		FaturaService service = new FaturaService(faturas, cartoes, mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), mock(ItemRepositoryPort.class), transacoes, relogio,
				mock(EstornarTransacaoVinculadaUseCase.class), pagamentos, aplicacoes);

		Fatura fechada = service.fechar(1L, 10L);

		assertThat(fechada.getStatus()).isEqualTo(StatusFatura.PAGA);
		var captor = org.mockito.ArgumentCaptor.forClass(com.finisus.domain.model.AplicacaoCreditoFatura.class);
		verify(aplicacoes).salvar(captor.capture());
		assertThat(captor.getValue().valor().valor()).isEqualByComparingTo("20.00");
	}

	@Test
	void registraPagamentosParciaisCreditoERepeticaoIdempotente() {
		FaturaRepositoryPort faturas = mock(FaturaRepositoryPort.class);
		CartaoCreditoUseCase cartoes = mock(CartaoCreditoUseCase.class);
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		PagamentoFaturaRepositoryPort pagamentos = mock(PagamentoFaturaRepositoryPort.class);
		ObterDataAtualPort relogio = mock(ObterDataAtualPort.class);
		Fatura fatura = Fatura.reconstituir(10L, 20L, AnoMes.parse("2026-07"), LocalDate.of(2026, 7, 20),
				LocalDate.of(2026, 7, 30), StatusFatura.FECHADA, 30L, 0);
		var conta = com.finisus.domain.model.Conta.reconstituir(30L, 1L, "Conta",
				com.finisus.domain.model.TipoConta.FISICO, null, ValorMonetario.of(new BigDecimal("500.00")), 0);
		Transacao gasto = Transacao.gastoCartao(1L, ValorMonetario.of(new BigDecimal("100.00")),
				LocalDate.of(2026, 7, 10), "Compra", 30L, null, 10L, List.of());
		when(faturas.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(fatura));
		when(faturas.buscarPorId(10L)).thenReturn(Optional.of(fatura));
		when(contas.buscarPorIdEUsuario(30L, 1L)).thenReturn(Optional.of(conta));
		when(transacoes.listarPorFatura(10L)).thenReturn(List.of(gasto));
		when(relogio.obterDataHora()).thenReturn(java.time.LocalDateTime.of(2026, 7, 30, 10, 0));
		java.util.ArrayList<com.finisus.domain.model.PagamentoFatura> registrados = new java.util.ArrayList<>();
		when(pagamentos.listarPorFaturaEUsuario(10L, 1L)).thenAnswer(i -> List.copyOf(registrados));
		when(pagamentos.buscarPorUsuarioEChave(any(), any())).thenAnswer(i -> registrados.stream()
				.filter(p -> p.chaveIdempotencia().equals(i.getArgument(1))).findFirst());
		java.util.concurrent.atomic.AtomicLong transacaoId = new java.util.concurrent.atomic.AtomicLong(90);
		when(transacoes.salvar(any())).thenAnswer(i -> {
			Transacao t = i.getArgument(0);
			return Transacao.reconstituir(transacaoId.getAndIncrement(), t.getUsuarioId(), t.getTipo(), t.getValor(),
					t.getData(), t.getDescricao(), t.getContaId(), null, null, null, t.getFaturaPagamentoId(), null,
					null, null, 0, List.of());
		});
		java.util.concurrent.atomic.AtomicLong pagamentoId = new java.util.concurrent.atomic.AtomicLong(1);
		when(pagamentos.salvar(any())).thenAnswer(i -> {
			var p = (com.finisus.domain.model.PagamentoFatura) i.getArgument(0);
			var salvo = new com.finisus.domain.model.PagamentoFatura(p.id() == null ? pagamentoId.getAndIncrement() : p.id(),
					p.faturaId(), p.usuarioId(), p.transacaoId(), p.contaId(), p.valor(), p.credito(), p.dataPagamento(),
					p.chaveIdempotencia(), p.hashRequisicao(), p.estornadoEm(), p.version());
			registrados.removeIf(atual -> atual.id().equals(salvo.id())); registrados.add(salvo); return salvo;
		});
		when(faturas.salvar(fatura)).thenReturn(fatura);
		FaturaService service = new FaturaService(faturas, cartoes, contas, mock(CategoriaRepositoryPort.class),
				mock(ItemRepositoryPort.class), transacoes, relogio, mock(EstornarTransacaoVinculadaUseCase.class), pagamentos,
				mock(AplicacaoCreditoFaturaRepositoryPort.class));

		Fatura parcial = service.pagar(1L, 10L, "pagamento-1",
				new FaturaUseCase.PagamentoCommand(new BigDecimal("40.00"), LocalDate.of(2026, 7, 30), null));
		StatusFatura statusParcial = parcial.getStatus();
		Fatura quitada = service.pagar(1L, 10L, "pagamento-2",
				new FaturaUseCase.PagamentoCommand(new BigDecimal("70.00"), LocalDate.of(2026, 7, 31), null));
		Fatura repetida = service.pagar(1L, 10L, "pagamento-2",
				new FaturaUseCase.PagamentoCommand(new BigDecimal("70.00"), LocalDate.of(2026, 7, 31), null));

		assertThat(statusParcial).isEqualTo(StatusFatura.FECHADA);
		assertThat(quitada.getStatus()).isEqualTo(StatusFatura.PAGA);
		assertThat(repetida.getStatus()).isEqualTo(StatusFatura.PAGA);
		assertThat(registrados).extracting(p -> p.credito().valor())
				.containsExactly(new BigDecimal("0.00"), new BigDecimal("10.00"));
		assertThat(conta.getSaldo().valor()).isEqualByComparingTo("390.00");
		verify(transacoes, times(2)).salvar(any());
	}

	@Test
	void fechaCiclosVencidosECriaProximaCompetenciaIdempotentemente() {
		FaturaRepositoryPort faturas = mock(FaturaRepositoryPort.class);
		CartaoCreditoUseCase cartoes = mock(CartaoCreditoUseCase.class);
		Fatura janeiro = Fatura.reconstituir(10L, 20L, AnoMes.parse("2026-01"), LocalDate.of(2026, 1, 31),
				LocalDate.of(2026, 2, 5), StatusFatura.ABERTA, 30L, 0);
		var cartao = com.finisus.domain.model.CartaoCredito.reconstituir(20L, 1L, "Principal",
				ValorMonetario.of(new BigDecimal("5000.00")), 31, 5, true);
		when(faturas.listarAbertasParaFechamento(1L, LocalDate.of(2026, 2, 1)))
				.thenReturn(List.of(janeiro), List.of());
		when(cartoes.buscarCartao(1L, 20L)).thenReturn(cartao);
		when(faturas.buscarPorCartaoEMes(20L, AnoMes.parse("2026-02"))).thenReturn(Optional.empty());
		when(faturas.salvar(any(Fatura.class))).thenAnswer(invocation -> invocation.getArgument(0));
		FaturaService service = new FaturaService(faturas, cartoes, mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), mock(ItemRepositoryPort.class), mock(TransacaoRepositoryPort.class),
				mock(ObterDataAtualPort.class), mock(EstornarTransacaoVinculadaUseCase.class),
				mock(PagamentoFaturaRepositoryPort.class), mock(AplicacaoCreditoFaturaRepositoryPort.class));

		var resultado = service.processarCiclos(1L, LocalDate.of(2026, 2, 1));

		assertThat(resultado.fechadas()).hasSize(1);
		assertThat(resultado.fechadas().getFirst().getStatus()).isEqualTo(StatusFatura.FECHADA);
		assertThat(resultado.criadas()).hasSize(1);
		assertThat(resultado.criadas().getFirst().getMesReferencia()).isEqualTo(AnoMes.parse("2026-02"));
		assertThat(resultado.criadas().getFirst().getDataFechamento()).isEqualTo(LocalDate.of(2026, 2, 28));
		assertThat(resultado.criadas().getFirst().getDataVencimento()).isEqualTo(LocalDate.of(2026, 3, 5));
	}

	@Test
	void detalhaFaturaComCreditoEExcluiEstornosDoTotal() {
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
		Transacao credito = Transacao.creditoFatura(1L, ValorMonetario.of(new BigDecimal("25.00")),
				LocalDate.of(2026, 7, 12), "Estorno do mercado", 30L, null, 10L, List.of());
		when(faturas.buscarPorId(10L)).thenReturn(Optional.of(fatura));
		when(transacoes.listarPorFatura(10L)).thenReturn(List.of(gastoAtivo, gastoEstornado, credito));
		when(transacoes.listarPagamentosPorFatura(10L)).thenReturn(List.of());

		FaturaService service = new FaturaService(faturas, cartoes, mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), mock(ItemRepositoryPort.class), transacoes,
				mock(ObterDataAtualPort.class), mock(EstornarTransacaoVinculadaUseCase.class),
				mock(PagamentoFaturaRepositoryPort.class), mock(AplicacaoCreditoFaturaRepositoryPort.class));

		var detalhe = service.buscarDetalhe(1L, 10L);

		assertThat(detalhe.transacoes()).containsExactly(gastoAtivo, gastoEstornado, credito);
		assertThat(detalhe.valorTotal()).isEqualByComparingTo("55.00");
		assertThat(detalhe.valorPago()).isEqualByComparingTo("0.00");
		assertThat(detalhe.valorEmAberto()).isEqualByComparingTo("55.00");
	}

	@Test
	void estornaPagamentoVinculadoEReabreFatura() {
		FaturaRepositoryPort faturas = mock(FaturaRepositoryPort.class);
		CartaoCreditoUseCase cartoes = mock(CartaoCreditoUseCase.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		EstornarTransacaoVinculadaUseCase estornos = mock(EstornarTransacaoVinculadaUseCase.class);
		Fatura paga = Fatura.reconstituir(10L, 20L, AnoMes.parse("2026-07"), LocalDate.of(2026, 7, 20),
				LocalDate.of(2026, 7, 30), StatusFatura.PAGA, 30L, 0);
		Transacao pagamento = Transacao.reconstituir(99L, 1L, com.finisus.domain.model.TipoTransacao.SAIDA,
				ValorMonetario.of(new BigDecimal("80.00")), LocalDate.of(2026, 7, 30), "Pagamento", 30L,
				null, null, null, 10L, null, null, null, 0, List.of());
		when(faturas.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(paga));
		when(transacoes.listarPagamentosPorFatura(10L)).thenReturn(List.of(pagamento));
		when(transacoes.listarPorFatura(10L)).thenReturn(List.of(Transacao.gastoCartao(1L,
				ValorMonetario.of(new BigDecimal("80.00")), LocalDate.of(2026, 7, 10), "Compra", 30L, null, 10L,
				List.of())));
		when(faturas.salvar(paga)).thenReturn(paga);
		var pagamentos = mock(PagamentoFaturaRepositoryPort.class);
		when(pagamentos.listarPorFaturaEUsuario(10L, 1L)).thenReturn(List.of(new com.finisus.domain.model.PagamentoFatura(
				1L, 10L, 1L, 99L, 30L, ValorMonetario.of(new BigDecimal("80.00")), ValorMonetario.zero(),
				LocalDate.of(2026, 7, 30), "legado-99", "LEGADO", null, 0)));
		ObterDataAtualPort relogio = mock(ObterDataAtualPort.class);
		when(relogio.obterDataHora()).thenReturn(java.time.LocalDateTime.of(2026, 8, 1, 10, 0));
		FaturaService service = new FaturaService(faturas, cartoes, mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), mock(ItemRepositoryPort.class), transacoes,
				relogio, estornos, pagamentos, mock(AplicacaoCreditoFaturaRepositoryPort.class));

		Fatura reaberta = service.estornarPagamento(1L, 10L);

		verify(estornos).estornarVinculada(1L, 99L);
		assertThat(reaberta.getStatus()).isEqualTo(StatusFatura.FECHADA);
	}
}
