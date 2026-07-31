package com.finisus.adapters.in.web;

import java.math.BigDecimal;

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
import com.finisus.application.ports.in.CartaoCreditoUseCase;
import com.finisus.domain.model.CartaoCredito;

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

@RestController
@RequestMapping("/api/v1/cartoes")
@Validated
@Tag(name = "Cartões", description = "Cadastro e manutenção de cartões de crédito.")
@SecurityRequirement(name = "bearerAuth")
public class CartaoCreditoController {
	private final CartaoCreditoUseCase useCase;

	public CartaoCreditoController(CartaoCreditoUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping
	PaginaResponse<Response> listar(@UsuarioAtual Long usuarioId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
		return PaginaResponse
				.from(useCase.listarCartoes(usuarioId, new Paginacao(pagina, tamanho)).map(Response::from));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	Response criar(@UsuarioAtual Long usuarioId, @Valid @RequestBody Request request) {
		return Response.from(useCase.criarCartao(usuarioId, new CartaoCreditoUseCase.CriarCartaoCommand(request.nome(),
				request.limite(), request.diaFechamento(), request.diaVencimento())));
	}

	@GetMapping("/{cartaoId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long cartaoId) {
		return Response.from(useCase.buscarCartao(usuarioId, cartaoId));
	}

	@PatchMapping("/{cartaoId}")
	Response atualizar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long cartaoId,
			@Valid @RequestBody Request request) {
		return Response.from(useCase.atualizarCartao(usuarioId, cartaoId, new CartaoCreditoUseCase.CriarCartaoCommand(
				request.nome(), request.limite(), request.diaFechamento(), request.diaVencimento())));
	}

	@DeleteMapping("/{cartaoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void inativar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long cartaoId) {
		useCase.inativarCartao(usuarioId, cartaoId);
	}

	record Request(@NotBlank @Size(max = 100) String nome, @NotNull @DecimalMin("0.01") BigDecimal limite,
			@Min(1) @Max(31) int diaFechamento, @Min(1) @Max(31) int diaVencimento) {
	}

	record Response(Long id, String nome, BigDecimal limite, int diaFechamento, int diaVencimento, boolean ativo) {
		static Response from(CartaoCredito cartao) {
			return new Response(cartao.getId(), cartao.getNome(), cartao.getLimite().valor(), cartao.getDiaFechamento(),
					cartao.getDiaVencimento(), cartao.isAtivo());
		}
	}
}
