package com.finisus;

import static org.assertj.core.api.Assertions.assertThat;

import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase;
import com.finisus.application.ports.in.PainelFinanceiroUseCase;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
class DashboardFinanceiroIntegrationTest {
	@Autowired
	private DashboardFinanceiroUseCase dashboard;

	@Autowired
	private PainelFinanceiroUseCase painel;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@Transactional
	void limitaConsultasDaAgendaEDoPatrimonioIndependentementeDaQuantidadeDeFaturasEInvestimentos() {
		inserirDadosDoMes();

		long consultasAgenda = medirConsultas(() -> painel.consultarAgenda(900_001L,
				LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
		long consultasPatrimonio = medirConsultas(() -> painel.consultarPatrimonio(900_001L,
				LocalDate.of(2026, 12, 31)));

		assertThat(consultasAgenda).as("consultas SQL da agenda").isLessThanOrEqualTo(10);
		assertThat(consultasPatrimonio).as("consultas SQL do patrimônio").isLessThanOrEqualTo(12);
	}

	@Test
	void consultaMesSemLancamentosComAgregacoesEPaginacaoValidas() {
		var mensal = dashboard.consultarMensal(999L, "2026-08");
		var balancete = dashboard.consultarBalancete(999L,
				new DashboardFinanceiroUseCase.ConsultaBalancete("2026-08", null, null, null), new Paginacao(0, 20));

		assertThat(mensal.categorias()).isEmpty();
		assertThat(mensal.resumo().resultadoCompetencia()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(balancete.linhas().conteudo()).isEmpty();
		assertThat(balancete.totais().resultadoCaixa()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@Transactional
	void consideraCompraNoMesDaFaturaEPagamentoNoResultadoDeCaixaSemDuplicarValor() {
		inserirDadosDoMes();

		var mensal = dashboard.consultarMensal(900_001L, "2026-08");
		var balancete = dashboard.consultarBalancete(900_001L,
				new DashboardFinanceiroUseCase.ConsultaBalancete("2026-08", null, null, null), new Paginacao(0, 20));
		var resumoPeriodo = dashboard.consultarResumoPeriodo(900_001L, "2026-08", 3);

		assertThat(mensal.resumo().receitas()).isEqualByComparingTo("1000.00");
		assertThat(mensal.resumo().gastosDiretos()).isEqualByComparingTo("200.00");
		assertThat(mensal.resumo().gastosFaturas()).isEqualByComparingTo("300.00");
		assertThat(mensal.resumo().valorFaturasPago()).isEqualByComparingTo("300.00");
		assertThat(mensal.resumo().valorFaturasEmAberto()).isEqualByComparingTo("0.00");
		assertThat(mensal.resumo().resultadoCompetencia()).isEqualByComparingTo("500.00");
		assertThat(mensal.resumo().resultadoCaixa()).isEqualByComparingTo("560.00");
		assertThat(balancete.linhas().conteudo()).hasSize(6);
		assertThat(balancete.totais().resultadoCompetencia()).isEqualByComparingTo("500.00");
		assertThat(balancete.totais().resultadoCaixa()).isEqualByComparingTo("560.00");
		assertThat(resumoPeriodo.mesInicial()).isEqualTo("2026-06");
		assertThat(resumoPeriodo.totais().receitas()).isEqualByComparingTo("1000.00");
		assertThat(resumoPeriodo.totais().despesas()).isEqualByComparingTo("500.00");
		assertThat(resumoPeriodo.totais().pagamentosFaturas()).isEqualByComparingTo("300.00");
		assertThat(resumoPeriodo.totais().resultadoCompetencia()).isEqualByComparingTo("500.00");
		assertThat(resumoPeriodo.totais().resultadoCaixa()).isEqualByComparingTo("560.00");
		assertThat(resumoPeriodo.origens()).filteredOn(origem -> origem.origem() == DashboardFinanceiroUseCase.OrigemResumo.CARTAO)
				.singleElement().satisfies(origem -> assertThat(origem.despesas()).isEqualByComparingTo("300.00"));
		assertThat(resumoPeriodo.origens()).filteredOn(origem -> origem.origem() == DashboardFinanceiroUseCase.OrigemResumo.RECORRENCIA)
				.singleElement().satisfies(origem -> assertThat(origem.despesas()).isEqualByComparingTo("40.00"));
		assertThat(resumoPeriodo.origens()).filteredOn(origem -> origem.origem() == DashboardFinanceiroUseCase.OrigemResumo.COMPRA_PARCELADA)
				.singleElement().satisfies(origem -> assertThat(origem.despesas()).isEqualByComparingTo("60.00"));
		assertThat(resumoPeriodo.origens()).filteredOn(origem -> origem.origem() == DashboardFinanceiroUseCase.OrigemResumo.PAGAMENTO_FATURA)
				.singleElement().satisfies(origem -> assertThat(origem.pagamentosFaturas()).isEqualByComparingTo("300.00"));
	}

	@Test
	@Transactional
	void consolidaAnoComMesesVaziosCategoriasOrigensEBalancetePaginado() {
		inserirDadosDoMes();

		var anual = dashboard.consultarAnual(900_001L, "2026");
		var composicao = dashboard.consultarComposicaoAnual(900_001L, "2026");
		var balancete = dashboard.consultarBalanceteAnual(900_001L,
				new DashboardFinanceiroUseCase.ConsultaBalanceteAnual("2026", null, null, null), new Paginacao(0, 20));

		assertThat(anual.meses()).hasSize(12);
		assertThat(anual.meses().get(0).anoMes()).isEqualTo("2026-01");
		assertThat(anual.meses().get(0).receitas()).isEqualByComparingTo("2000.00");
		assertThat(anual.meses().get(0).gastosDiretos()).isEqualByComparingTo("0.00");
		assertThat(anual.meses().get(3).resultadoCompetencia()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(anual.meses().get(7).gastosFaturas()).isEqualByComparingTo("300.00");
		assertThat(anual.totais().receitas()).isEqualByComparingTo("3000.00");
		assertThat(anual.totais().gastosDiretos()).isEqualByComparingTo("530.00");
		assertThat(anual.totais().gastosFaturas()).isEqualByComparingTo("300.00");
		assertThat(anual.totais().pagamentosFaturas()).isEqualByComparingTo("300.00");
		assertThat(anual.totais().resultadoCompetencia()).isEqualByComparingTo("2170.00");
		assertThat(anual.totais().resultadoCaixa()).isEqualByComparingTo("1730.00");
		assertThat(composicao.categorias()).extracting(DashboardFinanceiroUseCase.ResumoCategoria::categoriaNome)
				.contains("Salário", "Despesas fixas", "Financiamento")
				.doesNotContain("Investimentos");
		assertThat(composicao.origens()).filteredOn(origem -> origem.origem() == DashboardFinanceiroUseCase.OrigemResumo.CARTAO)
				.singleElement().satisfies(origem -> assertThat(origem.despesas()).isEqualByComparingTo("300.00"));
		assertThat(balancete.linhas().conteudo()).hasSize(10);
		assertThat(balancete.totais().resultadoCompetencia()).isEqualByComparingTo("2170.00");
		assertThat(balancete.totais().resultadoCaixa()).isEqualByComparingTo("1730.00");
	}

	private void inserirDadosDoMes() {
		jdbcTemplate.update("INSERT INTO usuario (id, nome, email, senha_hash, ativo) VALUES (900001, 'Dashboard', "
				+ "'dashboard@teste.local', 'senha', TRUE)");
		jdbcTemplate.update("INSERT INTO conta (id, usuario_id, nome, tipo, saldo) VALUES (900002, 900001, "
				+ "'Conta principal', 'FISICO', 1000.00)");
		jdbcTemplate.update("INSERT INTO categoria (id, usuario_id, nome, ativo) VALUES "
				+ "(900003, 900001, 'Salário', TRUE), (900004, 900001, 'Mercado', TRUE), "
				+ "(900005, 900001, 'Cartão', TRUE), (900016, 900001, 'Investimentos', TRUE), "
				+ "(900017, 900001, 'Despesas fixas', TRUE), (900018, 900001, 'Financiamento', TRUE)");
		jdbcTemplate.update("INSERT INTO cartao_credito (id, usuario_id, nome, limite, dia_fechamento, dia_vencimento) "
				+ "VALUES (900006, 900001, 'Cartão', 5000.00, 20, 30)");
		jdbcTemplate.update("INSERT INTO fatura (id, cartao_id, ano_mes, data_fechamento, data_vencimento, status, "
				+ "conta_pagamento_id) VALUES (900007, 900006, '2026-08', '2026-08-20', '2026-08-30', 'FECHADA', 900002)");
		jdbcTemplate.update("INSERT INTO recorrencia (id, usuario_id, nome, tipo, valor_esperado, dia_do_mes, categoria_id, "
				+ "conta_id, ativo) VALUES (900012, 900001, 'Academia', 'SAIDA', 40.00, 10, 900004, 900002, TRUE)");
		jdbcTemplate.update("INSERT INTO compra_parcelada (id, usuario_id, descricao, valor_total, numero_parcelas, "
				+ "data_compra, categoria_id, conta_id) VALUES (900013, 900001, 'Notebook', 120.00, 2, '2026-07-10', 900004, 900002)");
		jdbcTemplate.update("INSERT INTO transacao (id, usuario_id, tipo, valor, data, descricao, conta_id, categoria_id) "
				+ "VALUES (900008, 900001, 'ENTRADA', 1000.00, '2026-08-01', 'Salário', 900002, 900003), "
				+ "(900009, 900001, 'SAIDA', 100.00, '2026-08-05', 'Mercado', 900002, 900004)");
		jdbcTemplate.update("INSERT INTO transacao (id, usuario_id, tipo, valor, data, descricao, conta_id, categoria_id, "
				+ "fatura_id) VALUES (900010, 900001, 'SAIDA', 300.00, '2026-07-30', 'Compra no cartão', 900002, 900005, 900007)");
		jdbcTemplate.update("INSERT INTO transacao (id, usuario_id, tipo, valor, data, descricao, conta_id, "
				+ "fatura_pagamento_id) VALUES (900011, 900001, 'SAIDA', 300.00, '2026-08-22', 'Pagamento fatura', 900002, 900007)");
		jdbcTemplate.update("INSERT INTO transacao (id, usuario_id, tipo, valor, data, descricao, conta_id, categoria_id, "
				+ "recorrencia_id) VALUES (900014, 900001, 'SAIDA', 40.00, '2026-08-10', 'Academia', 900002, 900004, 900012)");
		jdbcTemplate.update("INSERT INTO transacao (id, usuario_id, tipo, valor, data, descricao, conta_id, categoria_id, "
				+ "compra_parcelada_id) VALUES (900015, 900001, 'SAIDA', 60.00, '2026-08-15', 'Parcela notebook', 900002, 900004, 900013)");
		jdbcTemplate.update("INSERT INTO transacao (id, usuario_id, tipo, valor, data, descricao, conta_id, categoria_id) "
				+ "VALUES (900019, 900001, 'ENTRADA', 2000.00, '2026-01-05', 'Salário janeiro', 900002, 900003), "
				+ "(900020, 900001, 'SAIDA', 500.00, '2026-01-10', 'Aporte investimento', 900002, 900016), "
				+ "(900021, 900001, 'SAIDA', 80.00, '2026-03-05', 'Condomínio', 900002, 900017), "
				+ "(900022, 900001, 'SAIDA', 250.00, '2026-02-15', 'Parcela financiamento', 900002, 900018)");
		inserirAporteDeInvestimentoVinculado();
	}

	private void inserirAporteDeInvestimentoVinculado() {
		jdbcTemplate.update("INSERT INTO investimento (id, usuario_id, nome, tipo, conta_origem_id) "
				+ "VALUES (900023, 900001, 'Reserva', 'RENDA_FIXA', 900002)");
		jdbcTemplate.update("INSERT INTO movimento_investimento (id, investimento_id, tipo, valor, data, transacao_id) "
				+ "VALUES (900024, 900023, 'APORTE', 500.00, '2026-01-10', 900020)");
	}

	private long medirConsultas(Runnable operacao) {
		var estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
		estatisticas.setStatisticsEnabled(true);
		estatisticas.clear();
		try {
			operacao.run();
			return estatisticas.getPrepareStatementCount();
		} finally {
			estatisticas.setStatisticsEnabled(false);
		}
	}
}
