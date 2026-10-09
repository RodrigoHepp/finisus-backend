package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase;
import com.finisus.application.ports.out.DashboardFinanceiroRepositoryPort;
import com.finisus.domain.DomainException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class DashboardFinanceiroServiceTest {
	private final DashboardFinanceiroRepositoryPort repositorio = mock(DashboardFinanceiroRepositoryPort.class);
	private final DashboardFinanceiroService service = new DashboardFinanceiroService(repositorio);

	@Test
	void consolidaCategoriasEFaturasSemMisturarPagamentoComDespesa() {
		var categorias = List.of(
				new DashboardFinanceiroUseCase.ResumoCategoria(1L, "Salário", new BigDecimal("5000.00"), BigDecimal.ZERO,
						new BigDecimal("5000.00")),
				new DashboardFinanceiroUseCase.ResumoCategoria(2L, "Mercado", BigDecimal.ZERO, new BigDecimal("700.00"),
						new BigDecimal("-700.00")));
		when(repositorio.consultarMensal(9L, "2026-08")).thenReturn(new DashboardFinanceiroRepositoryPort.DadosMensais(
				categorias, new BigDecimal("500.00"), new BigDecimal("500.00"), BigDecimal.ZERO,
				new BigDecimal("4200.00")));

		var dashboard = service.consultarMensal(9L, "2026-08");

		assertThat(dashboard.resumo().receitas()).isEqualByComparingTo("5000.00");
		assertThat(dashboard.resumo().gastosDiretos()).isEqualByComparingTo("200.00");
		assertThat(dashboard.resumo().gastosFaturas()).isEqualByComparingTo("500.00");
		assertThat(dashboard.resumo().valorFaturasPago()).isEqualByComparingTo("500.00");
		assertThat(dashboard.resumo().resultadoCompetencia()).isEqualByComparingTo("4300.00");
		assertThat(dashboard.resumo().resultadoCaixa()).isEqualByComparingTo("4200.00");
	}

	@Test
	void rejeitaMesInvalidoAntesDaConsulta() {
		assertThatThrownBy(() -> service.consultarMensal(9L, "2026-13"))
				.isInstanceOf(DomainException.class).hasMessage("error.anomes.invalid");
	}

	@Test
	void retornaTotaisSeparadosNoBalancete() {
		var consulta = new DashboardFinanceiroUseCase.ConsultaBalancete("2026-08", null, null, null);
		var paginacao = new Paginacao(0, 20);
		when(repositorio.consultarTotaisBalancete(9L, consulta)).thenReturn(new DashboardFinanceiroRepositoryPort.Totais(
				new BigDecimal("1000.00"), new BigDecimal("100.00"), new BigDecimal("300.00"),
				new BigDecimal("300.00"), BigDecimal.ZERO, BigDecimal.ZERO));
		when(repositorio.consultarBalancete(9L, consulta, paginacao)).thenReturn(new Pagina<>(List.of(), 0, 20, 0, 0));

		var balancete = service.consultarBalancete(9L, consulta, paginacao);

		assertThat(balancete.totais().resultadoCompetencia()).isEqualByComparingTo("600.00");
		assertThat(balancete.totais().resultadoCaixa()).isEqualByComparingTo("600.00");
	}

	@Test
	void resumePeriodoPorOrigemSemDuplicarPagamentoDaFaturaComoDespesa() {
		when(repositorio.consultarResumoPeriodo(9L, "2026-03", "2026-08")).thenReturn(List.of(
				new DashboardFinanceiroRepositoryPort.ValorPorOrigem(
						DashboardFinanceiroUseCase.OrigemResumo.MOVIMENTACAO_DIRETA, com.finisus.domain.model.TipoTransacao.ENTRADA,
						new BigDecimal("1000.00")),
				new DashboardFinanceiroRepositoryPort.ValorPorOrigem(
						DashboardFinanceiroUseCase.OrigemResumo.RECORRENCIA, com.finisus.domain.model.TipoTransacao.SAIDA,
						new BigDecimal("200.00")),
				new DashboardFinanceiroRepositoryPort.ValorPorOrigem(
						DashboardFinanceiroUseCase.OrigemResumo.COMPRA_PARCELADA, com.finisus.domain.model.TipoTransacao.SAIDA,
						new BigDecimal("50.00")),
				new DashboardFinanceiroRepositoryPort.ValorPorOrigem(
						DashboardFinanceiroUseCase.OrigemResumo.CARTAO, com.finisus.domain.model.TipoTransacao.SAIDA,
						new BigDecimal("300.00")),
				new DashboardFinanceiroRepositoryPort.ValorPorOrigem(
						DashboardFinanceiroUseCase.OrigemResumo.PAGAMENTO_FATURA, com.finisus.domain.model.TipoTransacao.SAIDA,
						new BigDecimal("300.00")),
				new DashboardFinanceiroRepositoryPort.ValorPorOrigem(
						DashboardFinanceiroUseCase.OrigemResumo.INVESTIMENTO, com.finisus.domain.model.TipoTransacao.SAIDA,
						new BigDecimal("400.00")),
				new DashboardFinanceiroRepositoryPort.ValorPorOrigem(
						DashboardFinanceiroUseCase.OrigemResumo.INVESTIMENTO, com.finisus.domain.model.TipoTransacao.ENTRADA,
						new BigDecimal("100.00"))));

		var resumo = service.consultarResumoPeriodo(9L, "2026-08", 6);

		assertThat(resumo.mesInicial()).isEqualTo("2026-03");
		assertThat(resumo.totais().receitas()).isEqualByComparingTo("1000.00");
		assertThat(resumo.totais().despesas()).isEqualByComparingTo("550.00");
		assertThat(resumo.totais().pagamentosFaturas()).isEqualByComparingTo("300.00");
		assertThat(resumo.totais().resultadoCompetencia()).isEqualByComparingTo("450.00");
		assertThat(resumo.totais().resultadoCaixa()).isEqualByComparingTo("200.00");
		assertThat(resumo.origens()).filteredOn(origem -> origem.origem() == DashboardFinanceiroUseCase.OrigemResumo.INVESTIMENTO)
				.singleElement().satisfies(origem -> {
					assertThat(origem.receitas()).isZero();
					assertThat(origem.despesas()).isZero();
				});
		assertThat(resumo.origens()).filteredOn(origem -> origem.origem() == DashboardFinanceiroUseCase.OrigemResumo.CARTAO)
				.singleElement().satisfies(origem -> assertThat(origem.despesas()).isEqualByComparingTo("300.00"));
	}

	@Test
	void rejeitaQuantidadeDeMesesForaDosPeriodosDisponiveis() {
		assertThatThrownBy(() -> service.consultarResumoPeriodo(9L, "2026-08", 2))
				.isInstanceOf(DomainException.class).hasMessage("error.dashboard.periodo.meses.invalid");
	}

	@Test
	void consolidaResumoAnualComMesesSemLancamentosECartaoNoMesDaFatura() {
		when(repositorio.consultarResumoAnual(9L, "2026")).thenReturn(List.of(
				new DashboardFinanceiroRepositoryPort.ValorMensal(null, 1,
						DashboardFinanceiroUseCase.OrigemResumo.MOVIMENTACAO_DIRETA,
						com.finisus.domain.model.TipoTransacao.ENTRADA, new BigDecimal("1000.00")),
				new DashboardFinanceiroRepositoryPort.ValorMensal(null, 1,
						DashboardFinanceiroUseCase.OrigemResumo.RECORRENCIA,
						com.finisus.domain.model.TipoTransacao.SAIDA, new BigDecimal("200.00")),
				new DashboardFinanceiroRepositoryPort.ValorMensal("2026-08", 7,
						DashboardFinanceiroUseCase.OrigemResumo.CARTAO,
						com.finisus.domain.model.TipoTransacao.SAIDA, new BigDecimal("300.00")),
				new DashboardFinanceiroRepositoryPort.ValorMensal(null, 8,
						DashboardFinanceiroUseCase.OrigemResumo.PAGAMENTO_FATURA,
						com.finisus.domain.model.TipoTransacao.SAIDA, new BigDecimal("300.00")),
				new DashboardFinanceiroRepositoryPort.ValorMensal(null, 1,
						DashboardFinanceiroUseCase.OrigemResumo.INVESTIMENTO,
						com.finisus.domain.model.TipoTransacao.SAIDA, new BigDecimal("400.00"))));

		var resumo = service.consultarAnual(9L, "2026");

		assertThat(resumo.meses()).hasSize(12);
		assertThat(resumo.meses().get(0).resultadoCompetencia()).isEqualByComparingTo("800.00");
		assertThat(resumo.meses().get(1).resultadoCompetencia()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(resumo.meses().get(7).gastosFaturas()).isEqualByComparingTo("300.00");
		assertThat(resumo.totais().resultadoCompetencia()).isEqualByComparingTo("500.00");
		assertThat(resumo.totais().resultadoCaixa()).isEqualByComparingTo("100.00");
	}

	@Test
	void retornaComposicaoAnualPorCategoriaEOrigem() {
		var categorias = List.of(new DashboardFinanceiroUseCase.ResumoCategoria(1L, "Salário",
				new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("1000.00")));
		when(repositorio.consultarResumoPeriodo(9L, "2026-01", "2026-12")).thenReturn(List.of(
				new DashboardFinanceiroRepositoryPort.ValorPorOrigem(
						DashboardFinanceiroUseCase.OrigemResumo.MOVIMENTACAO_DIRETA,
						com.finisus.domain.model.TipoTransacao.ENTRADA, new BigDecimal("1000.00"))));
		when(repositorio.consultarCategoriasAnual(9L, "2026")).thenReturn(categorias);

		var composicao = service.consultarComposicaoAnual(9L, "2026");

		assertThat(composicao.totais().receitas()).isEqualByComparingTo("1000.00");
		assertThat(composicao.categorias()).containsExactlyElementsOf(categorias);
		assertThat(composicao.origens()).filteredOn(origem -> origem.origem()
				== DashboardFinanceiroUseCase.OrigemResumo.MOVIMENTACAO_DIRETA).singleElement()
				.satisfies(origem -> assertThat(origem.receitas()).isEqualByComparingTo("1000.00"));
	}

	@Test
	void rejeitaAnoInvalidoAntesDaConsultaAnual() {
		assertThatThrownBy(() -> service.consultarAnual(9L, "202"))
				.isInstanceOf(DomainException.class).hasMessage("error.dashboard.ano.invalido");
	}
}
