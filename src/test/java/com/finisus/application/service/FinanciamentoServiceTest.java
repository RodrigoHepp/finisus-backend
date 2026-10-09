package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.finisus.application.ports.in.GerenciarParcelasFinanciamentoUseCase;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.FinanciamentoRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.ParcelaFinanciamentoRepositoryPort;
import com.finisus.domain.model.Financiamento;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.TipoConta;
import com.finisus.domain.model.StatusFinanciamento;
import com.finisus.domain.model.StatusParcelaFinanciamento;
import com.finisus.domain.vo.ValorMonetario;

class FinanciamentoServiceTest {

	@Test
	void fotografaCronogramaAntesDeRecalcularEAvancaVersao() {
		FinanciamentoRepositoryPort financiamentos = mock(FinanciamentoRepositoryPort.class);
		ParcelaFinanciamentoRepositoryPort parcelasPersistidas = mock(ParcelaFinanciamentoRepositoryPort.class);
		GerenciarParcelasFinanciamentoUseCase parcelas = mock(GerenciarParcelasFinanciamentoUseCase.class);
		Financiamento financiamento = Financiamento.reconstituir(10L, 1L, "Veículo",
				ValorMonetario.of(new BigDecimal("1000.00")), BigDecimal.ZERO, 10,
				LocalDate.of(2026, 1, 10), 2L);
		when(financiamentos.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(financiamento));
		when(parcelas.excluirERecalcular(financiamento, 20L))
				.thenReturn(Collections.nCopies(9, mock(com.finisus.domain.model.ParcelaFinanciamento.class)));
		when(financiamentos.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
		FinanciamentoService service = new FinanciamentoService(financiamentos, mock(ContaRepositoryPort.class),
				parcelas, parcelasPersistidas, mock(ObterDataAtualPort.class));

		int quantidade = service.excluirPorErroDeLancamento(1L, 10L, 20L);

		InOrder ordem = inOrder(parcelasPersistidas, parcelas, financiamentos);
		ordem.verify(parcelasPersistidas).registrarSnapshotCronograma(10L, 1);
		ordem.verify(parcelas).excluirERecalcular(financiamento, 20L);
		ArgumentCaptor<Financiamento> captor = ArgumentCaptor.forClass(Financiamento.class);
		ordem.verify(financiamentos).salvar(captor.capture());
		assertThat(quantidade).isEqualTo(9);
		assertThat(captor.getValue().getCronogramaVersao()).isEqualTo(2);
		assertThat(captor.getValue().getNumeroParcelas()).isEqualTo(9);
	}

	@Test
	void criaNovoContratoLigadoAoFinalizarFinanciamentoDeOrigem() {
		FinanciamentoRepositoryPort financiamentos = mock(FinanciamentoRepositoryPort.class);
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		GerenciarParcelasFinanciamentoUseCase parcelas = mock(GerenciarParcelasFinanciamentoUseCase.class);
		ParcelaFinanciamentoRepositoryPort parcelasPersistidas = mock(ParcelaFinanciamentoRepositoryPort.class);
		ObterDataAtualPort dataAtual = mock(ObterDataAtualPort.class);
		Financiamento origem = Financiamento.reconstituir(10L, 1L, "Contrato original",
				ValorMonetario.of(new BigDecimal("1000.00")), BigDecimal.ZERO, 10,
				LocalDate.of(2026, 1, 10), 2L);
		when(financiamentos.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(origem));
		when(contas.buscarPorIdEUsuario(2L, 1L)).thenReturn(Optional.of(Conta.reconstituir(2L, 1L, "Carteira",
				TipoConta.FISICO, null, ValorMonetario.zero(), 0)));
		when(parcelas.excluirPendentesAPartirDe(origem, 20L)).thenReturn(4);
		when(dataAtual.obterDataHora()).thenReturn(LocalDateTime.of(2026, 9, 24, 10, 0));
		when(financiamentos.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
		FinanciamentoService service = new FinanciamentoService(financiamentos, contas, parcelas,
				parcelasPersistidas, dataAtual);
		var comandoNovo = new com.finisus.application.ports.in.FinanciamentoUseCase.CriarCommand("Novo contrato",
				new BigDecimal("600.00"), new BigDecimal("1.00"), 6, LocalDate.of(2026, 10, 10), 2L);

		var resultado = service.refinanciar(1L, 10L,
				new com.finisus.application.ports.in.FinanciamentoUseCase.RefinanciarCommand(20L, comandoNovo));

		assertThat(resultado.financiamentoOrigem().isFinalizado()).isTrue();
		assertThat(resultado.financiamentoOrigem().getCronogramaVersao()).isEqualTo(2);
		assertThat(resultado.financiamentoOrigem().getNumeroParcelas()).isEqualTo(6);
		assertThat(resultado.novoFinanciamento().getFinanciamentoOrigemId()).isEqualTo(10L);
		assertThat(resultado.parcelasExcluidas()).isEqualTo(4);
		verify(parcelasPersistidas).registrarSnapshotCronograma(10L, 1);
		verify(parcelas).gerar(resultado.novoFinanciamento());
	}

	@Test
	void consultaSomenteVersaoHistoricaDeFinanciamentoDoUsuario() {
		FinanciamentoRepositoryPort financiamentos = mock(FinanciamentoRepositoryPort.class);
		ParcelaFinanciamentoRepositoryPort parcelasPersistidas = mock(ParcelaFinanciamentoRepositoryPort.class);
		Financiamento financiamento = Financiamento.reconstituir(10L, 1L, "Veículo",
				ValorMonetario.of(new BigDecimal("1000.00")), BigDecimal.ZERO, 9,
				LocalDate.of(2026, 1, 10), 2L, null, StatusFinanciamento.ATIVO, null, 2);
		var parcela = new ParcelaFinanciamentoRepositoryPort.ParcelaHistorica(20L, 1,
				new BigDecimal("100.00"), new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO,
				new BigDecimal("1000.00"), new BigDecimal("900.00"), LocalDate.of(2026, 1, 10),
				StatusParcelaFinanciamento.PENDENTE, null);
		when(financiamentos.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(financiamento));
		when(parcelasPersistidas.listarCronogramaHistorico(10L, 1)).thenReturn(java.util.List.of(parcela));
		FinanciamentoService service = new FinanciamentoService(financiamentos, mock(ContaRepositoryPort.class),
				mock(GerenciarParcelasFinanciamentoUseCase.class), parcelasPersistidas,
				mock(ObterDataAtualPort.class));

		assertThat(service.consultarCronogramaHistorico(1L, 10L, 1)).containsExactly(parcela);
		assertThatThrownBy(() -> service.consultarCronogramaHistorico(1L, 10L, 2))
				.isInstanceOf(com.finisus.domain.DomainException.class);
	}

	@Test
	void fotografaCronogramaEFinalizaFinanciamentoQuandoAmortizacaoQuitaSaldo() {
		FinanciamentoRepositoryPort financiamentos = mock(FinanciamentoRepositoryPort.class);
		ParcelaFinanciamentoRepositoryPort parcelasPersistidas = mock(ParcelaFinanciamentoRepositoryPort.class);
		GerenciarParcelasFinanciamentoUseCase parcelas = mock(GerenciarParcelasFinanciamentoUseCase.class);
		ObterDataAtualPort dataAtual = mock(ObterDataAtualPort.class);
		Financiamento financiamento = Financiamento.reconstituir(10L, 1L, "Veículo",
				ValorMonetario.of(new BigDecimal("1000.00")), BigDecimal.ZERO, 10,
				LocalDate.of(2026, 1, 10), 2L);
		LocalDate dataPagamento = LocalDate.of(2026, 9, 25);
		var comando = new com.finisus.application.ports.in.FinanciamentoUseCase.AmortizarCommand(
				new BigDecimal("600.00"), dataPagamento, 0);
		when(financiamentos.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(financiamento));
		when(parcelas.amortizar(1L, financiamento, comando.valor(), comando.dataPagamento(),
				comando.numeroParcelasRestantes(), comando.modalidade()))
				.thenReturn(new GerenciarParcelasFinanciamentoUseCase.AmortizacaoCronograma(30L,
						new BigDecimal("600.00"), BigDecimal.ZERO, 4, 0));
		when(dataAtual.obterDataHora()).thenReturn(LocalDateTime.of(2026, 9, 25, 10, 0));
		when(financiamentos.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
		FinanciamentoService service = new FinanciamentoService(financiamentos, mock(ContaRepositoryPort.class),
				parcelas, parcelasPersistidas, dataAtual);

		var resultado = service.amortizar(1L, 10L, comando);

		InOrder ordem = inOrder(parcelasPersistidas, parcelas, financiamentos);
		ordem.verify(parcelasPersistidas).registrarSnapshotCronograma(10L, 1);
		ordem.verify(parcelas).amortizar(1L, financiamento, comando.valor(), comando.dataPagamento(), 0, null);
		ArgumentCaptor<Financiamento> captor = ArgumentCaptor.forClass(Financiamento.class);
		ordem.verify(financiamentos).salvar(captor.capture());
		assertThat(resultado.transacaoId()).isEqualTo(30L);
		assertThat(resultado.saldoDevedorAnterior()).isEqualByComparingTo("600.00");
		assertThat(resultado.saldoDevedorAtual()).isZero();
		assertThat(resultado.parcelasRestantes()).isZero();
		assertThat(captor.getValue().getCronogramaVersao()).isEqualTo(2);
		assertThat(captor.getValue().getNumeroParcelas()).isEqualTo(4);
		assertThat(captor.getValue().isFinalizado()).isTrue();
	}
}
