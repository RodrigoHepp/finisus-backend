package com.finisus.adapters.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import com.finisus.adapters.in.web.security.UsuarioAtual;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.MovimentoInvestimentoUseCase;
import com.finisus.domain.model.MovimentoInvestimento;
import com.finisus.domain.model.TipoMovimentoInvestimento;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Movimentos de investimento", description = "Aportes e resgates de investimentos.")
@SecurityRequirement(name = "bearerAuth")
public class MovimentoInvestimentoController {
	private final MovimentoInvestimentoUseCase useCase;

	public MovimentoInvestimentoController(MovimentoInvestimentoUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping("/investimentos/{investimentoId}/movimentos")
	PaginaResponse<Response> listar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long investimentoId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
		return PaginaResponse
				.from(useCase.listar(usuarioId, investimentoId, new Paginacao(pagina, tamanho)).map(Response::from));
	}

	@PostMapping("/investimentos/movimentos")
	@ResponseStatus(HttpStatus.CREATED)
	Response movimentar(@UsuarioAtual Long usuarioId, @Valid @RequestBody Request request) {
		return Response.from(useCase.movimentar(usuarioId, new MovimentoInvestimentoUseCase.MovimentoCommand(
				request.investimentoId(), request.tipo(), request.valor(), request.data())));
	}

	@PostMapping("/investimentos/movimentos/{movimentoId}/estornar")
	Response estornar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long movimentoId) {
		return Response.from(useCase.estornar(usuarioId, movimentoId));
	}

	record Request(@NotNull @Positive Long investimentoId, @NotNull TipoMovimentoInvestimento tipo,
			@NotNull @DecimalMin("0.01") BigDecimal valor, @NotNull LocalDate data) {
	}

	record Response(Long id, Long investimentoId, TipoMovimentoInvestimento tipo, BigDecimal valor, LocalDate data,
			Long transacaoId, Long movimentoOrigemId, java.time.LocalDateTime estornadoEm) {
		static Response from(MovimentoInvestimento movimento) {
			return new Response(movimento.getId(), movimento.getInvestimentoId(), movimento.getTipo(),
					movimento.getValor().valor(), movimento.getData(), movimento.getTransacaoId(),
					movimento.getMovimentoOrigemId(), movimento.getEstornadoEm());
		}
	}
}
