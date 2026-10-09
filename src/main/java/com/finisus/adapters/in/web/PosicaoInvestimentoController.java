package com.finisus.adapters.in.web;

import com.finisus.adapters.in.web.security.UsuarioAtual;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.PosicaoInvestimentoUseCase;
import com.finisus.domain.model.PosicaoInvestimento;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/investimentos/{investimentoId}/posicoes")
@Validated
@Tag(name = "Posições de investimentos", description = "Posições manuais usadas na apuração patrimonial.")
@SecurityRequirement(name = "bearerAuth")
public class PosicaoInvestimentoController {
	private final PosicaoInvestimentoUseCase useCase;

	public PosicaoInvestimentoController(PosicaoInvestimentoUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping
	PaginaResponse<Response> listar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long investimentoId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Positive @Max(100) int tamanho) {
		return PaginaResponse.from(useCase.listar(usuarioId, investimentoId, new Paginacao(pagina, tamanho))
				.map(Response::from));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	Response registrar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long investimentoId,
			@Valid @RequestBody Request request) {
		return Response.from(useCase.registrar(usuarioId, investimentoId,
				new PosicaoInvestimentoUseCase.RegistrarCommand(request.valor(), request.dataReferencia())));
	}

	record Request(@NotNull @DecimalMin("0.00") BigDecimal valor, @NotNull LocalDate dataReferencia) { }
	record Response(Long id, Long investimentoId, BigDecimal valor, LocalDate dataReferencia) {
		static Response from(PosicaoInvestimento posicao) {
			return new Response(posicao.getId(), posicao.getInvestimentoId(), posicao.getValor().valor(),
					posicao.getDataReferencia());
		}
	}
}
