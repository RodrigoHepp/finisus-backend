package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.TipoTransacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DashboardFinanceiroUseCase {
	DashboardMensal consultarMensal(Long usuarioId, String anoMes);

	ResumoPeriodo consultarResumoPeriodo(Long usuarioId, String mesFinal, int periodoMeses);

	ResumoAnual consultarAnual(Long usuarioId, String ano);

	ComposicaoAnual consultarComposicaoAnual(Long usuarioId, String ano);

	Balancete consultarBalancete(Long usuarioId, ConsultaBalancete consulta, Paginacao paginacao);

	Balancete consultarBalanceteAnual(Long usuarioId, ConsultaBalanceteAnual consulta, Paginacao paginacao);

	record ConsultaBalancete(String anoMes, TipoTransacao tipo, Long categoriaId, Long contaId) {
	}

	record ConsultaBalanceteAnual(String ano, TipoTransacao tipo, Long categoriaId, Long contaId) {
	}

	record DashboardMensal(String anoMes, ResumoMensal resumo, List<ResumoCategoria> categorias) {
	}

	record ResumoMensal(BigDecimal receitas, BigDecimal gastosDiretos, BigDecimal gastosFaturas,
			BigDecimal valorFaturasPago, BigDecimal valorFaturasEmAberto, BigDecimal resultadoCompetencia,
			BigDecimal resultadoCaixa, SituacaoResultado situacaoCompetencia, SituacaoResultado situacaoCaixa) {
	}

	record ResumoPeriodo(String mesInicial, String mesFinal, int periodoMeses, TotaisPeriodo totais,
			List<ResumoOrigem> origens) {
	}

	record TotaisPeriodo(BigDecimal receitas, BigDecimal despesas, BigDecimal pagamentosFaturas,
			BigDecimal resultadoCompetencia, BigDecimal resultadoCaixa) {
	}

	record ResumoAnual(String ano, List<ResumoMes> meses, TotaisBalancete totais) {
	}

	record ResumoMes(String anoMes, BigDecimal receitas, BigDecimal gastosDiretos, BigDecimal gastosFaturas,
			BigDecimal pagamentosFaturas, BigDecimal resultadoCompetencia, BigDecimal resultadoCaixa) {
	}

	record ComposicaoAnual(String ano, TotaisPeriodo totais, List<ResumoOrigem> origens,
			List<ResumoCategoria> categorias) {
	}

	record ResumoOrigem(OrigemResumo origem, BigDecimal receitas, BigDecimal despesas,
			BigDecimal pagamentosFaturas) {
	}

	record ResumoCategoria(Long categoriaId, String categoriaNome, BigDecimal receitas, BigDecimal despesas,
			BigDecimal saldo) {
	}

	record Balancete(Pagina<LinhaBalancete> linhas, TotaisBalancete totais) {
	}

	record LinhaBalancete(Long id, LocalDate data, String descricao, TipoTransacao tipo, BigDecimal valor,
			Long contaId, Long categoriaId, String categoriaNome, OrigemLinha origem, Long faturaId) {
	}

	record TotaisBalancete(BigDecimal receitas, BigDecimal gastosDiretos, BigDecimal gastosFaturas,
			BigDecimal pagamentosFaturas, BigDecimal resultadoCompetencia, BigDecimal resultadoCaixa) {
	}

	enum OrigemLinha {
		DIRETA, FATURA, PAGAMENTO_FATURA
	}

	enum SituacaoResultado {
		POSITIVO, NEGATIVO, NEUTRO
	}

	enum OrigemResumo {
		MOVIMENTACAO_DIRETA, RECORRENCIA, COMPRA_PARCELADA, CARTAO, PAGAMENTO_FATURA, INVESTIMENTO
	}
}
