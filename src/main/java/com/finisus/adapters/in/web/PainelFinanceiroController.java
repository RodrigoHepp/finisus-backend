package com.finisus.adapters.in.web;

import com.finisus.adapters.in.web.security.UsuarioAtual;
import com.finisus.application.ports.in.PainelFinanceiroUseCase;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Validated
@Tag(name = "Painéis financeiros", description = "Visão geral, agenda, patrimônio e consolidação de compartilhados.")
@SecurityRequirement(name = "bearerAuth")
public class PainelFinanceiroController {
	private final PainelFinanceiroUseCase useCase;

	public PainelFinanceiroController(PainelFinanceiroUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping("/visao-geral")
	VisaoGeralResponse visaoGeral(@UsuarioAtual Long usuarioId, @RequestParam @NotNull LocalDate referencia,
			@RequestParam(defaultValue = "30") @Min(1) @Max(90) int janelaDias) {
		return VisaoGeralResponse.from(useCase.consultarVisaoGeral(usuarioId, referencia, janelaDias));
	}

	@GetMapping("/agenda")
	AgendaResponse agenda(@UsuarioAtual Long usuarioId, @RequestParam @NotNull LocalDate inicio,
			@RequestParam @NotNull LocalDate fim) {
		return AgendaResponse.from(useCase.consultarAgenda(usuarioId, inicio, fim));
	}

	@GetMapping("/patrimonio")
	PatrimonioResponse patrimonio(@UsuarioAtual Long usuarioId, @RequestParam @NotNull LocalDate referencia) {
		return PatrimonioResponse.from(useCase.consultarPatrimonio(usuarioId, referencia));
	}

	@GetMapping("/compartilhados")
	CompartilhadosResponse compartilhados(@UsuarioAtual Long usuarioId, @RequestParam @NotNull LocalDate inicio,
			@RequestParam @NotNull LocalDate fim) {
		return CompartilhadosResponse.from(useCase.consultarCompartilhados(usuarioId, inicio, fim));
	}

	@GetMapping("/receitas-gastos")
	AnaliseReceitasGastosResponse receitasEGastos(@UsuarioAtual Long usuarioId, @RequestParam String mesFinal,
			@RequestParam(defaultValue = "3") @Min(1) @Max(12) int periodoMeses) {
		return AnaliseReceitasGastosResponse.from(useCase.consultarReceitasEGastos(usuarioId, mesFinal, periodoMeses));
	}

	record VisaoGeralResponse(LocalDate referencia, BigDecimal saldoContas, BigDecimal resultadoCompetenciaOperacional,
			BigDecimal resultadoCaixa, BigDecimal comprometidoNaJanela, BigDecimal saldoLivreNaJanela,
			List<CategoriaGastoResponse> maioresGastos, List<AlertaResponse> alertas) {
		static VisaoGeralResponse from(PainelFinanceiroUseCase.VisaoGeral visao) {
			return new VisaoGeralResponse(visao.referencia(), visao.saldoContas(), visao.resultadoCompetenciaOperacional(),
					visao.resultadoCaixa(), visao.comprometidoNaJanela(), visao.saldoLivreNaJanela(),
					visao.maioresGastos().stream().map(CategoriaGastoResponse::from).toList(),
					visao.alertas().stream().map(AlertaResponse::from).toList());
		}
	}

	record CategoriaGastoResponse(Long categoriaId, String categoria, BigDecimal valor) {
		static CategoriaGastoResponse from(PainelFinanceiroUseCase.CategoriaGasto categoria) {
			return new CategoriaGastoResponse(categoria.categoriaId(), categoria.categoria(), categoria.valor());
		}
	}

	record AlertaResponse(String tipo, String descricao, LocalDate vencimento, BigDecimal valor, String nivel) {
		static AlertaResponse from(PainelFinanceiroUseCase.AlertaFinanceiro alerta) {
			return new AlertaResponse(alerta.tipo(), alerta.descricao(), alerta.vencimento(), alerta.valor(), alerta.nivel());
		}
	}

	record AgendaResponse(LocalDate inicio, LocalDate fim, BigDecimal totalComprometido,
			List<CompromissoResponse> compromissos) {
		static AgendaResponse from(PainelFinanceiroUseCase.AgendaFinanceira agenda) {
			return new AgendaResponse(agenda.inicio(), agenda.fim(), agenda.totalComprometido(),
					agenda.compromissos().stream().map(CompromissoResponse::from).toList());
		}
	}

	record CompromissoResponse(String tipo, Long referenciaId, String descricao, LocalDate vencimento, BigDecimal valor,
			String situacao) {
		static CompromissoResponse from(PainelFinanceiroUseCase.CompromissoFinanceiro compromisso) {
			return new CompromissoResponse(compromisso.tipo(), compromisso.referenciaId(), compromisso.descricao(),
					compromisso.vencimento(), compromisso.valor(), compromisso.situacao());
		}
	}

	record PatrimonioResponse(LocalDate referencia, Instant saldosEDividasConsultadosEm,
			boolean patrimonioHistoricoCompleto, BigDecimal saldoContas, BigDecimal capitalLiquidoInvestido,
			BigDecimal valorAtualInvestimentos, BigDecimal faturasEmAberto, BigDecimal obrigacoesEmAberto,
			BigDecimal parcelasFinanciamentoEmAberto, BigDecimal principalFinanciamentosEmAberto,
			BigDecimal jurosEncargosFinanciamentosFuturos, BigDecimal parcelasFinanciamentoSemComposicao,
			BigDecimal comprasParceladasRestantes, BigDecimal dividasPrincipais, BigDecimal dividasECompromissos,
			BigDecimal patrimonioLiquido,
			List<PosicaoInvestimentoResponse> investimentos) {
		static PatrimonioResponse from(PainelFinanceiroUseCase.Patrimonio patrimonio) {
			return new PatrimonioResponse(patrimonio.referencia(), patrimonio.saldosEDividasConsultadosEm(),
					patrimonio.patrimonioHistoricoCompleto(), patrimonio.saldoContas(),
					patrimonio.capitalLiquidoInvestido(), patrimonio.valorAtualInvestimentos(),
					patrimonio.faturasEmAberto(), patrimonio.obrigacoesEmAberto(),
					patrimonio.parcelasFinanciamentoEmAberto(), patrimonio.principalFinanciamentosEmAberto(),
					patrimonio.jurosEncargosFinanciamentosFuturos(), patrimonio.parcelasFinanciamentoSemComposicao(),
					patrimonio.comprasParceladasRestantes(), patrimonio.dividasPrincipais(),
					patrimonio.dividasECompromissos(), patrimonio.patrimonioLiquido(),
					patrimonio.investimentos().stream().map(PosicaoInvestimentoResponse::from).toList());
		}
	}

	record PosicaoInvestimentoResponse(Long investimentoId, String investimento, String tipo, BigDecimal capitalLiquido,
			BigDecimal valorAtual, LocalDate dataPosicao, boolean posicaoInformada) {
		static PosicaoInvestimentoResponse from(PainelFinanceiroUseCase.PosicaoInvestimento posicao) {
			return new PosicaoInvestimentoResponse(posicao.investimentoId(), posicao.investimento(), posicao.tipo(),
					posicao.capitalLiquido(), posicao.valorAtual(), posicao.dataPosicao(), posicao.posicaoInformada());
		}
	}

	record CompartilhadosResponse(LocalDate inicio, LocalDate fim, BigDecimal aReceber, BigDecimal aPagar,
			List<ResumoDivisaoCompartilhadaResponse> divisoes) {
		static CompartilhadosResponse from(PainelFinanceiroUseCase.Compartilhados compartilhados) {
			return new CompartilhadosResponse(compartilhados.inicio(), compartilhados.fim(), compartilhados.aReceber(),
					compartilhados.aPagar(), compartilhados.divisoes().stream()
							.map(ResumoDivisaoCompartilhadaResponse::from).toList());
		}
	}

	record ResumoDivisaoCompartilhadaResponse(Long divisaoId, String divisao, BigDecimal total, BigDecimal meuPago,
			BigDecimal meuDevido, BigDecimal meuSaldo, List<ParticipanteCompartilhadoResponse> participantes) {
		static ResumoDivisaoCompartilhadaResponse from(PainelFinanceiroUseCase.ResumoDivisaoCompartilhada resumo) {
			return new ResumoDivisaoCompartilhadaResponse(resumo.divisaoId(), resumo.divisao(), resumo.total(),
					resumo.meuPago(), resumo.meuDevido(), resumo.meuSaldo(), resumo.participantes().stream()
							.map(ParticipanteCompartilhadoResponse::from).toList());
		}
	}

	record ParticipanteCompartilhadoResponse(Long usuarioId, String nomeExibicao, BigDecimal pago, BigDecimal devido,
			BigDecimal saldo) {
		static ParticipanteCompartilhadoResponse from(PainelFinanceiroUseCase.ParticipanteCompartilhado participante) {
			return new ParticipanteCompartilhadoResponse(participante.usuarioId(), participante.nomeExibicao(), participante.pago(),
					participante.devido(), participante.saldo());
		}
	}

	record AnaliseReceitasGastosResponse(String mesInicial, String mesFinal, int periodoMeses,
			BigDecimal receitasOperacionais, BigDecimal gastosDeConsumo, BigDecimal resultadoOperacional,
			BigDecimal despesasFixasPrevistas, List<CategoriaAnaliseResponse> categorias) {
		static AnaliseReceitasGastosResponse from(PainelFinanceiroUseCase.AnaliseReceitasGastos analise) {
			return new AnaliseReceitasGastosResponse(analise.mesInicial(), analise.mesFinal(), analise.periodoMeses(),
					analise.receitasOperacionais(), analise.gastosDeConsumo(), analise.resultadoOperacional(),
					analise.despesasFixasPrevistas(), analise.categorias().stream().map(CategoriaAnaliseResponse::from).toList());
		}
	}

	record CategoriaAnaliseResponse(Long categoriaId, String categoria, BigDecimal receitas, BigDecimal despesas,
			BigDecimal saldo) {
		static CategoriaAnaliseResponse from(PainelFinanceiroUseCase.CategoriaAnalise categoria) {
			return new CategoriaAnaliseResponse(categoria.categoriaId(), categoria.categoria(), categoria.receitas(),
					categoria.despesas(), categoria.saldo());
		}
	}
}
