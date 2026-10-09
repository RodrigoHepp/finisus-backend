package com.finisus.adapters.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finisus.adapters.in.web.security.UsuarioAtual;
import com.finisus.application.ports.in.TransferenciaContaUseCase;
import com.finisus.domain.model.StatusTransferencia;
import com.finisus.domain.model.TransferenciaConta;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/transferencias")
@Validated
@Tag(name = "Transferências", description = "Transferências atômicas entre contas do mesmo usuário.")
@SecurityRequirement(name = "bearerAuth")
public class TransferenciaContaController {
	private final TransferenciaContaUseCase useCase;

	public TransferenciaContaController(TransferenciaContaUseCase useCase) {
		this.useCase = useCase;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	Response transferir(@UsuarioAtual Long usuarioId,
			@RequestHeader("Idempotency-Key") @NotBlank @Size(max = 100) String chaveIdempotencia,
			@Valid @RequestBody Request request) {
		return Response.from(useCase.transferir(usuarioId, chaveIdempotencia,
				new TransferenciaContaUseCase.CriarCommand(request.contaOrigemId(), request.contaDestinoId(),
						request.valor(), request.data(), request.descricao())));
	}

	@GetMapping("/{transferenciaId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long transferenciaId) {
		return Response.from(useCase.buscar(usuarioId, transferenciaId));
	}

	@PostMapping("/{transferenciaId}/estornar")
	Response estornar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long transferenciaId) {
		return Response.from(useCase.estornar(usuarioId, transferenciaId));
	}

	record Request(@NotNull @Positive Long contaOrigemId, @NotNull @Positive Long contaDestinoId,
			@NotNull @DecimalMin("0.01") BigDecimal valor, @NotNull LocalDate data,
			@NotBlank @Size(max = 300) String descricao) { }

	record Response(Long id, Long contaOrigemId, Long contaDestinoId, BigDecimal valor, LocalDate data,
			String descricao, StatusTransferencia status, LocalDateTime estornadaEm) {
		static Response from(TransferenciaConta transferencia) {
			return new Response(transferencia.id(), transferencia.contaOrigemId(), transferencia.contaDestinoId(),
					transferencia.valor().valor(), transferencia.data(), transferencia.descricao(),
					transferencia.status(), transferencia.estornadaEm());
		}
	}
}
