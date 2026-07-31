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
	@Primary
	CadastrarUsuarioUseCase cadastrarUsuarioUseCase(AutenticacaoService s) {
		return command -> s.executar(command);
	}

	@Bean
	@Primary
	AutenticarUsuarioUseCase autenticarUsuarioUseCase(AutenticacaoService s) {
		return command -> s.executar(command);
	}

	@Bean
	@Primary
	RenovarTokenUseCase renovarTokenUseCase(AutenticacaoService s) {
		return command -> s.executar(command);
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
			CategoriaRepositoryPort ca, ItemRepositoryPort i, TransacaoRepositoryPort t, ObterDataAtualPort d) {
		return new FaturaService(f, c, co, ca, i, t, d);
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

			public com.finisus.domain.model.Fatura fechar(Long u, Long id) {
				return s.fechar(u, id);
			}

			public com.finisus.domain.model.Fatura pagar(Long u, Long id, java.time.LocalDate d) {
				return m.medir("pagamento_fatura", () -> s.pagar(u, id, d));
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
	DespesaCompartilhadaService despesaCompartilhadaService(DespesaCompartilhadaRepositoryPort d,
			RateioDespesaRepositoryPort r, ConfiguracaoCompartilhamentoRepositoryPort c, TransacaoRepositoryPort t,
			UsuarioRepositoryPort u, ObterDataAtualPort dataAtual) {
		return new DespesaCompartilhadaService(d, r, c, t, u, dataAtual);
	}

	@Bean
	@Primary
	DespesaCompartilhadaUseCase despesaCompartilhadaUseCase(DespesaCompartilhadaService s) {
		return s;
	}

	@Bean
	RateioDespesaService rateioDespesaService(RateioDespesaRepositoryPort r, DespesaCompartilhadaRepositoryPort d) {
		return new RateioDespesaService(r, d);
	}

	@Bean
	@Primary
	RateioDespesaUseCase rateioDespesaUseCase(RateioDespesaService s) {
		return s;
	}

	@Bean
	CompraParceladaService compraParceladaService(CompraParceladaRepositoryPort c, ContaRepositoryPort co,
			CategoriaRepositoryPort ca, TransacaoRepositoryPort t, ObterDataAtualPort d) {
		return new CompraParceladaService(c, co, ca, t, d);
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
			DespesaCompartilhadaRepositoryPort dc) {
		return new TransacaoService(c, ca, m, i, t, d, dc);
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

			public com.finisus.domain.model.Transacao corrigir(Long u, Long id, RegistrarCommand c) {
				return s.corrigir(u, id, c);
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
	PerfilUsuarioService perfilUsuarioService(UsuarioRepositoryPort u, RefreshTokenRepositoryPort r) {
		return new PerfilUsuarioService(u, r);
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
			FinanciamentoRepositoryPort f, ContaRepositoryPort c, TransacaoRepositoryPort t, ObterDataAtualPort d) {
		return new ParcelaFinanciamentoService(p, f, c, t, d);
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
