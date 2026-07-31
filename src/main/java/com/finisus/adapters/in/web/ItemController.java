package com.finisus.adapters.in.web;

import com.finisus.adapters.in.web.security.UsuarioAtual;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.ItemUseCase;
import com.finisus.domain.model.Item;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/v1/itens")
@Validated
@Tag(name = "Itens", description = "Catálogo pessoal de itens reutilizáveis em lançamentos.")
@SecurityRequirement(name = "bearerAuth")
public class ItemController {
	private final ItemUseCase useCase;

	public ItemController(ItemUseCase useCase) {
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
		return Response.from(useCase.criar(usuarioId, command(request)));
	}

	@GetMapping("/{itemId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long itemId) {
		return Response.from(useCase.buscar(usuarioId, itemId));
	}

	@PatchMapping("/{itemId}")
	Response atualizar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long itemId,
			@Valid @RequestBody Request request) {
		return Response.from(useCase.atualizar(usuarioId, itemId, command(request)));
	}

	@DeleteMapping("/{itemId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void inativar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long itemId) {
		useCase.inativar(usuarioId, itemId);
	}

	private ItemUseCase.CriarCommand command(Request request) {
		return new ItemUseCase.CriarCommand(request.nome(), request.categoriaPadraoId());
	}

	record Request(@NotBlank @Size(max = 300) String nome, @Positive Long categoriaPadraoId) {
	}

	record Response(Long id, String nome, Long categoriaPadraoId, boolean ativo) {
		static Response from(Item item) {
			return new Response(item.getId(), item.getNome(), item.getCategoriaPadraoId(), item.isAtivo());
		}
	}
}
