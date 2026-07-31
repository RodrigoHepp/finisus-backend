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
import com.finisus.application.ports.in.BancoUseCase;
import com.finisus.domain.model.Banco;

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
@RequestMapping("/api/v1/bancos")
@Validated
@Tag(name = "Bancos", description = "Bancos do usuário e bancos do sistema.")
@SecurityRequirement(name = "bearerAuth")
public class BancoController {
	private final BancoUseCase useCase;

	public BancoController(BancoUseCase useCase) {
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
		return Response.from(useCase.criar(usuarioId, new BancoUseCase.CriarCommand(request.nome(), request.codigo())));
	}

	@GetMapping("/{bancoId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long bancoId) {
		return Response.from(useCase.buscar(usuarioId, bancoId));
	}

	@PatchMapping("/{bancoId}")
	Response atualizar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long bancoId,
			@Valid @RequestBody Request request) {
		return Response.from(
				useCase.atualizar(usuarioId, bancoId, new BancoUseCase.CriarCommand(request.nome(), request.codigo())));
	}

	@DeleteMapping("/{bancoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void inativar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long bancoId) {
		useCase.inativar(usuarioId, bancoId);
	}

	record Request(@NotBlank @Size(max = 150) String nome, @NotBlank @Size(max = 20) String codigo) {
	}

	record Response(Long id, String nome, String codigo, boolean sistema) {
		static Response from(Banco banco) {
			return new Response(banco.getId(), banco.getNome(), banco.getCodigo(), banco.isSistema());
		}
	}
}
