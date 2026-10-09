package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;

import com.finisus.application.ports.in.RegistrarTransacaoUseCase;
import com.finisus.application.ports.in.EstornarTransacaoVinculadaUseCase;
import com.finisus.application.ports.in.TransacaoUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.ObrigacaoFinanceiraRepositoryPort;
import com.finisus.application.ports.out.PagamentoObrigacaoRepositoryPort;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.ObrigacaoFinanceira;
import com.finisus.domain.model.PagamentoObrigacao;
import com.finisus.domain.model.TipoConta;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.vo.ValorMonetario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ObrigacaoFinanceiraServiceTest {
	@Test
	void pagarCriaSaidaSomenteNaLiquidacaoEVinculaTransacao() {
		ObrigacaoFinanceiraRepositoryPort obrigacoes = mock(ObrigacaoFinanceiraRepositoryPort.class);
		RegistrarTransacaoUseCase transacoes = mock(RegistrarTransacaoUseCase.class);
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		PagamentoObrigacaoRepositoryPort pagamentos = mock(PagamentoObrigacaoRepositoryPort.class);
		ObrigacaoFinanceiraService service = new ObrigacaoFinanceiraService(obrigacoes, contas,
				mock(CategoriaRepositoryPort.class), transacoes, mock(ObterDataAtualPort.class),
				mock(EstornarTransacaoVinculadaUseCase.class), pagamentos);
		ObrigacaoFinanceira aberta = ObrigacaoFinanceira.reconstituir(10L, 1L, "Internet", "Provedor",
				ValorMonetario.of(new BigDecimal("120.00")), LocalDate.of(2026, 9, 10), 2L, null,
				com.finisus.domain.model.StatusObrigacaoFinanceira.EM_ABERTO, null, null, null, 0);
		var transacao = com.finisus.domain.model.Transacao.reconstituir(99L, 1L, TipoTransacao.SAIDA,
				ValorMonetario.of(new BigDecimal("120.00")), LocalDate.of(2026, 9, 10), "Pagamento", 2L,
				null, null, null, null, null, null, null, 0, List.of());
		when(obrigacoes.buscarPorIdEUsuarioParaAtualizacao(10L, 1L)).thenReturn(Optional.of(aberta));
		when(transacoes.registrar(eq(1L), any(TransacaoUseCase.RegistrarCommand.class))).thenReturn(transacao);
		when(obrigacoes.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

		ObrigacaoFinanceira paga = service.pagar(1L, 10L, LocalDate.of(2026, 9, 11), null);

		assertThat(paga.getTransacaoId()).isEqualTo(99L);
		assertThat(paga.getDataLiquidacao()).isEqualTo(LocalDate.of(2026, 9, 11));
	}

	@Test
	void estornaTransacaoEReabreObrigacaoNaMesmaOperacao() {
		ObrigacaoFinanceiraRepositoryPort obrigacoes = mock(ObrigacaoFinanceiraRepositoryPort.class);
		ObterDataAtualPort dataAtual = mock(ObterDataAtualPort.class);
		EstornarTransacaoVinculadaUseCase estornos = mock(EstornarTransacaoVinculadaUseCase.class);
		PagamentoObrigacaoRepositoryPort pagamentos = mock(PagamentoObrigacaoRepositoryPort.class);
		ObrigacaoFinanceira paga = ObrigacaoFinanceira.nova(1L, "Internet", "Provedor",
				ValorMonetario.of(new BigDecimal("120.00")), LocalDate.of(2026, 9, 10), 2L, null)
				.liquidar(LocalDate.of(2026, 9, 10), 99L);
		when(obrigacoes.buscarPorIdEUsuarioParaAtualizacao(10L, 1L)).thenReturn(Optional.of(paga));
		when(dataAtual.obter()).thenReturn(LocalDate.of(2026, 9, 23));
		when(dataAtual.obterDataHora()).thenReturn(java.time.LocalDateTime.of(2026, 9, 23, 10, 0));
		when(pagamentos.listarPorObrigacaoEUsuario(10L, 1L)).thenReturn(List.of(new PagamentoObrigacao(7L, 10L,
				1L, 99L, ValorMonetario.of(new BigDecimal("120.00")), LocalDate.of(2026, 9, 10), null, 0)));
		when(obrigacoes.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
		ObrigacaoFinanceiraService service = new ObrigacaoFinanceiraService(obrigacoes,
				mock(ContaRepositoryPort.class), mock(CategoriaRepositoryPort.class),
				mock(RegistrarTransacaoUseCase.class), dataAtual, estornos, pagamentos);

		ObrigacaoFinanceira reaberta = service.estornarPagamento(1L, 10L);

		verify(estornos).estornarVinculada(1L, 99L);
		assertThat(reaberta.getStatus()).isEqualTo(com.finisus.domain.model.StatusObrigacaoFinanceira.VENCIDA);
	}

	@Test
	void pagamentoParcialMovimentaSomenteValorInformadoEMantemSaldoPendente() {
		ObrigacaoFinanceiraRepositoryPort obrigacoes = mock(ObrigacaoFinanceiraRepositoryPort.class);
		RegistrarTransacaoUseCase transacoes = mock(RegistrarTransacaoUseCase.class);
		PagamentoObrigacaoRepositoryPort pagamentos = mock(PagamentoObrigacaoRepositoryPort.class);
		ObrigacaoFinanceira aberta = ObrigacaoFinanceira.reconstituir(10L, 1L, "Internet", "Provedor",
				ValorMonetario.of(new BigDecimal("120.00")), LocalDate.of(2026, 9, 10), 2L, null,
				com.finisus.domain.model.StatusObrigacaoFinanceira.EM_ABERTO, null, null, null, 0);
		var transacao = com.finisus.domain.model.Transacao.reconstituir(99L, 1L, TipoTransacao.SAIDA,
				ValorMonetario.of(new BigDecimal("40.00")), LocalDate.of(2026, 9, 10), "Pagamento", 2L,
				null, null, null, null, null, null, null, 0, List.of());
		when(obrigacoes.buscarPorIdEUsuarioParaAtualizacao(10L, 1L)).thenReturn(Optional.of(aberta));
		when(transacoes.registrar(eq(1L), any())).thenReturn(transacao);
		when(obrigacoes.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
		var service = new ObrigacaoFinanceiraService(obrigacoes, mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), transacoes, mock(ObterDataAtualPort.class),
				mock(EstornarTransacaoVinculadaUseCase.class), pagamentos);

		ObrigacaoFinanceira parcial = service.pagar(1L, 10L, LocalDate.of(2026, 9, 11), new BigDecimal("40.00"));

		assertThat(parcial.getValorPago().valor()).isEqualByComparingTo("40.00");
		assertThat(parcial.getSaldoPendente().valor()).isEqualByComparingTo("80.00");
		assertThat(parcial.getStatus()).isEqualTo(com.finisus.domain.model.StatusObrigacaoFinanceira.EM_ABERTO);
		verify(pagamentos).salvar(any(PagamentoObrigacao.class));
	}

	@Test
	void separaDescontoDoAbatimentoEJurosEEncargosDoCaixa() {
		ObrigacaoFinanceiraRepositoryPort obrigacoes = mock(ObrigacaoFinanceiraRepositoryPort.class);
		RegistrarTransacaoUseCase transacoes = mock(RegistrarTransacaoUseCase.class);
		PagamentoObrigacaoRepositoryPort pagamentos = mock(PagamentoObrigacaoRepositoryPort.class);
		ObrigacaoFinanceira aberta = ObrigacaoFinanceira.reconstituir(10L, 1L, "IPTU", "Prefeitura",
				ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 9, 30), 2L, null,
				com.finisus.domain.model.StatusObrigacaoFinanceira.EM_ABERTO, null, null, null, 0);
		var transacao = com.finisus.domain.model.Transacao.reconstituir(99L, 1L, TipoTransacao.SAIDA,
				ValorMonetario.of(new BigDecimal("97.00")), LocalDate.of(2026, 9, 20), "Pagamento", 2L,
				null, null, null, null, null, null, null, 0, List.of());
		when(obrigacoes.buscarPorIdEUsuarioParaAtualizacao(10L, 1L)).thenReturn(Optional.of(aberta));
		when(transacoes.registrar(eq(1L), any())).thenReturn(transacao);
		when(obrigacoes.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
		var service = new ObrigacaoFinanceiraService(obrigacoes, mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), transacoes, mock(ObterDataAtualPort.class),
				mock(EstornarTransacaoVinculadaUseCase.class), pagamentos);

		ObrigacaoFinanceira quitada = service.pagar(1L, 10L,
				new com.finisus.application.ports.in.ObrigacaoFinanceiraUseCase.PagamentoCommand(
						LocalDate.of(2026, 9, 20), new BigDecimal("90.00"), new BigDecimal("5.00"),
						new BigDecimal("2.00"), new BigDecimal("10.00")));

		ArgumentCaptor<TransacaoUseCase.RegistrarCommand> transacaoCommand = ArgumentCaptor.forClass(
				TransacaoUseCase.RegistrarCommand.class);
		verify(transacoes).registrar(eq(1L), transacaoCommand.capture());
		assertThat(transacaoCommand.getValue().valor()).isEqualByComparingTo("97.00");
		assertThat(quitada.getStatus()).isEqualTo(com.finisus.domain.model.StatusObrigacaoFinanceira.PAGA);
		assertThat(quitada.getSaldoPendente().valor()).isZero();
	}
}
