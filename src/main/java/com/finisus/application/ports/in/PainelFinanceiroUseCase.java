package com.finisus.application.ports.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface PainelFinanceiroUseCase {
	VisaoGeral consultarVisaoGeral(Long usuarioId, LocalDate referencia, int janelaDias);
	AgendaFinanceira consultarAgenda(Long usuarioId, LocalDate inicio, LocalDate fim);
	Patrimonio consultarPatrimonio(Long usuarioId, LocalDate referencia);
	Compartilhados consultarCompartilhados(Long usuarioId, LocalDate inicio, LocalDate fim);
	AnaliseReceitasGastos consultarReceitasEGastos(Long usuarioId, String mesFinal, int periodoMeses);

	record VisaoGeral(LocalDate referencia, BigDecimal saldoContas, BigDecimal resultadoCompetenciaOperacional,
			BigDecimal resultadoCaixa, BigDecimal comprometidoNaJanela, BigDecimal saldoLivreNaJanela,
			List<CategoriaGasto> maioresGastos, List<AlertaFinanceiro> alertas) { }
	record CategoriaGasto(Long categoriaId, String categoria, BigDecimal valor) { }
	record AgendaFinanceira(LocalDate inicio, LocalDate fim, BigDecimal totalComprometido,
			List<CompromissoFinanceiro> compromissos) { }
	record CompromissoFinanceiro(String tipo, Long referenciaId, String descricao, LocalDate vencimento,
			BigDecimal valor, String situacao) { }
	record AlertaFinanceiro(String tipo, String descricao, LocalDate vencimento, BigDecimal valor, String nivel) { }
	record Patrimonio(LocalDate referencia, Instant saldosEDividasConsultadosEm, boolean patrimonioHistoricoCompleto,
			BigDecimal saldoContas, BigDecimal capitalLiquidoInvestido,
			BigDecimal valorAtualInvestimentos, BigDecimal faturasEmAberto, BigDecimal obrigacoesEmAberto,
			BigDecimal parcelasFinanciamentoEmAberto, BigDecimal principalFinanciamentosEmAberto,
			BigDecimal jurosEncargosFinanciamentosFuturos, BigDecimal parcelasFinanciamentoSemComposicao,
			BigDecimal comprasParceladasRestantes, BigDecimal dividasPrincipais, BigDecimal dividasECompromissos,
			BigDecimal patrimonioLiquido, List<PosicaoInvestimento> investimentos) { }
	record PosicaoInvestimento(Long investimentoId, String investimento, String tipo, BigDecimal capitalLiquido,
			BigDecimal valorAtual, LocalDate dataPosicao, boolean posicaoInformada) { }
	record Compartilhados(LocalDate inicio, LocalDate fim, BigDecimal aReceber, BigDecimal aPagar,
			List<ResumoDivisaoCompartilhada> divisoes) { }
	record ResumoDivisaoCompartilhada(Long divisaoId, String divisao, BigDecimal total, BigDecimal meuPago,
			BigDecimal meuDevido, BigDecimal meuSaldo, List<ParticipanteCompartilhado> participantes) { }
	record ParticipanteCompartilhado(Long usuarioId, String nomeExibicao, BigDecimal pago, BigDecimal devido,
			BigDecimal saldo) { }
	record AnaliseReceitasGastos(String mesInicial, String mesFinal, int periodoMeses, BigDecimal receitasOperacionais,
			BigDecimal gastosDeConsumo, BigDecimal resultadoOperacional, BigDecimal despesasFixasPrevistas,
			List<CategoriaAnalise> categorias) { }
	record CategoriaAnalise(Long categoriaId, String categoria, BigDecimal receitas, BigDecimal despesas,
			BigDecimal saldo) { }
}
