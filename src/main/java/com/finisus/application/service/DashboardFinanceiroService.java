package com.finisus.application.service;

import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase;
import com.finisus.application.ports.out.DashboardFinanceiroRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.vo.AnoMes;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

public class DashboardFinanceiroService implements DashboardFinanceiroUseCase {
	private static final Set<Integer> PERIODOS_PERMITIDOS = Set.of(1, 3, 6, 12);
	private final DashboardFinanceiroRepositoryPort dashboard;

	public DashboardFinanceiroService(DashboardFinanceiroRepositoryPort dashboard) {
		this.dashboard = dashboard;
	}

	@Override
	public DashboardMensal consultarMensal(Long usuarioId, String anoMes) {
		AnoMes periodo = periodo(anoMes);
		var dados = dashboard.consultarMensal(usuarioId, periodo.formatado());
		BigDecimal receitas = dados.categorias().stream().map(ResumoCategoria::receitas).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal despesas = dados.categorias().stream().map(ResumoCategoria::despesas).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal gastosDiretos = despesas.subtract(dados.gastosFaturas());
		BigDecimal resultadoCompetencia = receitas.subtract(despesas);
		return new DashboardMensal(periodo.formatado(), new ResumoMensal(receitas, gastosDiretos, dados.gastosFaturas(),
				dados.valorFaturasPago(), dados.valorFaturasEmAberto(), resultadoCompetencia, dados.resultadoCaixa(),
				situacao(resultadoCompetencia), situacao(dados.resultadoCaixa())), dados.categorias());
	}

	@Override
	public ResumoPeriodo consultarResumoPeriodo(Long usuarioId, String mesFinal, int periodoMeses) {
		if (!PERIODOS_PERMITIDOS.contains(periodoMeses)) {
			throw new DomainException("error.dashboard.periodo.meses.invalid");
		}
		AnoMes fim = periodo(mesFinal);
		YearMonth fimYearMonth = YearMonth.of(fim.ano(), fim.mes());
		AnoMes inicio = AnoMes.parse(fimYearMonth.minusMonths(periodoMeses - 1L).toString());
		var acumulados = new EnumMap<OrigemResumo, AcumuladoOrigem>(OrigemResumo.class);
		for (OrigemResumo origem : OrigemResumo.values()) acumulados.put(origem, new AcumuladoOrigem());
		BigDecimal receitas = BigDecimal.ZERO;
		BigDecimal despesas = BigDecimal.ZERO;
		BigDecimal pagamentosFaturas = BigDecimal.ZERO;
		BigDecimal resultadoCaixa = BigDecimal.ZERO;
		for (var valor : dashboard.consultarResumoPeriodo(usuarioId, inicio.formatado(), fim.formatado())) {
			AcumuladoOrigem acumulado = acumulados.get(valor.origem());
			if (valor.origem() == OrigemResumo.INVESTIMENTO) {
				resultadoCaixa = valor.tipo() == com.finisus.domain.model.TipoTransacao.ENTRADA
						? resultadoCaixa.add(valor.valor()) : resultadoCaixa.subtract(valor.valor());
			} else if (valor.origem() == OrigemResumo.PAGAMENTO_FATURA) {
				acumulado.pagamentosFaturas = acumulado.pagamentosFaturas.add(valor.valor());
				pagamentosFaturas = pagamentosFaturas.add(valor.valor());
				resultadoCaixa = resultadoCaixa.subtract(valor.valor());
			} else if (valor.tipo() == com.finisus.domain.model.TipoTransacao.ENTRADA) {
				acumulado.receitas = acumulado.receitas.add(valor.valor());
				receitas = receitas.add(valor.valor());
				if (valor.origem() != OrigemResumo.CARTAO && valor.origem() != OrigemResumo.COMPRA_PARCELADA) {
					resultadoCaixa = resultadoCaixa.add(valor.valor());
				}
			} else {
				acumulado.despesas = acumulado.despesas.add(valor.valor());
				despesas = despesas.add(valor.valor());
				if (valor.origem() != OrigemResumo.CARTAO && valor.origem() != OrigemResumo.COMPRA_PARCELADA) {
					resultadoCaixa = resultadoCaixa.subtract(valor.valor());
				}
			}
		}
		BigDecimal resultadoCompetencia = receitas.subtract(despesas);
		List<ResumoOrigem> origens = acumulados.entrySet().stream()
				.map(entry -> new ResumoOrigem(entry.getKey(), entry.getValue().receitas, entry.getValue().despesas,
						entry.getValue().pagamentosFaturas))
				.toList();
		return new ResumoPeriodo(inicio.formatado(), fim.formatado(), periodoMeses,
				new TotaisPeriodo(receitas, despesas, pagamentosFaturas, resultadoCompetencia, resultadoCaixa), origens);
	}

	@Override
	public ResumoAnual consultarAnual(Long usuarioId, String ano) {
		Year periodo = ano(ano);
		List<AcumuladoMes> acumulados = new ArrayList<>();
		for (int mes = 1; mes <= 12; mes++) acumulados.add(new AcumuladoMes());
		for (var valor : dashboard.consultarResumoAnual(usuarioId, periodo.toString())) {
			int mes = valor.anoMesFatura() == null ? valor.mesData() : YearMonth.parse(valor.anoMesFatura()).getMonthValue();
			acumulados.get(mes - 1).adicionar(valor.origem(), valor.tipo(), valor.valor());
		}
		List<ResumoMes> meses = new ArrayList<>();
		for (int mes = 1; mes <= 12; mes++) meses.add(acumulados.get(mes - 1).resumo(periodo, mes));
		BigDecimal receitas = meses.stream().map(ResumoMes::receitas).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal gastosDiretos = meses.stream().map(ResumoMes::gastosDiretos).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal gastosFaturas = meses.stream().map(ResumoMes::gastosFaturas).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal pagamentosFaturas = meses.stream().map(ResumoMes::pagamentosFaturas).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal resultadoCompetencia = receitas.subtract(gastosDiretos).subtract(gastosFaturas);
		BigDecimal resultadoCaixa = meses.stream().map(ResumoMes::resultadoCaixa).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new ResumoAnual(periodo.toString(), meses, new TotaisBalancete(receitas, gastosDiretos, gastosFaturas,
				pagamentosFaturas, resultadoCompetencia, resultadoCaixa));
	}

	@Override
	public ComposicaoAnual consultarComposicaoAnual(Long usuarioId, String ano) {
		Year periodo = ano(ano);
		ResumoPeriodo resumo = consultarResumoPeriodo(usuarioId, periodo + "-12", 12);
		return new ComposicaoAnual(periodo.toString(), resumo.totais(), resumo.origens(),
				dashboard.consultarCategoriasAnual(usuarioId, periodo.toString()));
	}

	@Override
	public Balancete consultarBalancete(Long usuarioId, ConsultaBalancete consulta, Paginacao paginacao) {
		AnoMes periodo = periodo(consulta.anoMes());
		ConsultaBalancete normalizada = new ConsultaBalancete(periodo.formatado(), consulta.tipo(), consulta.categoriaId(),
				consulta.contaId());
		var totais = dashboard.consultarTotaisBalancete(usuarioId, normalizada);
		return new Balancete(dashboard.consultarBalancete(usuarioId, normalizada, paginacao), totaisBalancete(totais));
	}

	@Override
	public Balancete consultarBalanceteAnual(Long usuarioId, ConsultaBalanceteAnual consulta, Paginacao paginacao) {
		Year periodo = ano(consulta.ano());
		ConsultaBalanceteAnual normalizada = new ConsultaBalanceteAnual(periodo.toString(), consulta.tipo(),
				consulta.categoriaId(), consulta.contaId());
		var totais = dashboard.consultarTotaisBalanceteAnual(usuarioId, normalizada);
		return new Balancete(dashboard.consultarBalanceteAnual(usuarioId, normalizada, paginacao), totaisBalancete(totais));
	}

	private TotaisBalancete totaisBalancete(DashboardFinanceiroRepositoryPort.Totais totais) {
		BigDecimal gastosDiretos = totais.gastosDiretos().add(totais.comprasParceladas());
		BigDecimal resultadoCompetencia = totais.receitas().subtract(gastosDiretos).subtract(totais.gastosFaturas());
		BigDecimal resultadoCaixa = totais.receitas().subtract(totais.gastosDiretos()).subtract(totais.pagamentosFaturas())
				.add(totais.resultadoCaixaInvestimentos());
		return new TotaisBalancete(totais.receitas(), gastosDiretos, totais.gastosFaturas(),
				totais.pagamentosFaturas(), resultadoCompetencia, resultadoCaixa);
	}

	private SituacaoResultado situacao(BigDecimal resultado) {
		return resultado.signum() > 0 ? SituacaoResultado.POSITIVO
				: resultado.signum() < 0 ? SituacaoResultado.NEGATIVO : SituacaoResultado.NEUTRO;
	}

	private AnoMes periodo(String anoMes) {
		try {
			return AnoMes.parse(anoMes);
		} catch (DateTimeException exception) {
			throw new DomainException("error.anomes.invalid");
		}
	}

	private Year ano(String ano) {
		try {
			if (ano == null || !ano.matches("\\d{4}")) throw new DateTimeException("Ano inválido");
			return Year.parse(ano);
		} catch (DateTimeException exception) {
			throw new DomainException("error.dashboard.ano.invalido");
		}
	}

	private static class AcumuladoOrigem {
		private BigDecimal receitas = BigDecimal.ZERO;
		private BigDecimal despesas = BigDecimal.ZERO;
		private BigDecimal pagamentosFaturas = BigDecimal.ZERO;
	}

	private static class AcumuladoMes {
		private BigDecimal receitas = BigDecimal.ZERO;
		private BigDecimal gastosDiretos = BigDecimal.ZERO;
		private BigDecimal gastosFaturas = BigDecimal.ZERO;
		private BigDecimal pagamentosFaturas = BigDecimal.ZERO;
		private BigDecimal resultadoCaixa = BigDecimal.ZERO;

		private void adicionar(OrigemResumo origem, com.finisus.domain.model.TipoTransacao tipo, BigDecimal valor) {
			if (origem == OrigemResumo.INVESTIMENTO) {
				resultadoCaixa = tipo == com.finisus.domain.model.TipoTransacao.ENTRADA
						? resultadoCaixa.add(valor) : resultadoCaixa.subtract(valor);
			} else if (origem == OrigemResumo.PAGAMENTO_FATURA) {
				pagamentosFaturas = pagamentosFaturas.add(valor);
				resultadoCaixa = resultadoCaixa.subtract(valor);
			} else if (tipo == com.finisus.domain.model.TipoTransacao.ENTRADA) {
				receitas = receitas.add(valor);
				if (origem != OrigemResumo.CARTAO && origem != OrigemResumo.COMPRA_PARCELADA) resultadoCaixa = resultadoCaixa.add(valor);
			} else {
				if (origem == OrigemResumo.CARTAO) gastosFaturas = gastosFaturas.add(valor);
				else gastosDiretos = gastosDiretos.add(valor);
				if (origem != OrigemResumo.CARTAO && origem != OrigemResumo.COMPRA_PARCELADA) resultadoCaixa = resultadoCaixa.subtract(valor);
			}
		}

		private ResumoMes resumo(Year ano, int mes) {
			BigDecimal resultadoCompetencia = receitas.subtract(gastosDiretos).subtract(gastosFaturas);
			return new ResumoMes(YearMonth.of(ano.getValue(), mes).toString(), receitas, gastosDiretos, gastosFaturas,
					pagamentosFaturas, resultadoCompetencia, resultadoCaixa);
		}
	}
}
