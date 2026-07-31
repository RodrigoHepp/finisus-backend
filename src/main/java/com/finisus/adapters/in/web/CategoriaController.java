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
import com.finisus.application.ports.in.CategoriaUseCase;
import com.finisus.domain.model.Categoria;

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
@RequestMapping("/api/v1/categorias")
@Validated
@Tag(name = "Categorias", description = "Categorias financeiras do usuário.")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {
	private final CategoriaUseCase useCase;

	public CategoriaController(CategoriaUseCase useCase) {
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
		return Response.from(
				useCase.criar(usuarioId, new CategoriaUseCase.CriarCommand(request.nome(), request.categoriaPaiId())));
	}

	@GetMapping("/{categoriaId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long categoriaId) {
		return Response.from(useCase.buscar(usuarioId, categoriaId));
	}

	@PatchMapping("/{categoriaId}")
	Response atualizar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long categoriaId,
			@Valid @RequestBody Request request) {
		return Response.from(useCase.atualizar(usuarioId, categoriaId,
				new CategoriaUseCase.CriarCommand(request.nome(), request.categoriaPaiId())));
	}

	@DeleteMapping("/{categoriaId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void inativar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long categoriaId) {
		useCase.inativar(usuarioId, categoriaId);
	}

	record Request(@NotBlank @Size(max = 100) String nome, @Positive Long categoriaPaiId) {
	}

	record Response(Long id, String nome, Long categoriaPaiId, boolean ativo) {
		static Response from(Categoria categoria) {
			return new Response(categoria.getId(), categoria.getNome(), categoria.getCategoriaPaiId(),
					categoria.isAtivo());
		}
	}
}
