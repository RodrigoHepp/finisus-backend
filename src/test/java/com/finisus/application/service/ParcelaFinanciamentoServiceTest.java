package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.ArgumentCaptor;

import com.finisus.application.ports.in.EstornarTransacaoVinculadaUseCase;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.FinanciamentoRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.ParcelaFinanciamentoRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.domain.model.Financiamento;
import com.finisus.domain.model.ModalidadeAmortizacaoFinanciamento;
import com.finisus.domain.model.ParcelaFinanciamento;
import com.finisus.domain.model.StatusParcelaFinanciamento;
import com.finisus.domain.model.TipoInvestimento;
import com.finisus.domain.model.TipoConta;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.vo.ValorMonetario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ParcelaFinanciamentoServiceTest {
	@Test
	void reduzPrazoMantendoPrestacaoSemExigirQuantidadeManual() {
		List<ParcelaFinanciamento> novas = amortizarPorModalidade(
				ModalidadeAmortizacaoFinanciamento.REDUZIR_PRAZO);

		assertThat(novas).hasSize(2);
		assertThat(novas).extracting(parcela -> parcela.getValor().valor())
				.containsExactly(new BigDecimal("225.00"), new BigDecimal("225.00"));
	}

	@Test
	void reduzPrestacaoMantendoQuantidadeDeParcelasPendentes() {
		List<ParcelaFinanciamento> novas = amortizarPorModalidade(
				ModalidadeAmortizacaoFinanciamento.REDUZIR_PRESTACAO);

		assertThat(novas).hasSize(3);
		assertThat(novas).extracting(parcela -> parcela.getValor().valor())
				.containsExactly(new BigDecimal("150.00"), new BigDecimal("150.00"), new BigDecimal("150.00"));
	}

	@Test
	void amortizaSaldoERecriaSomenteParcelasPendentes() {
		ParcelaFinanciamentoRepositoryPort parcelas = mock(ParcelaFinanciamentoRepositoryPort.class);
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		ObterDataAtualPort dataAtual = mock(ObterDataAtualPort.class);
		Financiamento financiamento = Financiamento.reconstituir(10L, 1L, "Veículo",
				ValorMonetario.of(new BigDecimal("1000.00")), BigDecimal.ZERO, 4,
				LocalDate.of(2026, 1, 10), 2L);
		ParcelaFinanciamento paga = ParcelaFinanciamento.reconstituir(20L, 10L, 1,
				ValorMonetario.of(new BigDecimal("250.00")), ValorMonetario.of(new BigDecimal("250.00")),
				ValorMonetario.zero(), ValorMonetario.zero(), ValorMonetario.of(new BigDecimal("1000.00")),
				ValorMonetario.of(new BigDecimal("750.00")), LocalDate.of(2026, 1, 10),
				StatusParcelaFinanciamento.PAGA, 80L);
		ParcelaFinanciamento pendente = ParcelaFinanciamento.reconstituir(21L, 10L, 2,
				ValorMonetario.of(new BigDecimal("250.00")), ValorMonetario.of(new BigDecimal("250.00")),
				ValorMonetario.zero(), ValorMonetario.zero(), ValorMonetario.of(new BigDecimal("750.00")),
				ValorMonetario.of(new BigDecimal("500.00")), LocalDate.of(2026, 2, 10),
				StatusParcelaFinanciamento.PENDENTE, null);
		ParcelaFinanciamento terceira = ParcelaFinanciamento.reconstituir(22L, 10L, 3,
				ValorMonetario.of(new BigDecimal("250.00")), ValorMonetario.of(new BigDecimal("250.00")),
				ValorMonetario.zero(), ValorMonetario.zero(), ValorMonetario.of(new BigDecimal("500.00")),
				ValorMonetario.of(new BigDecimal("250.00")), LocalDate.of(2026, 3, 10),
				StatusParcelaFinanciamento.PENDENTE, null);
		ParcelaFinanciamento quarta = ParcelaFinanciamento.reconstituir(23L, 10L, 4,
				ValorMonetario.of(new BigDecimal("250.00")), ValorMonetario.of(new BigDecimal("250.00")),
				ValorMonetario.zero(), ValorMonetario.zero(), ValorMonetario.of(new BigDecimal("250.00")),
				ValorMonetario.zero(), LocalDate.of(2026, 4, 10), StatusParcelaFinanciamento.PENDENTE, null);
		when(parcelas.listarPorFinanciamento(10L)).thenReturn(List.of(paga, pendente, terceira, quarta));
		when(contas.buscarPorIdEUsuario(2L, 1L)).thenReturn(Optional.of(
				com.finisus.domain.model.Conta.reconstituir(2L, 1L, "Principal", TipoConta.FISICO, null,
						ValorMonetario.of(new BigDecimal("1000.00")), true, 0)));
		when(transacoes.salvar(org.mockito.ArgumentMatchers.any())).thenReturn(Transacao.reconstituir(90L, 1L,
				TipoTransacao.SAIDA, ValorMonetario.of(new BigDecimal("150.00")), LocalDate.of(2026, 1, 20),
				"Amortização - Veículo", 2L, null, null, null, null, null, null, null, 0, List.of()));
		when(dataAtual.obterDataHora()).thenReturn(java.time.LocalDateTime.of(2026, 1, 20, 10, 0));
		ParcelaFinanciamentoService service = new ParcelaFinanciamentoService(parcelas,
				mock(FinanciamentoRepositoryPort.class), contas, transacoes, dataAtual,
				mock(EstornarTransacaoVinculadaUseCase.class));

		var resultado = service.amortizar(1L, financiamento, new BigDecimal("150.00"),
				LocalDate.of(2026, 1, 20), 2, null);

		assertThat(resultado.saldoDevedorAnterior()).isEqualByComparingTo("750.00");
		assertThat(resultado.saldoDevedorAtual()).isEqualByComparingTo("600.00");
		assertThat(resultado.parcelasPagasPreservadas()).isEqualTo(1);
		verify(parcelas).excluirPendentesAPartirDe(10L, 2);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<ParcelaFinanciamento>> novas = ArgumentCaptor.forClass(List.class);
		verify(parcelas).salvarNovas(eq(10L), novas.capture());
		assertThat(novas.getValue()).extracting(p -> p.getPrincipal().valor())
				.containsExactly(new BigDecimal("300.00"), new BigDecimal("300.00"));
		assertThat(novas.getValue()).extracting(ParcelaFinanciamento::getNumero).containsExactly(2, 3);
		ArgumentCaptor<com.finisus.domain.model.Conta> contaAtualizada = ArgumentCaptor.forClass(
				com.finisus.domain.model.Conta.class);
		verify(contas).salvar(contaAtualizada.capture());
		assertThat(contaAtualizada.getValue().getSaldo().valor()).isEqualByComparingTo("850.00");
	}

	@Test
	void distribuiResidualNaUltimaParcelaSemPerderCentavosDoPrincipal() {
		ParcelaFinanciamentoRepositoryPort parcelas = mock(ParcelaFinanciamentoRepositoryPort.class);
		Financiamento financiamento = Financiamento.reconstituir(10L, 1L, "Sem juros",
				ValorMonetario.of(new BigDecimal("100.00")), BigDecimal.ZERO, 3,
				LocalDate.of(2026, 1, 10), 2L);
		ParcelaFinanciamentoService service = new ParcelaFinanciamentoService(parcelas,
				mock(FinanciamentoRepositoryPort.class), mock(ContaRepositoryPort.class),
				mock(TransacaoRepositoryPort.class), mock(ObterDataAtualPort.class),
				mock(EstornarTransacaoVinculadaUseCase.class));

		service.gerar(financiamento);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<ParcelaFinanciamento>> captor = ArgumentCaptor.forClass(List.class);
		verify(parcelas).salvarNovas(eq(10L), captor.capture());
		assertThat(captor.getValue()).extracting(p -> p.getValor().valor())
				.containsExactly(new BigDecimal("33.33"), new BigDecimal("33.33"), new BigDecimal("33.34"));
		BigDecimal soma = captor.getValue().stream().map(p -> p.getValor().valor())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		assertThat(soma).isEqualByComparingTo("100.00");
		assertThat(captor.getValue()).extracting(p -> p.getPrincipal().valor())
				.containsExactly(new BigDecimal("33.33"), new BigDecimal("33.33"), new BigDecimal("33.34"));
		assertThat(captor.getValue()).extracting(p -> p.getSaldoDevedorFinal().valor())
				.containsExactly(new BigDecimal("66.67"), new BigDecimal("33.34"), new BigDecimal("0.00"));
	}

	@Test
	void separaPrincipalEJurosEZeraSaldoNaUltimaParcela() {
		ParcelaFinanciamentoRepositoryPort parcelas = mock(ParcelaFinanciamentoRepositoryPort.class);
		Financiamento financiamento = Financiamento.reconstituir(10L, 1L, "Com juros",
				ValorMonetario.of(new BigDecimal("1000.00")), new BigDecimal("1.00"), 3,
				LocalDate.of(2026, 1, 10), 2L);
		ParcelaFinanciamentoService service = new ParcelaFinanciamentoService(parcelas,
				mock(FinanciamentoRepositoryPort.class), mock(ContaRepositoryPort.class),
				mock(TransacaoRepositoryPort.class), mock(ObterDataAtualPort.class),
				mock(EstornarTransacaoVinculadaUseCase.class));

		service.gerar(financiamento);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<ParcelaFinanciamento>> captor = ArgumentCaptor.forClass(List.class);
		verify(parcelas).salvarNovas(eq(10L), captor.capture());
		List<ParcelaFinanciamento> cronograma = captor.getValue();
		assertThat(cronograma).extracting(p -> p.getJuros().valor())
				.containsExactly(new BigDecimal("10.00"), new BigDecimal("6.70"), new BigDecimal("3.37"));
		assertThat(cronograma.stream().map(p -> p.getPrincipal().valor()).reduce(BigDecimal.ZERO, BigDecimal::add))
				.isEqualByComparingTo("1000.00");
		assertThat(cronograma.get(2).getSaldoDevedorFinal().valor()).isEqualByComparingTo("0.00");
		assertThat(cronograma).allSatisfy(parcela -> assertThat(parcela.getValor().valor())
				.isEqualByComparingTo(parcela.getPrincipal().valor().add(parcela.getJuros().valor())
						.add(parcela.getEncargos().valor())));
	}

	@Test
	void estornaTransacaoEReabreParcelaVencida() {
		ParcelaFinanciamentoRepositoryPort parcelas = mock(ParcelaFinanciamentoRepositoryPort.class);
		FinanciamentoRepositoryPort financiamentos = mock(FinanciamentoRepositoryPort.class);
		ObterDataAtualPort dataAtual = mock(ObterDataAtualPort.class);
		EstornarTransacaoVinculadaUseCase estornos = mock(EstornarTransacaoVinculadaUseCase.class);
		Financiamento financiamento = Financiamento.reconstituir(10L, 1L, "Veículo",
				ValorMonetario.of(new BigDecimal("1000.00")), BigDecimal.ZERO, 10,
				LocalDate.of(2026, 1, 10), 2L);
		ParcelaFinanciamento paga = ParcelaFinanciamento.reconstituir(20L, 10L, 1,
				ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 1, 10),
				StatusParcelaFinanciamento.PAGA, 99L);
		when(financiamentos.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(financiamento));
		when(parcelas.listarPorFinanciamento(10L)).thenReturn(List.of(paga));
		when(dataAtual.obter()).thenReturn(LocalDate.of(2026, 9, 23));
		when(parcelas.salvar(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));
		ParcelaFinanciamentoService service = new ParcelaFinanciamentoService(parcelas, financiamentos,
				mock(ContaRepositoryPort.class), mock(TransacaoRepositoryPort.class), dataAtual, estornos);

		ParcelaFinanciamento reaberta = service.estornarPagamento(1L, 10L, 20L);

		verify(estornos).estornarVinculada(1L, 99L);
		assertThat(reaberta.getStatus()).isEqualTo(StatusParcelaFinanciamento.ATRASADA);
		assertThat(reaberta.getTransacaoId()).isNull();
	}

	private List<ParcelaFinanciamento> amortizarPorModalidade(
			ModalidadeAmortizacaoFinanciamento modalidade) {
		ParcelaFinanciamentoRepositoryPort parcelas = mock(ParcelaFinanciamentoRepositoryPort.class);
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		ObterDataAtualPort dataAtual = mock(ObterDataAtualPort.class);
		Financiamento financiamento = Financiamento.reconstituir(10L, 1L, "Veículo",
				ValorMonetario.of(new BigDecimal("750.00")), BigDecimal.ZERO, 3,
				LocalDate.of(2026, 2, 10), 2L);
		List<ParcelaFinanciamento> pendentes = java.util.stream.IntStream.rangeClosed(1, 3)
				.mapToObj(numero -> ParcelaFinanciamento.reconstituir(20L + numero, 10L, numero,
						ValorMonetario.of(new BigDecimal("250.00")),
						ValorMonetario.of(new BigDecimal("250.00")), ValorMonetario.zero(),
						ValorMonetario.zero(), ValorMonetario.of(new BigDecimal(1000 - numero * 250 + ".00")),
						ValorMonetario.of(new BigDecimal(750 - numero * 250 + ".00")),
						LocalDate.of(2026, 1 + numero, 10), StatusParcelaFinanciamento.PENDENTE, null))
				.toList();
		when(parcelas.listarPorFinanciamento(10L)).thenReturn(pendentes);
		when(contas.buscarPorIdEUsuario(2L, 1L)).thenReturn(Optional.of(
				com.finisus.domain.model.Conta.reconstituir(2L, 1L, "Principal", TipoConta.FISICO, null,
						ValorMonetario.of(new BigDecimal("1000.00")), true, 0)));
		when(transacoes.salvar(org.mockito.ArgumentMatchers.any())).thenReturn(Transacao.reconstituir(90L, 1L,
				TipoTransacao.SAIDA, ValorMonetario.of(new BigDecimal("300.00")), LocalDate.of(2026, 1, 20),
				"Amortização - Veículo", 2L, null, null, null, null, null, null, null, 0, List.of()));
		when(dataAtual.obterDataHora()).thenReturn(java.time.LocalDateTime.of(2026, 1, 20, 10, 0));
		ParcelaFinanciamentoService service = new ParcelaFinanciamentoService(parcelas,
				mock(FinanciamentoRepositoryPort.class), contas, transacoes, dataAtual,
				mock(EstornarTransacaoVinculadaUseCase.class));

		service.amortizar(1L, financiamento, new BigDecimal("300.00"), LocalDate.of(2026, 1, 20), null,
				modalidade);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<ParcelaFinanciamento>> captor = ArgumentCaptor.forClass(List.class);
		verify(parcelas).salvarNovas(eq(10L), captor.capture());
		return captor.getValue();
	}
}
