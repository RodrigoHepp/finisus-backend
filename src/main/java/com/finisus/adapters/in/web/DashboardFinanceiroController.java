package com.finisus.adapters.in.web;

import com.finisus.adapters.in.web.security.UsuarioAtual;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase;
import com.finisus.domain.model.TipoTransacao;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
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
@Tag(name = "Dashboard financeiro", description = "Resumo mensal, categorias e balancete.")
@SecurityRequirement(name = "bearerAuth")
public class DashboardFinanceiroController {
	private final DashboardFinanceiroUseCase useCase;

	public DashboardFinanceiroController(DashboardFinanceiroUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping("/mensal")
	MensalResponse consultarMensal(@UsuarioAtual Long usuarioId,
			@RequestParam @Pattern(regexp = "\\d{4}-\\d{2}") String anoMes) {
		return MensalResponse.from(useCase.consultarMensal(usuarioId, anoMes));
	}

	@GetMapping("/resumo")
	ResumoPeriodoResponse consultarResumoPeriodo(@UsuarioAtual Long usuarioId,
			@RequestParam @Pattern(regexp = "\\d{4}-\\d{2}") String mesFinal,
			@RequestParam(defaultValue = "1") @Positive int periodoMeses) {
		return ResumoPeriodoResponse.from(useCase.consultarResumoPeriodo(usuarioId, mesFinal, periodoMeses));
	}

	@GetMapping("/anual")
	ResumoAnualResponse consultarAnual(@UsuarioAtual Long usuarioId,
			@RequestParam @Pattern(regexp = "\\d{4}") String ano) {
		return ResumoAnualResponse.from(useCase.consultarAnual(usuarioId, ano));
	}

	@GetMapping("/anual/composicao")
	ComposicaoAnualResponse consultarComposicaoAnual(@UsuarioAtual Long usuarioId,
			@RequestParam @Pattern(regexp = "\\d{4}") String ano) {
		return ComposicaoAnualResponse.from(useCase.consultarComposicaoAnual(usuarioId, ano));
	}

	@GetMapping("/balancete")
	BalanceteResponse consultarBalancete(@UsuarioAtual Long usuarioId,
			@RequestParam @Pattern(regexp = "\\d{4}-\\d{2}") String anoMes,
			@RequestParam(required = false) TipoTransacao tipo,
			@RequestParam(required = false) @Positive Long categoriaId,
			@RequestParam(required = false) @Positive Long contaId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Positive @Max(100) int tamanho) {
		return BalanceteResponse.from(useCase.consultarBalancete(usuarioId,
				new DashboardFinanceiroUseCase.ConsultaBalancete(anoMes, tipo, categoriaId, contaId),
				new Paginacao(pagina, tamanho)));
	}

	@GetMapping("/balancete/anual")
	BalanceteResponse consultarBalanceteAnual(@UsuarioAtual Long usuarioId,
			@RequestParam @Pattern(regexp = "\\d{4}") String ano, @RequestParam(required = false) TipoTransacao tipo,
			@RequestParam(required = false) @Positive Long categoriaId,
			@RequestParam(required = false) @Positive Long contaId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Positive @Max(100) int tamanho) {
		return BalanceteResponse.from(useCase.consultarBalanceteAnual(usuarioId,
				new DashboardFinanceiroUseCase.ConsultaBalanceteAnual(ano, tipo, categoriaId, contaId),
				new Paginacao(pagina, tamanho)));
	}

	record MensalResponse(String anoMes, ResumoResponse resumo, List<CategoriaResponse> categorias) {
		static MensalResponse from(DashboardFinanceiroUseCase.DashboardMensal dashboard) {
			return new MensalResponse(dashboard.anoMes(), ResumoResponse.from(dashboard.resumo()),
					dashboard.categorias().stream().map(CategoriaResponse::from).toList());
		}
	}

	record ResumoResponse(BigDecimal receitas, BigDecimal gastosDiretos, BigDecimal gastosFaturas,
			BigDecimal valorFaturasPago, BigDecimal valorFaturasEmAberto, BigDecimal resultadoCompetencia,
			BigDecimal resultadoCaixa, String situacaoCompetencia, String situacaoCaixa) {
		static ResumoResponse from(DashboardFinanceiroUseCase.ResumoMensal resumo) {
			return new ResumoResponse(resumo.receitas(), resumo.gastosDiretos(), resumo.gastosFaturas(),
					resumo.valorFaturasPago(), resumo.valorFaturasEmAberto(), resumo.resultadoCompetencia(),
					resumo.resultadoCaixa(), resumo.situacaoCompetencia().name(), resumo.situacaoCaixa().name());
		}
	}

	record ResumoPeriodoResponse(String mesInicial, String mesFinal, int periodoMeses, TotaisPeriodoResponse totais,
			List<OrigemResumoResponse> origens) {
		static ResumoPeriodoResponse from(DashboardFinanceiroUseCase.ResumoPeriodo resumo) {
			return new ResumoPeriodoResponse(resumo.mesInicial(), resumo.mesFinal(), resumo.periodoMeses(),
					TotaisPeriodoResponse.from(resumo.totais()),
					resumo.origens().stream().map(OrigemResumoResponse::from).toList());
		}
	}

	record TotaisPeriodoResponse(BigDecimal receitas, BigDecimal despesas, BigDecimal pagamentosFaturas,
			BigDecimal resultadoCompetencia, BigDecimal resultadoCaixa) {
		static TotaisPeriodoResponse from(DashboardFinanceiroUseCase.TotaisPeriodo totais) {
			return new TotaisPeriodoResponse(totais.receitas(), totais.despesas(), totais.pagamentosFaturas(),
					totais.resultadoCompetencia(), totais.resultadoCaixa());
		}
	}

	record ResumoAnualResponse(String ano, List<ResumoMesResponse> meses, TotaisResponse totais) {
		static ResumoAnualResponse from(DashboardFinanceiroUseCase.ResumoAnual resumo) {
			return new ResumoAnualResponse(resumo.ano(), resumo.meses().stream().map(ResumoMesResponse::from).toList(),
					TotaisResponse.from(resumo.totais()));
		}
	}

	record ResumoMesResponse(String anoMes, BigDecimal receitas, BigDecimal gastosDiretos, BigDecimal gastosFaturas,
			BigDecimal pagamentosFaturas, BigDecimal resultadoCompetencia, BigDecimal resultadoCaixa) {
		static ResumoMesResponse from(DashboardFinanceiroUseCase.ResumoMes resumo) {
			return new ResumoMesResponse(resumo.anoMes(), resumo.receitas(), resumo.gastosDiretos(),
					resumo.gastosFaturas(), resumo.pagamentosFaturas(), resumo.resultadoCompetencia(),
					resumo.resultadoCaixa());
		}
	}

	record ComposicaoAnualResponse(String ano, TotaisPeriodoResponse totais, List<OrigemResumoResponse> origens,
			List<CategoriaResponse> categorias) {
		static ComposicaoAnualResponse from(DashboardFinanceiroUseCase.ComposicaoAnual composicao) {
			return new ComposicaoAnualResponse(composicao.ano(), TotaisPeriodoResponse.from(composicao.totais()),
					composicao.origens().stream().map(OrigemResumoResponse::from).toList(),
					composicao.categorias().stream().map(CategoriaResponse::from).toList());
		}
	}

	record OrigemResumoResponse(String origem, BigDecimal receitas, BigDecimal despesas, BigDecimal pagamentosFaturas) {
		static OrigemResumoResponse from(DashboardFinanceiroUseCase.ResumoOrigem origem) {
			return new OrigemResumoResponse(origem.origem().name(), origem.receitas(), origem.despesas(),
					origem.pagamentosFaturas());
		}
	}

	record CategoriaResponse(Long categoriaId, String categoriaNome, BigDecimal receitas, BigDecimal despesas,
			BigDecimal saldo) {
		static CategoriaResponse from(DashboardFinanceiroUseCase.ResumoCategoria categoria) {
			return new CategoriaResponse(categoria.categoriaId(), categoria.categoriaNome(), categoria.receitas(),
					categoria.despesas(), categoria.saldo());
		}
	}

	record BalanceteResponse(PaginaResponse<LinhaResponse> linhas, TotaisResponse totais) {
		static BalanceteResponse from(DashboardFinanceiroUseCase.Balancete balancete) {
			return new BalanceteResponse(PaginaResponse.from(balancete.linhas().map(LinhaResponse::from)),
					TotaisResponse.from(balancete.totais()));
		}
	}

	record LinhaResponse(Long id, LocalDate data, String descricao, TipoTransacao tipo, BigDecimal valor, Long contaId,
			Long categoriaId, String categoriaNome, String origem, Long faturaId) {
		static LinhaResponse from(DashboardFinanceiroUseCase.LinhaBalancete linha) {
			return new LinhaResponse(linha.id(), linha.data(), linha.descricao(), linha.tipo(), linha.valor(),
					linha.contaId(), linha.categoriaId(), linha.categoriaNome(), linha.origem().name(),
					linha.faturaId());
		}
	}

	record TotaisResponse(BigDecimal receitas, BigDecimal gastosDiretos, BigDecimal gastosFaturas,
			BigDecimal pagamentosFaturas, BigDecimal resultadoCompetencia, BigDecimal resultadoCaixa) {
		static TotaisResponse from(DashboardFinanceiroUseCase.TotaisBalancete totais) {
			return new TotaisResponse(totais.receitas(), totais.gastosDiretos(), totais.gastosFaturas(),
					totais.pagamentosFaturas(), totais.resultadoCompetencia(), totais.resultadoCaixa());
		}
	}
}
