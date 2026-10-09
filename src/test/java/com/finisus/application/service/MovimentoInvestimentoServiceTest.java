package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import com.finisus.application.ports.in.InvestimentoUseCase;
import com.finisus.application.ports.in.MovimentoInvestimentoUseCase;
import com.finisus.application.ports.in.RegistrarTransacaoUseCase;
import com.finisus.application.ports.in.TransacaoUseCase;
import com.finisus.application.ports.out.MovimentoInvestimentoRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.domain.model.Investimento;
import com.finisus.domain.model.MovimentoInvestimento;
import com.finisus.domain.model.TipoInvestimento;
import com.finisus.domain.model.TipoMovimentoInvestimento;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.vo.ValorMonetario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MovimentoInvestimentoServiceTest {
	@Test
	void registraRendimentoETaxaComEfeitosDeCaixaDistintosSemInferirValores() {
		MovimentoInvestimentoRepositoryPort movimentos = mock(MovimentoInvestimentoRepositoryPort.class);
		InvestimentoUseCase investimentos = mock(InvestimentoUseCase.class);
		RegistrarTransacaoUseCase registro = mock(RegistrarTransacaoUseCase.class);
		Investimento investimento = Investimento.reconstituir(20L, 1L, "Reserva",
				TipoInvestimento.RENDA_FIXA, 40L);
		when(investimentos.buscar(1L, 20L)).thenReturn(investimento);
		when(registro.registrar(any(), any())).thenReturn(Transacao.reconstituir(30L, 1L, TipoTransacao.ENTRADA,
				ValorMonetario.of(new BigDecimal("12.34")), LocalDate.of(2026, 9, 30), "Rendimento", 40L,
				null, null, null, null, null, null, null, 0, List.of()));
		when(movimentos.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
		MovimentoInvestimentoService service = new MovimentoInvestimentoService(movimentos, investimentos, registro,
				mock(TransacaoUseCase.class), mock(ObterDataAtualPort.class));

		service.movimentar(1L, new MovimentoInvestimentoUseCase.MovimentoCommand(20L,
				TipoMovimentoInvestimento.RENDIMENTO_REALIZADO, new BigDecimal("12.34"),
				LocalDate.of(2026, 9, 30)));
		service.movimentar(1L, new MovimentoInvestimentoUseCase.MovimentoCommand(20L,
				TipoMovimentoInvestimento.TAXA, new BigDecimal("1.23"), LocalDate.of(2026, 9, 30)));

		ArgumentCaptor<TransacaoUseCase.RegistrarCommand> comandos = ArgumentCaptor.forClass(
				TransacaoUseCase.RegistrarCommand.class);
		verify(registro, org.mockito.Mockito.times(2)).registrar(org.mockito.ArgumentMatchers.eq(1L),
				comandos.capture());
		assertThat(comandos.getAllValues()).extracting(TransacaoUseCase.RegistrarCommand::tipo,
				TransacaoUseCase.RegistrarCommand::valor)
				.containsExactly(org.assertj.core.groups.Tuple.tuple(TipoTransacao.ENTRADA, new BigDecimal("12.34")),
						org.assertj.core.groups.Tuple.tuple(TipoTransacao.SAIDA, new BigDecimal("1.23")));
	}

	@Test
	void estornoReverteSomenteATransacaoOriginal() {
		MovimentoInvestimentoRepositoryPort movimentos = mock(MovimentoInvestimentoRepositoryPort.class);
		InvestimentoUseCase investimentos = mock(InvestimentoUseCase.class);
		RegistrarTransacaoUseCase registro = mock(RegistrarTransacaoUseCase.class);
		TransacaoUseCase transacoes = mock(TransacaoUseCase.class);
		ObterDataAtualPort dataAtual = mock(ObterDataAtualPort.class);
		MovimentoInvestimento original = MovimentoInvestimento.reconstituir(10L, 20L,
				TipoMovimentoInvestimento.APORTE, ValorMonetario.of(new BigDecimal("100.00")),
				LocalDate.of(2026, 9, 1), 30L, null, null);
		when(movimentos.buscarPorId(10L)).thenReturn(Optional.of(original));
		when(movimentos.existeCompensacao(10L)).thenReturn(false);
		when(investimentos.buscar(1L, 20L)).thenReturn(
				Investimento.reconstituir(20L, 1L, "Reserva", TipoInvestimento.RENDA_FIXA, 40L));
		when(dataAtual.obterDataHora()).thenReturn(LocalDateTime.of(2026, 9, 23, 10, 0));
		when(movimentos.salvar(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));

		MovimentoInvestimento resultado = new MovimentoInvestimentoService(movimentos, investimentos, registro,
				transacoes, dataAtual).estornar(1L, 10L);

		verify(transacoes).estornar(1L, 30L);
		verify(registro, never()).registrar(org.mockito.ArgumentMatchers.anyLong(),
				org.mockito.ArgumentMatchers.any());
		assertThat(resultado.getId()).isEqualTo(10L);
		assertThat(resultado.getMovimentoOrigemId()).isNull();
		assertThat(resultado.getEstornadoEm()).isEqualTo(LocalDateTime.of(2026, 9, 23, 10, 0));
	}
}
