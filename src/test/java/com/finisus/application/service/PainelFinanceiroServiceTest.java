package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finisus.application.ports.in.DashboardFinanceiroUseCase;
import com.finisus.application.ports.in.ConsultarResumoDivisaoUseCase;
import com.finisus.application.ports.in.FaturaUseCase;
import com.finisus.application.ports.in.PainelFinanceiroUseCase;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.DashboardFinanceiroRepositoryPort;
import com.finisus.application.ports.out.DivisaoCompartilhadaRepositoryPort;
import com.finisus.application.ports.out.FaturaRepositoryPort;
import com.finisus.application.ports.out.InvestimentoRepositoryPort;
import com.finisus.application.ports.out.MovimentoInvestimentoRepositoryPort;
import com.finisus.application.ports.out.ObrigacaoFinanceiraRepositoryPort;
import com.finisus.application.ports.out.ParcelaFinanciamentoRepositoryPort;
import com.finisus.application.ports.out.PosicaoInvestimentoRepositoryPort;
import com.finisus.application.ports.out.RecorrenciaRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.TipoConta;
import com.finisus.domain.model.Usuario;
import com.finisus.domain.DomainException;
import com.finisus.domain.vo.Email;
import com.finisus.domain.vo.ValorMonetario;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PainelFinanceiroServiceTest {
	private static final Instant INSTANTE_CONSULTA = Instant.parse("2026-09-24T15:30:00Z");
	private static final Clock CLOCK = Clock.fixed(INSTANTE_CONSULTA, ZoneOffset.UTC);

	@Test
	void recusaIntervaloDeAgendaSuperiorA366Dias() {
		PainelFinanceiroService service = serviceVazio();

		assertThatThrownBy(() -> service.consultarAgenda(1L, LocalDate.of(2026, 1, 1),
				LocalDate.of(2027, 1, 2))).isInstanceOf(DomainException.class)
				.hasMessage("error.dashboard.agenda.periodo.invalido");
	}

	@Test
	void recusaIntervaloDeCompartilhadosSuperiorA366Dias() {
		PainelFinanceiroService service = serviceVazio();

		assertThatThrownBy(() -> service.consultarCompartilhados(1L, LocalDate.of(2026, 1, 1),
				LocalDate.of(2027, 1, 2))).isInstanceOf(DomainException.class)
				.hasMessage("error.divisao.periodo.invalido");
	}

	@Test
	void apresentaSaldoLivreAposCompromissosDaJanela() {
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		InvestimentoRepositoryPort investimentos = mock(InvestimentoRepositoryPort.class);
		FaturaRepositoryPort faturas = mock(FaturaRepositoryPort.class);
		ParcelaFinanciamentoRepositoryPort parcelas = mock(ParcelaFinanciamentoRepositoryPort.class);
		RecorrenciaRepositoryPort recorrencias = mock(RecorrenciaRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		ObrigacaoFinanceiraRepositoryPort obrigacoes = mock(ObrigacaoFinanceiraRepositoryPort.class);
		DashboardFinanceiroUseCase dashboard = mock(DashboardFinanceiroUseCase.class);
		when(contas.listarPorUsuario(1L)).thenReturn(List.of(Conta.reconstituir(2L, 1L, "Principal", TipoConta.FISICO,
				null, ValorMonetario.of(new BigDecimal("1000.00")), true, 0)));
		var obrigacao = com.finisus.domain.model.ObrigacaoFinanceira.nova(1L, "Energia", "Concessionária",
				ValorMonetario.of(new BigDecimal("200.00")), LocalDate.of(2026, 9, 10), 2L, null);
		var vencida = com.finisus.domain.model.ObrigacaoFinanceira.nova(1L, "Água vencida", "Concessionária",
				ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 8, 25), 2L, null);
		when(obrigacoes.listarPendentesPorUsuarioEPeriodo(anyLong(), any(), any()))
				.thenReturn(List.of(obrigacao, vencida));
		when(investimentos.listarPorUsuario(1L)).thenReturn(List.of());
		when(faturas.listarEmAbertoPorUsuario(1L)).thenReturn(List.of());
		when(parcelas.listarPendentesPorUsuario(1L)).thenReturn(List.of());
		when(recorrencias.listarAtivasPorUsuario(1L)).thenReturn(List.of());
		when(transacoes.listarPorUsuarioEPeriodo(anyLong(), any(), any())).thenReturn(List.of());
		when(dashboard.consultarMensal(1L, "2026-09")).thenReturn(new DashboardFinanceiroUseCase.DashboardMensal("2026-09",
				new DashboardFinanceiroUseCase.ResumoMensal(new BigDecimal("2000.00"), new BigDecimal("300.00"),
						BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1700.00"),
						new BigDecimal("1700.00"), DashboardFinanceiroUseCase.SituacaoResultado.POSITIVO,
						DashboardFinanceiroUseCase.SituacaoResultado.POSITIVO),
				List.of(new DashboardFinanceiroUseCase.ResumoCategoria(3L, "Mercado", BigDecimal.ZERO,
						new BigDecimal("300.00"), new BigDecimal("-300.00")))));

		PainelFinanceiroService service = new PainelFinanceiroService(contas, investimentos,
				mock(MovimentoInvestimentoRepositoryPort.class), mock(PosicaoInvestimentoRepositoryPort.class),
				faturas, mock(FaturaUseCase.class), obrigacoes, parcelas, recorrencias, transacoes, dashboard,
				mock(DashboardFinanceiroRepositoryPort.class), mock(DivisaoCompartilhadaRepositoryPort.class),
				mock(ConsultarResumoDivisaoUseCase.class), mock(UsuarioRepositoryPort.class), CLOCK);

		var visao = service.consultarVisaoGeral(1L, LocalDate.of(2026, 9, 1), 30);

		assertThat(visao.comprometidoNaJanela()).isEqualByComparingTo("300.00");
		assertThat(visao.saldoLivreNaJanela()).isEqualByComparingTo("700.00");
		assertThat(visao.alertas()).extracting("descricao", "nivel")
				.contains(tuple("Água vencida", "VENCIDO"));
		assertThat(visao.maioresGastos()).singleElement().extracting("categoria").isEqualTo("Mercado");
	}

	@Test
	void separaCreditoAReceberDeCompensacaoAPagarNasDivisoes() {
		DivisaoCompartilhadaRepositoryPort divisoes = mock(DivisaoCompartilhadaRepositoryPort.class);
		ConsultarResumoDivisaoUseCase resumos = mock(ConsultarResumoDivisaoUseCase.class);
		UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
		when(divisoes.listarAtivasPorParticipante(1L)).thenReturn(List.of(
				com.finisus.domain.model.DivisaoCompartilhada.reconstituir(20L, 1L, "Apartamento",
						List.of(new com.finisus.domain.model.ParticipanteDivisao(1L, new BigDecimal("50")),
								new com.finisus.domain.model.ParticipanteDivisao(2L, new BigDecimal("50"))),
						com.finisus.domain.model.StatusDivisaoCompartilhada.ATIVA)));
		when(resumos.consultar(1L, 20L, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))).thenReturn(
				new com.finisus.domain.model.ResumoDivisao(20L, "Apartamento", ValorMonetario.of(new BigDecimal("200.00")),
						List.of(new com.finisus.domain.model.ResumoParticipanteDivisao(1L, new BigDecimal("50"),
								ValorMonetario.of(new BigDecimal("150.00")), ValorMonetario.of(new BigDecimal("100.00")),
								new BigDecimal("50.00")), new com.finisus.domain.model.ResumoParticipanteDivisao(2L,
								new BigDecimal("50"), ValorMonetario.of(new BigDecimal("50.00")),
								ValorMonetario.of(new BigDecimal("100.00")), new BigDecimal("-50.00")))));
		when(usuarios.buscarPorIds(List.of(1L, 2L))).thenReturn(List.of(
				Usuario.reconstituir(1L, "Ana", new Email("ana@finisus.test"), "hash", true,
						LocalDateTime.of(2026, 1, 1, 0, 0), 0),
				Usuario.reconstituir(2L, "Bruno", new Email("bruno@finisus.test"), "hash", true,
						LocalDateTime.of(2026, 1, 1, 0, 0), 0)));

		PainelFinanceiroService service = new PainelFinanceiroService(mock(ContaRepositoryPort.class),
				mock(InvestimentoRepositoryPort.class), mock(MovimentoInvestimentoRepositoryPort.class),
				mock(PosicaoInvestimentoRepositoryPort.class), mock(FaturaRepositoryPort.class), mock(FaturaUseCase.class),
				mock(ObrigacaoFinanceiraRepositoryPort.class), mock(ParcelaFinanciamentoRepositoryPort.class),
				mock(RecorrenciaRepositoryPort.class), mock(TransacaoRepositoryPort.class), mock(DashboardFinanceiroUseCase.class),
				mock(DashboardFinanceiroRepositoryPort.class), divisoes, resumos, usuarios, CLOCK);

		var compartilhados = service.consultarCompartilhados(1L, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

		assertThat(compartilhados.aReceber()).isEqualByComparingTo("50.00");
		assertThat(compartilhados.aPagar()).isZero();
		assertThat(compartilhados.divisoes()).singleElement().extracting("meuSaldo").isEqualTo(new BigDecimal("50.00"));
		assertThat(compartilhados.divisoes())
				.flatExtracting(PainelFinanceiroUseCase.ResumoDivisaoCompartilhada::participantes)
				.extracting(PainelFinanceiroUseCase.ParticipanteCompartilhado::nomeExibicao)
				.containsExactly("Ana", "Bruno");
	}

	@Test
	void mantemNoPatrimonioContasEInvestimentosInativosComValor() {
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		InvestimentoRepositoryPort investimentos = mock(InvestimentoRepositoryPort.class);
		MovimentoInvestimentoRepositoryPort movimentos = mock(MovimentoInvestimentoRepositoryPort.class);
		PosicaoInvestimentoRepositoryPort posicoes = mock(PosicaoInvestimentoRepositoryPort.class);
		FaturaRepositoryPort faturas = mock(FaturaRepositoryPort.class);
		ObrigacaoFinanceiraRepositoryPort obrigacoes = mock(ObrigacaoFinanceiraRepositoryPort.class);
		ParcelaFinanciamentoRepositoryPort parcelas = mock(ParcelaFinanciamentoRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		LocalDate referencia = LocalDate.of(2026, 9, 24);
		when(contas.listarPorUsuario(1L)).thenReturn(List.of(
				Conta.reconstituir(2L, 1L, "Encerrada com saldo", TipoConta.FISICO, null,
						ValorMonetario.of(new BigDecimal("150.00")), false, 0),
				Conta.reconstituir(3L, 1L, "Custódia", TipoConta.APLICACAO, 99L,
						ValorMonetario.of(new BigDecimal("540.00")), true, 0),
				Conta.reconstituir(4L, 1L, "Encerrada zerada", TipoConta.FISICO, null,
						ValorMonetario.zero(), false, 0)));
		var investimentoInativo = com.finisus.domain.model.Investimento.reconstituir(10L, 1L, "CDB encerrado",
				com.finisus.domain.model.TipoInvestimento.RENDA_FIXA, 2L, 3L, false);
		when(investimentos.listarPorUsuario(1L)).thenReturn(List.of(investimentoInativo));
		var movimentosInvestimento = List.of(
				com.finisus.domain.model.MovimentoInvestimento.novo(10L,
						com.finisus.domain.model.TipoMovimentoInvestimento.APORTE,
						ValorMonetario.of(new BigDecimal("500.00")), LocalDate.of(2026, 1, 10)),
				com.finisus.domain.model.MovimentoInvestimento.novo(10L,
						com.finisus.domain.model.TipoMovimentoInvestimento.RENDIMENTO_REALIZADO,
						ValorMonetario.of(new BigDecimal("40.00")), LocalDate.of(2026, 9, 15)),
				com.finisus.domain.model.MovimentoInvestimento.novo(10L,
						com.finisus.domain.model.TipoMovimentoInvestimento.TAXA,
						ValorMonetario.of(new BigDecimal("10.00")), LocalDate.of(2026, 9, 16)));
		when(movimentos.listarPorInvestimentos(List.of(10L))).thenReturn(java.util.Map.of(10L, movimentosInvestimento));
		var ultimaPosicao = com.finisus.domain.model.PosicaoInvestimento.nova(10L,
				ValorMonetario.of(new BigDecimal("540.00")), LocalDate.of(2026, 9, 20));
		when(posicoes.buscarUltimasAte(List.of(10L), referencia)).thenReturn(java.util.Map.of(10L, ultimaPosicao));
		when(faturas.listarEmAbertoPorUsuario(1L)).thenReturn(List.of());
		when(obrigacoes.listarPendentesPorUsuarioEPeriodo(anyLong(), any(), any())).thenReturn(List.of());
		when(parcelas.listarPendentesPorUsuario(1L)).thenReturn(List.of(
				com.finisus.domain.model.ParcelaFinanciamento.reconstituir(30L, 40L, 1,
						ValorMonetario.of(new BigDecimal("110.00")), ValorMonetario.of(new BigDecimal("100.00")),
						ValorMonetario.of(new BigDecimal("8.00")), ValorMonetario.of(new BigDecimal("2.00")),
						ValorMonetario.of(new BigDecimal("500.00")), ValorMonetario.of(new BigDecimal("400.00")),
						LocalDate.of(2026, 10, 10), com.finisus.domain.model.StatusParcelaFinanciamento.PENDENTE, null),
				com.finisus.domain.model.ParcelaFinanciamento.reconstituir(31L, 41L, 1,
						ValorMonetario.of(new BigDecimal("50.00")), LocalDate.of(2026, 10, 15),
						com.finisus.domain.model.StatusParcelaFinanciamento.PENDENTE)));
		when(transacoes.listarPorUsuarioEPeriodo(anyLong(), any(), any())).thenReturn(List.of());

		PainelFinanceiroService service = new PainelFinanceiroService(contas, investimentos, movimentos, posicoes,
				faturas, mock(FaturaUseCase.class), obrigacoes, parcelas, mock(RecorrenciaRepositoryPort.class),
				transacoes, mock(DashboardFinanceiroUseCase.class), mock(DashboardFinanceiroRepositoryPort.class),
				mock(DivisaoCompartilhadaRepositoryPort.class), mock(ConsultarResumoDivisaoUseCase.class),
				mock(UsuarioRepositoryPort.class), CLOCK);

		var patrimonio = service.consultarPatrimonio(1L, referencia);

		assertThat(patrimonio.saldosEDividasConsultadosEm()).isEqualTo(INSTANTE_CONSULTA);
		assertThat(patrimonio.patrimonioHistoricoCompleto()).isFalse();
		assertThat(patrimonio.saldoContas()).isEqualByComparingTo("150.00");
		assertThat(patrimonio.valorAtualInvestimentos()).isEqualByComparingTo("540.00");
		assertThat(patrimonio.parcelasFinanciamentoEmAberto()).isEqualByComparingTo("160.00");
		assertThat(patrimonio.principalFinanciamentosEmAberto()).isEqualByComparingTo("150.00");
		assertThat(patrimonio.jurosEncargosFinanciamentosFuturos()).isEqualByComparingTo("10.00");
		assertThat(patrimonio.parcelasFinanciamentoSemComposicao()).isEqualByComparingTo("50.00");
		assertThat(patrimonio.dividasPrincipais()).isEqualByComparingTo("150.00");
		assertThat(patrimonio.dividasECompromissos()).isEqualByComparingTo("160.00");
		assertThat(patrimonio.patrimonioLiquido()).isEqualByComparingTo("540.00");
		assertThat(patrimonio.investimentos()).singleElement()
				.extracting("investimento", "capitalLiquido", "valorAtual")
				.containsExactly("CDB encerrado", new BigDecimal("500.00"), new BigDecimal("540.00"));
		verify(movimentos, never()).listarPorInvestimento(anyLong());
		verify(posicoes, never()).buscarUltimaAte(anyLong(), any());
	}

	private PainelFinanceiroService serviceVazio() {
		return new PainelFinanceiroService(mock(ContaRepositoryPort.class), mock(InvestimentoRepositoryPort.class),
				mock(MovimentoInvestimentoRepositoryPort.class), mock(PosicaoInvestimentoRepositoryPort.class),
				mock(FaturaRepositoryPort.class), mock(FaturaUseCase.class), mock(ObrigacaoFinanceiraRepositoryPort.class),
				mock(ParcelaFinanciamentoRepositoryPort.class), mock(RecorrenciaRepositoryPort.class),
				mock(TransacaoRepositoryPort.class), mock(DashboardFinanceiroUseCase.class),
				mock(DashboardFinanceiroRepositoryPort.class), mock(DivisaoCompartilhadaRepositoryPort.class),
				mock(ConsultarResumoDivisaoUseCase.class), mock(UsuarioRepositoryPort.class), CLOCK);
	}
}
