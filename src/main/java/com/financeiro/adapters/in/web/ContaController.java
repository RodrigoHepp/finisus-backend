package com.financeiro.adapters.in.web;

import java.math.BigDecimal;

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
import com.financeiro.application.ports.in.ContaUseCase;
import com.financeiro.domain.model.Conta;
import com.financeiro.domain.model.TipoConta;

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
@RequestMapping("/api/v1/contas")
@Validated
@Tag(name = "Contas", description = "Contas financeiras do usuário.")
@SecurityRequirement(name = "bearerAuth")
public class ContaController {
	private final ContaUseCase useCase;

	public ContaController(ContaUseCase useCase) {
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
				new ContaUseCase.CriarCommand(request.nome(), request.tipo(), request.bancoId())));
	}

	@GetMapping("/{contaId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long contaId) {
		return Response.from(useCase.buscar(usuarioId, contaId));
	}

	@PatchMapping("/{contaId}")
	Response atualizar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long contaId,
			@Valid @RequestBody Request request) {
		return Response.from(useCase.atualizar(usuarioId, contaId,
				new ContaUseCase.CriarCommand(request.nome(), request.tipo(), request.bancoId())));
	}

	@DeleteMapping("/{contaId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void inativar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long contaId) {
		useCase.inativar(usuarioId, contaId);
	}

	record Request(@NotBlank @Size(max = 150) String nome, @NotNull TipoConta tipo, @Positive Long bancoId) {
	}

	record Response(Long id, String nome, TipoConta tipo, Long bancoId, BigDecimal saldo, boolean ativo) {
		static Response from(Conta conta) {
			return new Response(conta.getId(), conta.getNome(), conta.getTipo(), conta.getBancoId(),
					conta.getSaldo().valor(), conta.isAtivo());
		}
	}
}
