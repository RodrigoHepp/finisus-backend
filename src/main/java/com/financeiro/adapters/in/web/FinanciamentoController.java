package com.financeiro.adapters.in.web;

import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.FinanciamentoUseCase;
import com.financeiro.domain.model.Financiamento;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import com.financeiro.adapters.in.web.security.UsuarioAtual;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/financiamentos")
@Validated
@Tag(name = "Financiamentos", description = "Cadastro e consulta de financiamentos.")
@SecurityRequirement(name = "bearerAuth")
public class FinanciamentoController {
	private final FinanciamentoUseCase useCase;

	public FinanciamentoController(FinanciamentoUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping
	PaginaResponse<Response> listar(@UsuarioAtual Long usuarioId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
		return PaginaResponse.from(useCase.listar(usuarioId, new Paginacao(pagina, tamanho)).map(Response::from));
	}

	@GetMapping("/{financiamentoId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long financiamentoId) {
		return Response.from(useCase.buscar(usuarioId, financiamentoId));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	Response criar(@UsuarioAtual Long usuarioId, @Valid @RequestBody Request request) {
		return Response.from(
				useCase.criar(usuarioId, new FinanciamentoUseCase.CriarCommand(request.descricao(), request.principal(),
						request.taxaJurosMensal(), request.numeroParcelas(), request.dataInicio(), request.contaId())));
	}

	@PostMapping("/{financiamentoId}/cancelar")
	Response cancelar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long financiamentoId) {
		return Response.from(useCase.cancelar(usuarioId, financiamentoId));
	}

	record Request(@NotBlank @Size(max = 300) String descricao, @NotNull @DecimalMin("0.01") BigDecimal principal,
			@NotNull @DecimalMin("0.00") BigDecimal taxaJurosMensal, @Min(1) int numeroParcelas,
			@NotNull LocalDate dataInicio, @NotNull @Positive Long contaId) {
	}

	record Response(Long id, String descricao, BigDecimal principal, BigDecimal taxaJurosMensal, int numeroParcelas,
			LocalDate dataInicio, Long contaId, com.financeiro.domain.model.StatusFinanciamento status) {
		static Response from(Financiamento financiamento) {
			return new Response(financiamento.getId(), financiamento.getDescricao(),
					financiamento.getPrincipal().valor(), financiamento.getTaxaJurosMensal(),
					financiamento.getNumeroParcelas(), financiamento.getDataInicio(), financiamento.getContaId(),
					financiamento.getStatus());
		}
	}
}
