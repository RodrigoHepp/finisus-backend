package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.finisus.application.ports.in.CartaoCreditoUseCase;
import com.finisus.application.ports.in.CompraParceladaUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.CompraParceladaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.FaturaRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.CartaoCredito;
import com.finisus.domain.model.CompraParcelada;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.Fatura;
import com.finisus.domain.model.StatusFatura;
import com.finisus.domain.model.TipoConta;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.vo.AnoMes;
import com.finisus.domain.vo.ValorMonetario;

class CompraParceladaServiceTest {

	@Test
	void distribuiParcelasNasFaturasConformeDataDeFechamento() {
		CompraParceladaRepositoryPort compras = mock(CompraParceladaRepositoryPort.class);
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		FaturaRepositoryPort faturas = mock(FaturaRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		CartaoCreditoUseCase cartoes = mock(CartaoCreditoUseCase.class);
		var conta = Conta.reconstituir(30L, 1L, "Conta", TipoConta.FISICO, null, ValorMonetario.zero(), 0);
		var cartao = CartaoCredito.reconstituir(20L, 1L, "Principal",
				ValorMonetario.of(new BigDecimal("1000.00")), 10, 20, true);
		when(contas.buscarPorIdEUsuario(30L, 1L)).thenReturn(Optional.of(conta));
		when(cartoes.buscarCartao(1L, 20L)).thenReturn(cartao);
		when(compras.salvar(any())).thenAnswer(invocation -> {
			CompraParcelada c = invocation.getArgument(0);
			return CompraParcelada.reconstituir(40L, c.getUsuarioId(), c.getDescricao(), c.getValorTotal(),
					c.getNumeroParcelas(), c.getDataCompra(), c.getCategoriaId(), c.getContaId(), c.getCartaoId(), null);
		});
		Map<AnoMes, Fatura> porCompetencia = new HashMap<>();
		when(faturas.buscarPorCartaoEMes(any(), any())).thenAnswer(invocation ->
				Optional.ofNullable(porCompetencia.get(invocation.getArgument(1))));
		AtomicLong sequenciaFatura = new AtomicLong(100);
		when(faturas.salvar(any())).thenAnswer(invocation -> {
			Fatura f = invocation.getArgument(0);
			Fatura salva = Fatura.reconstituir(sequenciaFatura.getAndIncrement(), f.getCartaoId(), f.getMesReferencia(),
					f.getDataFechamento(), f.getDataVencimento(), f.getStatus(), f.getContaPagamentoId(), 0);
			porCompetencia.put(salva.getMesReferencia(), salva);
			return salva;
		});
		List<Transacao> parcelas = new ArrayList<>();
		when(transacoes.salvar(any())).thenAnswer(invocation -> {
			Transacao t = invocation.getArgument(0);
			parcelas.add(t);
			return t;
		});
		when(transacoes.listarPorFatura(any())).thenReturn(List.of());
		ObterDataAtualPort relogio = mock(ObterDataAtualPort.class);
		when(relogio.obterDataHora()).thenReturn(LocalDateTime.of(2026, 1, 15, 10, 0));
		var service = new CompraParceladaService(compras, contas, mock(CategoriaRepositoryPort.class), transacoes,
				faturas, cartoes, relogio);

		CompraParcelada criada = service.criar(1L, new CompraParceladaUseCase.CriarCommand("Notebook",
				new BigDecimal("300.00"), 3, LocalDate.of(2026, 1, 15), null, 30L, 20L));

		assertThat(criada.getCartaoId()).isEqualTo(20L);
		assertThat(porCompetencia.keySet()).containsExactlyInAnyOrder(AnoMes.parse("2026-02"),
				AnoMes.parse("2026-03"), AnoMes.parse("2026-04"));
		assertThat(parcelas).extracting(Transacao::getCompraParceladaId).containsOnly(40L);
		assertThat(parcelas).extracting(Transacao::getFaturaId).containsExactly(100L, 101L, 102L);
		assertThat(parcelas).extracting(t -> t.getValor().valor()).containsExactly(new BigDecimal("100.00"),
				new BigDecimal("100.00"), new BigDecimal("100.00"));
	}

	@Test
	void rejeitaParcelaQuandoAFaturaDaCompetenciaJaEstaFechada() {
		CompraParceladaRepositoryPort compras = mock(CompraParceladaRepositoryPort.class);
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		FaturaRepositoryPort faturas = mock(FaturaRepositoryPort.class);
		CartaoCreditoUseCase cartoes = mock(CartaoCreditoUseCase.class);
		when(contas.buscarPorIdEUsuario(30L, 1L)).thenReturn(Optional.of(Conta.reconstituir(30L, 1L, "Conta",
				TipoConta.FISICO, null, ValorMonetario.zero(), 0)));
		when(cartoes.buscarCartao(1L, 20L)).thenReturn(CartaoCredito.reconstituir(20L, 1L, "Principal",
				ValorMonetario.of(new BigDecimal("1000.00")), 20, 25, true));
		when(compras.salvar(any())).thenReturn(CompraParcelada.reconstituir(40L, 1L, "Notebook",
				ValorMonetario.of(new BigDecimal("100.00")), 1, LocalDate.of(2026, 1, 10), null, 30L, 20L, null));
		when(faturas.buscarPorCartaoEMes(20L, AnoMes.parse("2026-01"))).thenReturn(Optional.of(Fatura.reconstituir(
				100L, 20L, AnoMes.parse("2026-01"), LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 25),
				StatusFatura.FECHADA, 30L, 0)));
		var service = new CompraParceladaService(compras, contas, mock(CategoriaRepositoryPort.class),
				mock(TransacaoRepositoryPort.class), faturas, cartoes, mock(ObterDataAtualPort.class));

		assertThatThrownBy(() -> service.criar(1L, new CompraParceladaUseCase.CriarCommand("Notebook",
				new BigDecimal("100.00"), 1, LocalDate.of(2026, 1, 10), null, 30L, 20L)))
				.isInstanceOf(DomainException.class).hasMessage("error.fatura.fechada");
	}

	@Test
	void cancelaParcelaPassadaQuandoElaAindaPertenceAFaturaAberta() {
		CompraParceladaRepositoryPort compras = mock(CompraParceladaRepositoryPort.class);
		FaturaRepositoryPort faturas = mock(FaturaRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		ObterDataAtualPort relogio = mock(ObterDataAtualPort.class);
		CompraParcelada compra = CompraParcelada.reconstituir(40L, 1L, "Notebook",
				ValorMonetario.of(new BigDecimal("100.00")), 1, LocalDate.of(2026, 1, 15), null, 30L, 20L, null);
		Transacao parcela = Transacao.geradaPorCompraParcelada(1L, ValorMonetario.of(new BigDecimal("100.00")),
				LocalDate.of(2026, 1, 15), "Notebook (1/1)", 30L, null, 40L, 100L);
		when(compras.buscarPorIdEUsuario(40L, 1L)).thenReturn(Optional.of(compra));
		when(compras.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(transacoes.listarPorCompraParcelada(40L)).thenReturn(List.of(parcela));
		when(transacoes.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(faturas.buscarPorId(100L)).thenReturn(Optional.of(Fatura.reconstituir(100L, 20L,
				AnoMes.parse("2026-02"), LocalDate.of(2026, 2, 10), LocalDate.of(2026, 2, 20),
				StatusFatura.ABERTA, 30L, 0)));
		when(relogio.obterDataHora()).thenReturn(LocalDateTime.of(2026, 1, 20, 10, 0));
		var service = new CompraParceladaService(compras, mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), transacoes, faturas, mock(CartaoCreditoUseCase.class), relogio);

		service.cancelar(1L, 40L);

		assertThat(parcela.isEstornada()).isFalse();
		org.mockito.Mockito.verify(transacoes).salvar(org.mockito.ArgumentMatchers.argThat(Transacao::isEstornada));
	}
}
