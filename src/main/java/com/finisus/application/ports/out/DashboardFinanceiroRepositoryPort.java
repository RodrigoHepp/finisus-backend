package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase.ConsultaBalancete;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase.ConsultaBalanceteAnual;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase.LinhaBalancete;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase.ResumoCategoria;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase.OrigemResumo;
import com.finisus.domain.model.TipoTransacao;
import java.math.BigDecimal;
import java.util.List;

public interface DashboardFinanceiroRepositoryPort {
	DadosMensais consultarMensal(Long usuarioId, String anoMes);

	List<ValorPorOrigem> consultarResumoPeriodo(Long usuarioId, String mesInicial, String mesFinal);

	List<ValorMensal> consultarResumoAnual(Long usuarioId, String ano);

	List<ResumoCategoria> consultarCategoriasAnual(Long usuarioId, String ano);

	List<ResumoCategoria> consultarCategoriasPeriodo(Long usuarioId, String mesInicial, String mesFinal);

	Pagina<LinhaBalancete> consultarBalancete(Long usuarioId, ConsultaBalancete consulta, Paginacao paginacao);

	Pagina<LinhaBalancete> consultarBalanceteAnual(Long usuarioId, ConsultaBalanceteAnual consulta,
			Paginacao paginacao);

	Totais consultarTotaisBalancete(Long usuarioId, ConsultaBalancete consulta);

	Totais consultarTotaisBalanceteAnual(Long usuarioId, ConsultaBalanceteAnual consulta);

	record DadosMensais(List<ResumoCategoria> categorias, BigDecimal gastosFaturas, BigDecimal valorFaturasPago,
			BigDecimal valorFaturasEmAberto, BigDecimal resultadoCaixa) {
	}

	record Totais(BigDecimal receitas, BigDecimal gastosDiretos, BigDecimal gastosFaturas,
			BigDecimal pagamentosFaturas, BigDecimal comprasParceladas, BigDecimal resultadoCaixaInvestimentos) {
	}

	record ValorPorOrigem(OrigemResumo origem, TipoTransacao tipo, BigDecimal valor) {
	}

	record ValorMensal(String anoMesFatura, Integer mesData, OrigemResumo origem, TipoTransacao tipo,
			BigDecimal valor) {
	}
}
