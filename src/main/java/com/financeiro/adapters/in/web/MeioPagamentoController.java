package com.financeiro.adapters.in.web;

import org.springframework.http.HttpStatus;
import com.financeiro.adapters.in.web.security.UsuarioAtual;
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

import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.MeioPagamentoUseCase;
import com.financeiro.domain.model.MeioPagamento;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/meios-pagamento")
@Validated
@Tag(name = "Meios de pagamento", description = "Meios de pagamento do usuário.")
@SecurityRequirement(name = "bearerAuth")
public class MeioPagamentoController {
	private final MeioPagamentoUseCase useCase;

	public MeioPagamentoController(MeioPagamentoUseCase useCase) {
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
		return Response.from(useCase.criar(usuarioId, new MeioPagamentoUseCase.CriarCommand(request.nome())));
	}

	@GetMapping("/{meioPagamentoId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long meioPagamentoId) {
		return Response.from(useCase.buscar(usuarioId, meioPagamentoId));
	}

	@PatchMapping("/{meioPagamentoId}")
	Response atualizar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long meioPagamentoId,
			@Valid @RequestBody Request request) {
		return Response.from(
				useCase.atualizar(usuarioId, meioPagamentoId, new MeioPagamentoUseCase.CriarCommand(request.nome())));
	}

	@DeleteMapping("/{meioPagamentoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void inativar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long meioPagamentoId) {
		useCase.inativar(usuarioId, meioPagamentoId);
	}

	record Request(@NotBlank @Size(max = 100) String nome) {
	}

	record Response(Long id, String nome, boolean ativo) {
		static Response from(MeioPagamento meio) {
			return new Response(meio.getId(), meio.getNome(), meio.isAtivo());
		}
	}
}
