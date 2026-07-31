package com.financeiro.adapters.in.web;

import java.math.BigDecimal;
import java.util.List;

import com.financeiro.adapters.in.web.security.UsuarioAtual;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.PrevisaoFluxoCaixaUseCase;
import com.financeiro.domain.model.PrevisaoMensal;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/api/v1/previsoes")
@Validated
@Tag(name = "Previsões", description = "Previsão mensal de fluxo de caixa.")
@SecurityRequirement(name = "bearerAuth")
public class PrevisaoFluxoCaixaController {
	private final PrevisaoFluxoCaixaUseCase useCase;

	public PrevisaoFluxoCaixaController(PrevisaoFluxoCaixaUseCase useCase) {
		this.useCase = useCase;
	}

	@PostMapping("/recalcular")
	List<Response> recalcular(@UsuarioAtual Long usuarioId,
			@RequestParam(defaultValue = "3") @Min(1) @Max(12) int meses) {
		return useCase.recalcular(usuarioId, meses).stream().map(Response::from).toList();
	}

	@GetMapping("/{anoMes}")
	PaginaResponse<Response> consultar(@UsuarioAtual Long usuarioId,
			@PathVariable @Pattern(regexp = "\\d{4}-\\d{2}") String anoMes,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
		return PaginaResponse
				.from(useCase.consultar(usuarioId, anoMes, new Paginacao(pagina, tamanho)).map(Response::from));
	}

	record Response(String anoMes, Long categoriaId, BigDecimal entrada, BigDecimal saida) {
		static Response from(PrevisaoMensal previsao) {
			return new Response(previsao.getAnoMes().formatado(), previsao.getCategoriaId(),
					previsao.getValorProjetadoEntrada().valor(), previsao.getValorProjetadoSaida().valor());
		}
	}
}
