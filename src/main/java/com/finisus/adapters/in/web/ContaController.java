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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.ContaUseCase;
import com.finisus.application.ports.in.ReconciliarSaldoContaUseCase;
import com.finisus.application.ports.in.AjustarSaldoContaUseCase;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.TipoConta;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
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
	private final ReconciliarSaldoContaUseCase reconciliacao;
	private final AjustarSaldoContaUseCase ajusteSaldo;

	public ContaController(ContaUseCase useCase, ReconciliarSaldoContaUseCase reconciliacao,
			AjustarSaldoContaUseCase ajusteSaldo) {
		this.useCase = useCase;
		this.reconciliacao = reconciliacao;
		this.ajusteSaldo = ajusteSaldo;
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

	@GetMapping("/{contaId}/reconciliacao")
	ReconciliacaoResponse reconciliar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long contaId) {
		return ReconciliacaoResponse.from(reconciliacao.reconciliar(usuarioId, contaId));
	}

	@PostMapping("/{contaId}/ajustes-saldo")
	@ResponseStatus(HttpStatus.CREATED)
	AjusteSaldoResponse ajustarSaldo(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long contaId,
			@RequestHeader("Idempotency-Key") @NotBlank @Size(max = 100) String chaveIdempotencia,
			@Valid @RequestBody AjusteSaldoRequest request) {
		var ajuste = ajusteSaldo.ajustar(usuarioId, contaId, chaveIdempotencia,
				new AjustarSaldoContaUseCase.AjustarCommand(request.saldoInformado(), request.motivo()));
		return AjusteSaldoResponse.from(AjustarSaldoContaUseCase.Resultado.from(ajuste));
	}

	@GetMapping("/{contaId}/ajustes-saldo")
	PaginaResponse<AjusteSaldoResponse> listarAjustesSaldo(@UsuarioAtual Long usuarioId,
			@PathVariable @Positive Long contaId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
		return PaginaResponse.from(ajusteSaldo.listar(usuarioId, contaId, new Paginacao(pagina, tamanho))
				.map(ajuste -> AjusteSaldoResponse.from(AjustarSaldoContaUseCase.Resultado.from(ajuste))));
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

	record ReconciliacaoResponse(Long contaId, BigDecimal saldoMaterializado, BigDecimal saldoCalculado,
			BigDecimal divergencia, long quantidadeMovimentos, long quantidadeAjustes, boolean conciliado) {
		static ReconciliacaoResponse from(ReconciliarSaldoContaUseCase.ReconciliacaoSaldo resultado) {
			return new ReconciliacaoResponse(resultado.contaId(), resultado.saldoMaterializado(),
					resultado.saldoCalculado(), resultado.divergencia(), resultado.quantidadeMovimentos(),
					resultado.quantidadeAjustes(), resultado.conciliado());
		}
	}

	record AjusteSaldoRequest(@NotNull @DecimalMin("0.00") BigDecimal saldoInformado,
			@NotBlank @Size(max = 500) String motivo) { }

	record AjusteSaldoResponse(Long id, Long contaId, BigDecimal saldoAnterior, BigDecimal saldoCalculadoAnterior,
			BigDecimal saldoInformado, BigDecimal valorAjuste, String motivo, java.time.LocalDate dataAjuste) {
		static AjusteSaldoResponse from(AjustarSaldoContaUseCase.Resultado resultado) {
			return new AjusteSaldoResponse(resultado.id(), resultado.contaId(), resultado.saldoAnterior(),
					resultado.saldoCalculadoAnterior(), resultado.saldoInformado(), resultado.valorAjuste(),
					resultado.motivo(), resultado.dataAjuste());
		}
	}
}
