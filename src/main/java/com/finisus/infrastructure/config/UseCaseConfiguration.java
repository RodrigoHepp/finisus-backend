package com.finisus.infrastructure.config;

import com.finisus.application.ports.in.*;
import com.finisus.application.ports.out.*;
import com.finisus.application.service.*;
import com.finisus.infrastructure.observability.OperacaoFinanceiraMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class UseCaseConfiguration {
	@Bean
	AutenticacaoService autenticacaoService(UsuarioRepositoryPort u, PasswordEncoderPort p, TokenPort t,
			RefreshTokenRepositoryPort r, ObterDataAtualPort d) {
		return new AutenticacaoService(u, p, t, r, d);
	}

	@Bean
	CartaoCreditoService cartaoCreditoService(CartaoCreditoRepositoryPort r) {
		return new CartaoCreditoService(r);
	}

	@Bean
	@Primary
	CartaoCreditoUseCase cartaoCreditoUseCase(CartaoCreditoService s) {
		return s;
	}

	@Bean
	FaturaService faturaService(FaturaRepositoryPort f, CartaoCreditoUseCase c, ContaRepositoryPort co,
			CategoriaRepositoryPort ca, ItemRepositoryPort i, TransacaoRepositoryPort t, ObterDataAtualPort d,
			EstornarTransacaoVinculadaUseCase e, PagamentoFaturaRepositoryPort p,
			AplicacaoCreditoFaturaRepositoryPort ac) {
		return new FaturaService(f, c, co, ca, i, t, d, e, p, ac);
	}

	@Bean
	@Primary
	FaturaUseCase faturaUseCase(FaturaService s, OperacaoFinanceiraMetrics m) {
		return new FaturaUseCase() {
			public com.finisus.domain.model.Fatura criar(Long u, CriarCommand c) {
				return s.criar(u, c);
			}

			public com.finisus.domain.model.Fatura buscar(Long u, Long id) {
				return s.buscar(u, id);
			}

			public Detalhe buscarDetalhe(Long u, Long id) {
				return s.buscarDetalhe(u, id);
			}

			public java.util.Map<Long, java.math.BigDecimal> buscarValoresEmAberto(Long u, java.util.List<Long> ids) {
				return s.buscarValoresEmAberto(u, ids);
			}

			public java.util.List<com.finisus.domain.model.Fatura> listar(Long u, Long id) {
				return s.listar(u, id);
			}

			public com.finisus.application.pagination.Pagina<com.finisus.domain.model.Fatura> listar(Long u,
					Long id, com.finisus.application.pagination.Paginacao p) {
				return s.listar(u, id, p);
			}

			public com.finisus.domain.model.Transacao lancarGasto(Long u, LancarGastoCommand c) {
				return m.medir("gasto_cartao", () -> s.lancarGasto(u, c));
			}

			public com.finisus.domain.model.Transacao lancarCredito(Long u, LancarGastoCommand c) {
				return m.medir("credito_fatura", () -> s.lancarCredito(u, c));
			}

			public com.finisus.domain.model.Fatura fechar(Long u, Long id) {
				return s.fechar(u, id);
			}

			public ResultadoProcessamentoCiclo processarCiclos(Long u, java.time.LocalDate d) {
				return m.medir("processamento_ciclo_fatura", () -> s.processarCiclos(u, d));
			}

			public com.finisus.domain.model.Fatura pagar(Long u, Long id, String k, PagamentoCommand c) {
				return m.medir("pagamento_fatura", () -> s.pagar(u, id, k, c));
			}

			public com.finisus.domain.model.Fatura estornarPagamento(Long u, Long id) {
				return m.medir("estorno_pagamento_fatura", () -> s.estornarPagamento(u, id));
			}

			public com.finisus.domain.model.Fatura atualizar(Long u, Long id, AtualizarCommand c) {
				return s.atualizar(u, id, c);
			}

			public com.finisus.domain.model.Fatura cancelar(Long u, Long id) {
				return s.cancelar(u, id);
			}
		};
	}

	@Bean
	ImportacaoFinanceiraService importacaoFinanceiraService(BancoUseCase b, ImportacaoFinanceiraRepositoryPort i,
			LeitorDocumentoFinanceiroPort l, TransacaoRepositoryPort t, RegistrarTransacaoUseCase r, FaturaUseCase f,
			ObrigacaoFinanceiraUseCase o, ObrigacaoFinanceiraRepositoryPort or, ContaRepositoryPort co,
			CategoriaRepositoryPort ca, ItemRepositoryPort it, UsuarioRepositoryPort u, OperacaoFinanceiraMetrics m) {
		return new ImportacaoFinanceiraService(b, i, l, t, r, f, o, or, co, ca, it, u, m);
	}

	@Bean
	@Primary
	ImportacaoFinanceiraUseCase importacaoFinanceiraUseCase(ImportacaoFinanceiraService s,
			OperacaoFinanceiraMetrics m) {
		return new ImportacaoFinanceiraUseCase() {
			public Revisao iniciar(Long u, IniciarCommand c) {
				return m.medir("importacao_inicio", () -> s.iniciar(u, c));
			}

			public Revisao buscar(Long u, Long id) {
				return m.medir("importacao_consulta", () -> s.buscar(u, id));
			}

			public Revisao revisar(Long u, Long id, RevisarCommand c) {
				return m.medir("importacao_revisao", () -> s.revisar(u, id, c));
			}

			public com.finisus.domain.model.ImportacaoFinanceira confirmar(Long u, Long id) {
				return m.medir("importacao_confirmacao", () -> s.confirmar(u, id));
			}
		};
	}

	@Bean
	ObrigacaoFinanceiraService obrigacaoFinanceiraService(ObrigacaoFinanceiraRepositoryPort o, ContaRepositoryPort c,
			CategoriaRepositoryPort ca, RegistrarTransacaoUseCase t, ObterDataAtualPort d,
			EstornarTransacaoVinculadaUseCase e, PagamentoObrigacaoRepositoryPort p) {
		return new ObrigacaoFinanceiraService(o, c, ca, t, d, e, p);
	}

	@Bean
	@Primary
	ObrigacaoFinanceiraUseCase obrigacaoFinanceiraUseCase(ObrigacaoFinanceiraService s,
			OperacaoFinanceiraMetrics m) {
		return new ObrigacaoFinanceiraUseCase() {
			public com.finisus.domain.model.ObrigacaoFinanceira criar(Long u, CriarCommand c) {
				return m.medir("criacao_obrigacao", () -> s.criar(u, c));
			}

			public com.finisus.domain.model.ObrigacaoFinanceira buscar(Long u, Long id) {
				return s.buscar(u, id);
			}

			public com.finisus.application.pagination.Pagina<com.finisus.domain.model.ObrigacaoFinanceira> listar(Long u,
					FiltroListagem f, com.finisus.application.pagination.Paginacao p) {
				return s.listar(u, f, p);
			}

			public com.finisus.domain.model.ObrigacaoFinanceira pagar(Long u, Long id, PagamentoCommand c) {
				return m.medir("pagamento_obrigacao", () -> s.pagar(u, id, c));
			}

			public java.util.List<com.finisus.domain.model.PagamentoObrigacao> listarPagamentos(Long u, Long id) {
				return s.listarPagamentos(u, id);
			}

			public com.finisus.domain.model.ObrigacaoFinanceira estornarPagamento(Long u, Long id, Long p) {
				return m.medir("estorno_pagamento_obrigacao", () -> s.estornarPagamento(u, id, p));
			}

			public com.finisus.domain.model.ObrigacaoFinanceira estornarPagamento(Long u, Long id) {
				return m.medir("estorno_pagamento_obrigacao", () -> s.estornarPagamento(u, id));
			}

			public com.finisus.domain.model.ObrigacaoFinanceira cancelar(Long u, Long id) {
				return s.cancelar(u, id);
			}

			public int processarVencimentos(java.time.LocalDate d) {
				return s.processarVencimentos(d);
			}
		};
	}

	@Bean
	DashboardFinanceiroService dashboardFinanceiroService(DashboardFinanceiroRepositoryPort d) {
		return new DashboardFinanceiroService(d);
	}

	@Bean
	@Primary
	DashboardFinanceiroUseCase dashboardFinanceiroUseCase(DashboardFinanceiroService s, OperacaoFinanceiraMetrics m) {
		return new DashboardFinanceiroUseCase() {
			public DashboardMensal consultarMensal(Long u, String a) {
				return m.medir("consulta_dashboard_mensal", () -> s.consultarMensal(u, a));
			}

			public ResumoPeriodo consultarResumoPeriodo(Long u, String mesFinal, int periodoMeses) {
				return m.medir("consulta_dashboard_periodo", () -> s.consultarResumoPeriodo(u, mesFinal, periodoMeses));
			}

			public ResumoAnual consultarAnual(Long u, String ano) {
				return m.medir("consulta_dashboard_anual", () -> s.consultarAnual(u, ano));
			}

			public ComposicaoAnual consultarComposicaoAnual(Long u, String ano) {
				return m.medir("consulta_dashboard_anual_composicao", () -> s.consultarComposicaoAnual(u, ano));
			}

			public Balancete consultarBalancete(Long u, ConsultaBalancete c,
					com.finisus.application.pagination.Paginacao p) {
				return m.medir("consulta_dashboard_balancete", () -> s.consultarBalancete(u, c, p));
			}

			public Balancete consultarBalanceteAnual(Long u, ConsultaBalanceteAnual c,
					com.finisus.application.pagination.Paginacao p) {
				return m.medir("consulta_dashboard_balancete_anual", () -> s.consultarBalanceteAnual(u, c, p));
			}
		};
	}

	@Bean
	PainelFinanceiroService painelFinanceiroService(ContaRepositoryPort c, InvestimentoRepositoryPort i,
			MovimentoInvestimentoRepositoryPort m, PosicaoInvestimentoRepositoryPort p, FaturaRepositoryPort f,
			FaturaUseCase fu, ObrigacaoFinanceiraRepositoryPort o, ParcelaFinanciamentoRepositoryPort pa,
			RecorrenciaRepositoryPort r, TransacaoRepositoryPort t, DashboardFinanceiroUseCase d,
			DashboardFinanceiroRepositoryPort dd, DivisaoCompartilhadaRepositoryPort dc,
			ConsultarResumoDivisaoUseCase rd, UsuarioRepositoryPort u, java.time.Clock clock) {
		return new PainelFinanceiroService(c, i, m, p, f, fu, o, pa, r, t, d, dd, dc, rd, u, clock);
	}

	@Bean
	@Primary
	PainelFinanceiroUseCase painelFinanceiroUseCase(PainelFinanceiroService s, OperacaoFinanceiraMetrics m) {
		return new PainelFinanceiroUseCase() {
			public VisaoGeral consultarVisaoGeral(Long u, java.time.LocalDate r, int j) {
				return m.medir("consulta_painel_visao_geral", () -> s.consultarVisaoGeral(u, r, j));
			}

			public AgendaFinanceira consultarAgenda(Long u, java.time.LocalDate i, java.time.LocalDate f) {
				return m.medir("consulta_painel_agenda", () -> s.consultarAgenda(u, i, f));
			}

			public Patrimonio consultarPatrimonio(Long u, java.time.LocalDate r) {
				return m.medir("consulta_painel_patrimonio", () -> s.consultarPatrimonio(u, r));
			}

			public Compartilhados consultarCompartilhados(Long u, java.time.LocalDate i, java.time.LocalDate f) {
				return m.medir("consulta_painel_compartilhados", () -> s.consultarCompartilhados(u, i, f));
			}

			public AnaliseReceitasGastos consultarReceitasEGastos(Long u, String f, int p) {
				return m.medir("consulta_painel_receitas_gastos", () -> s.consultarReceitasEGastos(u, f, p));
			}
		};
	}

	@Bean
	ConfiguracaoCompartilhamentoService configuracaoCompartilhamentoService(
			ConfiguracaoCompartilhamentoRepositoryPort r) {
		return new ConfiguracaoCompartilhamentoService(r);
	}

	@Bean
	@Primary
	ConfiguracaoCompartilhamentoUseCase configuracaoCompartilhamentoUseCase(ConfiguracaoCompartilhamentoService s) {
		return s;
	}

	@Bean
	DivisaoCompartilhadaService divisaoCompartilhadaService(DivisaoCompartilhadaRepositoryPort d,
			VinculoTransacaoDivisaoRepositoryPort v, TransacaoRepositoryPort t, UsuarioRepositoryPort u,
			com.finisus.application.ports.out.AlocacaoPagamentoDivisaoRepositoryPort alocacoes,
			com.finisus.application.ports.out.ReembolsoDivisaoRepositoryPort reembolsos) {
		return new DivisaoCompartilhadaService(d, v, t, u, alocacoes, reembolsos);
	}

	@Bean
	@Primary
	DivisaoCompartilhadaUseCase divisaoCompartilhadaUseCase(DivisaoCompartilhadaService s) {
		return new DivisaoCompartilhadaUseCase() {
			public com.finisus.domain.model.DivisaoCompartilhada criar(Long u, CriarCommand c) {
				return s.criar(u, c);
			}

			public com.finisus.application.pagination.Pagina<com.finisus.domain.model.DivisaoCompartilhada> listar(Long u,
					com.finisus.application.pagination.Paginacao p) {
				return s.listar(u, p);
			}

			public com.finisus.domain.model.DivisaoCompartilhada buscar(Long u, Long id) {
				return s.buscar(u, id);
			}

			public com.finisus.domain.model.DivisaoCompartilhada atualizarParticipantes(Long u, Long id,
					java.util.List<ParticipanteCommand> p) {
				return s.atualizarParticipantes(u, id, p);
			}

			public com.finisus.domain.model.DivisaoCompartilhada inativar(Long u, Long id) {
				return s.inativar(u, id);
			}

			public java.util.List<com.finisus.domain.model.HistoricoParticipanteDivisao> listarHistoricoParticipantes(
					Long u, Long id) {
				return s.listarHistoricoParticipantes(u, id);
			}
		};
	}

	@Bean
	@Primary
	AssociarTransacaoDivisaoUseCase associarTransacaoDivisaoUseCase(DivisaoCompartilhadaService s) {
		return new AssociarTransacaoDivisaoUseCase() {
			public void associar(Long u, Long divisaoId, Long transacaoId) {
				s.associar(u, divisaoId, transacaoId);
			}

			public void associar(Long u, Long divisaoId, Long transacaoId, java.math.BigDecimal baseCompartilhada,
					java.util.List<ResponsabilidadeCommand> responsabilidades) {
				s.associar(u, divisaoId, transacaoId, baseCompartilhada, responsabilidades);
			}

			public void desassociar(Long u, Long divisaoId, Long transacaoId) {
				s.desassociar(u, divisaoId, transacaoId);
			}

			public java.util.List<VinculoPendente> listarPendentes(Long u, Long divisaoId) {
				return s.listarPendentes(u, divisaoId);
			}

			public void revisar(Long u, Long divisaoId, Long transacaoId,
					java.util.List<ResponsabilidadeCommand> responsabilidades) {
				s.revisar(u, divisaoId, transacaoId, responsabilidades);
			}

			public java.util.List<com.finisus.domain.model.AlocacaoPagamentoDivisao> listarAlocacoes(
					Long u, Long divisaoId, Long transacaoId) {
				return s.listarAlocacoes(u, divisaoId, transacaoId);
			}

			public java.util.List<com.finisus.domain.model.AlocacaoPagamentoDivisao> substituirAlocacoes(
					Long u, Long divisaoId, Long transacaoId, java.util.List<AlocacaoCommand> alocacoes) {
				return s.substituirAlocacoes(u, divisaoId, transacaoId, alocacoes);
			}

			public com.finisus.domain.model.ResumoPagamentoDivisao consultarPagamento(
					Long u, Long divisaoId, Long transacaoId) {
				return s.consultarPagamento(u, divisaoId, transacaoId);
			}

			public void cancelarAlocacao(Long u, Long divisaoId, Long transacaoId, Long alocacaoId) {
				s.cancelarAlocacao(u, divisaoId, transacaoId, alocacaoId);
			}

			public com.finisus.domain.model.ReembolsoDivisao registrarReembolso(Long u, Long divisaoId,
					Long transacaoId, Long recebedorId, java.math.BigDecimal valor) {
				return s.registrarReembolso(u, divisaoId, transacaoId, recebedorId, valor);
			}

			public void cancelarReembolso(Long u, Long divisaoId, Long reembolsoId) {
				s.cancelarReembolso(u, divisaoId, reembolsoId);
			}

			public java.util.List<com.finisus.domain.model.ReembolsoDivisao> listarReembolsos(Long u, Long divisaoId) {
				return s.listarReembolsos(u, divisaoId);
			}
		};
	}

	@Bean
	@Primary
	ConsultarResumoDivisaoUseCase consultarResumoDivisaoUseCase(DivisaoCompartilhadaService s) {
		return s::consultarResumo;
	}

	@Bean
	CompraParceladaService compraParceladaService(CompraParceladaRepositoryPort c, ContaRepositoryPort co,
			CategoriaRepositoryPort ca, TransacaoRepositoryPort t, FaturaRepositoryPort f,
			CartaoCreditoUseCase cc, ObterDataAtualPort d) {
		return new CompraParceladaService(c, co, ca, t, f, cc, d);
	}

	@Bean
	@Primary
	CompraParceladaUseCase compraParceladaUseCase(CompraParceladaService s) {
		return s;
	}

	@Bean
	BancoService bancoService(BancoRepositoryPort r) {
		return new BancoService(r);
	}

	@Bean
	@Primary
	BancoUseCase bancoUseCase(BancoService s) {
		return s;
	}

	@Bean
	ContaService contaService(ContaRepositoryPort c, BancoRepositoryPort b) {
		return new ContaService(c, b);
	}

	@Bean
	ReconciliacaoSaldoContaService reconciliacaoSaldoContaService(ContaRepositoryPort c) {
		return new ReconciliacaoSaldoContaService(c);
	}

	@Bean
	@Primary
	ReconciliarSaldoContaUseCase reconciliarSaldoContaUseCase(ReconciliacaoSaldoContaService s,
			OperacaoFinanceiraMetrics m) {
		return (u, c) -> m.medir("reconciliacao_saldo", () -> {
			var resultado = s.reconciliar(u, c);
			if (!resultado.conciliado()) m.registrarDivergenciaSaldo();
			return resultado;
		});
	}

	@Bean
	AjustarSaldoContaUseCase ajustarSaldoContaUseCase(AjusteSaldoContaRepositoryPort a, ContaRepositoryPort c,
			ObterDataAtualPort d) {
		return new AjusteSaldoContaService(a, c, d);
	}

	@Bean
	TransferenciaContaService transferenciaContaService(TransferenciaContaRepositoryPort tr, ContaRepositoryPort c,
			TransacaoRepositoryPort t, ObterDataAtualPort d) {
		return new TransferenciaContaService(tr, c, t, d);
	}

	@Bean
	@Primary
	TransferenciaContaUseCase transferenciaContaUseCase(TransferenciaContaService s) {
		return s;
	}

	@Bean
	@Primary
	ContaUseCase contaUseCase(ContaService s) {
		return s;
	}

	@Bean
	CategoriaService categoriaService(CategoriaRepositoryPort r) {
		return new CategoriaService(r);
	}

	@Bean
	@Primary
	CategoriaUseCase categoriaUseCase(CategoriaService s) {
		return s;
	}

	@Bean
	ItemService itemService(ItemRepositoryPort i, CategoriaRepositoryPort c) {
		return new ItemService(i, c);
	}

	@Bean
	@Primary
	ItemUseCase itemUseCase(ItemService s) {
		return s;
	}

	@Bean
	MeioPagamentoService meioPagamentoService(MeioPagamentoRepositoryPort r) {
		return new MeioPagamentoService(r);
	}

	@Bean
	@Primary
	MeioPagamentoUseCase meioPagamentoUseCase(MeioPagamentoService s) {
		return s;
	}

	@Bean
	TransacaoService transacaoService(ContaRepositoryPort c, CategoriaRepositoryPort ca, MeioPagamentoRepositoryPort m,
			ItemRepositoryPort i, TransacaoRepositoryPort t, ObterDataAtualPort d,
			VinculoTransacaoDivisaoRepositoryPort v, ObrigacaoFinanceiraRepositoryPort o,
			ParcelaFinanciamentoRepositoryPort p) {
		return new TransacaoService(c, ca, m, i, t, d, v, o, p);
	}

	@Bean
	@Primary
	TransacaoUseCase transacaoUseCase(TransacaoService s, OperacaoFinanceiraMetrics m) {
		return new TransacaoUseCase() {
			public com.finisus.domain.model.Transacao registrar(Long u, RegistrarCommand c) {
				return m.medir("transacao", () -> s.registrar(u, c));
			}

			public com.finisus.domain.model.Transacao buscar(Long u, Long id) {
				return s.buscar(u, id);
			}

			public java.util.List<com.finisus.domain.model.Transacao> listar(Long u) {
				return s.listar(u);
			}

			public com.finisus.application.pagination.Pagina<com.finisus.domain.model.Transacao> listar(Long u,
					com.finisus.application.pagination.Paginacao p) {
				return s.listar(u, p);
			}

			public com.finisus.application.pagination.Pagina<com.finisus.domain.model.Transacao> listar(Long u,
					com.finisus.application.pagination.Paginacao p, FiltroListagem f) {
				return m.medir("consulta_transacoes", () -> s.listar(u, p, f));
			}

			public com.finisus.domain.model.Transacao corrigir(Long u, Long id, CorrigirCommand c) {
				return s.corrigir(u, id, c);
			}

			public com.finisus.domain.model.Transacao detalhar(Long u, Long id, DetalharCommand c) {
				return s.detalhar(u, id, c);
			}

			public com.finisus.domain.model.Transacao estornar(Long u, Long id) {
				return s.estornar(u, id);
			}
		};
	}

	@Bean
	@Primary
	RegistrarTransacaoUseCase registrarTransacaoUseCase(TransacaoService s, OperacaoFinanceiraMetrics m) {
		return (u, c) -> m.medir("transacao", () -> s.registrar(u, c));
	}

	@Bean
	ConsultarHistoricoTransacaoService consultarHistoricoTransacaoService(TransacaoRepositoryPort r) {
		return new ConsultarHistoricoTransacaoService(r);
	}

	@Bean
	@Primary
	ConsultarHistoricoTransacaoUseCase consultarHistoricoTransacaoUseCase(ConsultarHistoricoTransacaoService s) {
		return s;
	}

	@Bean
	InvestimentoService investimentoService(InvestimentoRepositoryPort i, ContaRepositoryPort c) {
		return new InvestimentoService(i, c);
	}

	@Bean
	@Primary
	InvestimentoUseCase investimentoUseCase(InvestimentoService s) {
		return s;
	}

	@Bean
	PosicaoInvestimentoService posicaoInvestimentoService(PosicaoInvestimentoRepositoryPort p,
			InvestimentoUseCase i) {
		return new PosicaoInvestimentoService(p, i);
	}

	@Bean
	@Primary
	PosicaoInvestimentoUseCase posicaoInvestimentoUseCase(PosicaoInvestimentoService s) {
		return s;
	}

	@Bean
	MovimentoInvestimentoService movimentoInvestimentoService(MovimentoInvestimentoRepositoryPort m,
			InvestimentoUseCase i, RegistrarTransacaoUseCase t, TransacaoUseCase tc, ObterDataAtualPort d) {
		return new MovimentoInvestimentoService(m, i, t, tc, d);
	}

	@Bean
	@Primary
	MovimentoInvestimentoUseCase movimentoInvestimentoUseCase(MovimentoInvestimentoService s) {
		return s;
	}

	@Bean
	PerfilUsuarioService perfilUsuarioService(UsuarioRepositoryPort u, RefreshTokenRepositoryPort r,
			DadosPessoaisPort d, SolicitacaoPrivacidadeRepositoryPort s, java.time.Clock c) {
		return new PerfilUsuarioService(u, r, d, s, c);
	}

	@Bean
	@Primary
	GerenciarPerfilUseCase gerenciarPerfilUseCase(PerfilUsuarioService s) {
		return s;
	}

	@Bean
	RecorrenciaService recorrenciaService(RecorrenciaRepositoryPort r, ContaRepositoryPort c,
			CategoriaRepositoryPort ca, MeioPagamentoRepositoryPort m, TransacaoRepositoryPort t,
			ObterDataAtualPort d) {
		return new RecorrenciaService(r, c, ca, m, t, d);
	}

	@Bean
	@Primary
	RecorrenciaUseCase recorrenciaUseCase(RecorrenciaService s) {
		return s;
	}

	@Bean
	ParcelaFinanciamentoService parcelaFinanciamentoService(ParcelaFinanciamentoRepositoryPort p,
			FinanciamentoRepositoryPort f, ContaRepositoryPort c, TransacaoRepositoryPort t, ObterDataAtualPort d,
			EstornarTransacaoVinculadaUseCase e) {
		return new ParcelaFinanciamentoService(p, f, c, t, d, e);
	}

	@Bean
	@Primary
	GerenciarParcelasFinanciamentoUseCase gerenciarParcelasFinanciamentoUseCase(ParcelaFinanciamentoService s) {
		return new GerenciarParcelasFinanciamentoUseCase() {
			public void gerar(com.finisus.domain.model.Financiamento f) {
				s.gerar(f);
			}

			public int excluirPendentesAPartirDe(com.finisus.domain.model.Financiamento f, Long id) {
				return s.excluirPendentesAPartirDe(f, id);
			}

			public java.util.List<com.finisus.domain.model.ParcelaFinanciamento> excluirERecalcular(
					com.finisus.domain.model.Financiamento f, Long id) {
				return s.excluirERecalcular(f, id);
			}

			public AmortizacaoCronograma amortizar(Long u, com.finisus.domain.model.Financiamento f,
					java.math.BigDecimal v, java.time.LocalDate d, Integer n,
					com.finisus.domain.model.ModalidadeAmortizacaoFinanciamento m) {
				return s.amortizar(u, f, v, d, n, m);
			}
		};
	}

	@Bean
	FinanciamentoService financiamentoService(FinanciamentoRepositoryPort f, ContaRepositoryPort c,
			GerenciarParcelasFinanciamentoUseCase p, ParcelaFinanciamentoRepositoryPort pp, ObterDataAtualPort d) {
		return new FinanciamentoService(f, c, p, pp, d);
	}

	@Bean
	@Primary
	FinanciamentoUseCase financiamentoUseCase(FinanciamentoService s) {
		return s;
	}

	@Bean
	@Primary
	ParcelaFinanciamentoUseCase parcelaFinanciamentoUseCase(ParcelaFinanciamentoService s,
			OperacaoFinanceiraMetrics m) {
		return new ParcelaFinanciamentoUseCase() {
			public java.util.List<com.finisus.domain.model.ParcelaFinanciamento> listar(Long u, Long f) {
				return s.listar(u, f);
			}

			public com.finisus.application.pagination.Pagina<com.finisus.domain.model.ParcelaFinanciamento> listar(
					Long u, Long f, com.finisus.application.pagination.Paginacao p) {
				return s.listar(u, f, p);
			}

			public com.finisus.domain.model.ParcelaFinanciamento pagarParcela(Long u, Long f, Long id,
					java.time.LocalDate data) {
				return m.medir("pagamento_parcela", () -> s.pagarParcela(u, f, id, data));
			}

			public com.finisus.domain.model.ParcelaFinanciamento estornarPagamento(Long u, Long f, Long id) {
				return m.medir("estorno_pagamento_parcela", () -> s.estornarPagamento(u, f, id));
			}

			public int processarAtrasos(ProcessarAtrasosCommand c) {
				return s.processarAtrasos(c);
			}
		};
	}

	@Bean
	PrevisaoFluxoCaixaService previsaoFluxoCaixaService(PrevisaoMensalRepositoryPort p, RecorrenciaRepositoryPort r,
			TransacaoRepositoryPort t, ParcelaFinanciamentoRepositoryPort pa, ObterDataAtualPort d) {
		return new PrevisaoFluxoCaixaService(p, r, t, pa, d);
	}

	@Bean
	@Primary
	PrevisaoFluxoCaixaUseCase previsaoFluxoCaixaUseCase(PrevisaoFluxoCaixaService s) {
		return s;
	}
}
