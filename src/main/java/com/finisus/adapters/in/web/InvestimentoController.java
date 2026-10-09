package com.finisus.adapters.in.web;

import org.springframework.http.HttpStatus;
import com.finisus.adapters.in.web.security.UsuarioAtual;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.InvestimentoUseCase;
import com.finisus.domain.model.Investimento;
import com.finisus.domain.model.TipoInvestimento;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/investimentos")
@Validated
@Tag(name = "Investimentos", description = "Cadastro e manutenção de investimentos.")
@SecurityRequirement(name = "bearerAuth")
public class InvestimentoController {
	private final InvestimentoUseCase useCase;

	public InvestimentoController(InvestimentoUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping
	PaginaResponse<Response> listar(@UsuarioAtual Long usuarioId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
		return PaginaResponse.from(useCase.listar(usuarioId, new Paginacao(pagina, tamanho)).map(Response::from));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	Response criar(@UsuarioAtual Long usuarioId, @Valid @RequestBody Request request) {
		return Response.from(useCase.criar(usuarioId,
				new InvestimentoUseCase.CriarCommand(request.nome(), request.tipo(), request.contaOrigemId(),
						request.contaCustodiaId())));
	}

	@GetMapping("/{investimentoId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long investimentoId) {
		return Response.from(useCase.buscar(usuarioId, investimentoId));
	}

	@PatchMapping("/{investimentoId}")
	Response atualizar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long investimentoId,
			@Valid @RequestBody Request request) {
		return Response.from(useCase.atualizar(usuarioId, investimentoId,
				new InvestimentoUseCase.CriarCommand(request.nome(), request.tipo(), request.contaOrigemId(),
						request.contaCustodiaId())));
	}

	@DeleteMapping("/{investimentoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void inativar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long investimentoId) {
		useCase.inativar(usuarioId, investimentoId);
	}

	record Request(@NotBlank @Size(max = 150) String nome, @NotNull TipoInvestimento tipo,
			@NotNull @Positive Long contaOrigemId, @Positive Long contaCustodiaId) {
	}

	record Response(Long id, String nome, TipoInvestimento tipo, Long contaOrigemId, Long contaCustodiaId,
			boolean ativo) {
		static Response from(Investimento investimento) {
			return new Response(investimento.getId(), investimento.getNome(), investimento.getTipo(),
					investimento.getContaOrigemId(), investimento.getContaCustodiaId(), investimento.isAtivo());
		}
	}
}
