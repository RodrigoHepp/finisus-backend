package com.financeiro.adapters.in.web;

import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.DespesaCompartilhadaUseCase;
import com.financeiro.domain.model.TipoRateio;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.financeiro.adapters.in.web.security.UsuarioAtual;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/compartilhamentos")
@Tag(name = "Compartilhamentos", description = "Despesas compartilhadas, rateios e opt-in.")
@SecurityRequirement(name = "bearerAuth")
public class DespesaCompartilhadaController {
	private final DespesaCompartilhadaUseCase useCase;

	public DespesaCompartilhadaController(DespesaCompartilhadaUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping
	PaginaResponse<DespesaResponse> listar(@UsuarioAtual Long usuarioId,
			@RequestParam(defaultValue = "0") @jakarta.validation.constraints.PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) int tamanho) {
		return PaginaResponse.from(useCase.listar(usuarioId, new Paginacao(pagina, tamanho))
				.map(despesa -> new DespesaResponse(despesa.getId(), despesa.getTransacaoId(),
						despesa.getTransacaoItemId(), despesa.getStatus(), 0)));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	DespesaResponse criar(@UsuarioAtual Long usuarioId, @Valid @RequestBody CriarRequest request) {
		var participantes = request.participantes().stream()
				.map(p -> new DespesaCompartilhadaUseCase.ParticipanteCommand(p.usuarioId(), p.nomeExterno(),
						p.emailExterno(), p.valorFixo(), p.percentual()))
				.toList();
		var resultado = useCase.criar(usuarioId, new DespesaCompartilhadaUseCase.CriarCommand(request.transacaoId(),
				request.transacaoItemId(), request.tipoRateio(), participantes));
		return new DespesaResponse(resultado.despesa().getId(), resultado.despesa().getTransacaoId(),
				resultado.despesa().getTransacaoItemId(), resultado.despesa().getStatus(), resultado.rateios().size());
	}

	@GetMapping("/{despesaId}")
	DespesaResponse buscar(@UsuarioAtual Long usuarioId,
			@PathVariable @jakarta.validation.constraints.Positive Long despesaId) {
		var despesa = useCase.buscar(usuarioId, despesaId);
		return new DespesaResponse(despesa.getId(), despesa.getTransacaoId(), despesa.getTransacaoItemId(),
				despesa.getStatus(), 0);
	}

	@PostMapping("/{despesaId}/cancelar")
	DespesaResponse cancelar(@UsuarioAtual Long usuarioId,
			@PathVariable @jakarta.validation.constraints.Positive Long despesaId) {
		var resultado = useCase.cancelar(usuarioId, despesaId);
		return new DespesaResponse(resultado.despesa().getId(), resultado.despesa().getTransacaoId(),
				resultado.despesa().getTransacaoItemId(), resultado.despesa().getStatus(), resultado.rateios().size());
	}

	record CriarRequest(@NotNull @jakarta.validation.constraints.Positive Long transacaoId,
			@jakarta.validation.constraints.Positive Long transacaoItemId, @NotNull TipoRateio tipoRateio,
			@NotEmpty List<@Valid ParticipanteRequest> participantes) {
	}

	record ParticipanteRequest(@jakarta.validation.constraints.Positive Long usuarioId, String nomeExterno,
			String emailExterno, @DecimalMin("0.01") BigDecimal valorFixo,
			@DecimalMin("0.01") @DecimalMax("100.00") BigDecimal percentual) {
	}

	record DespesaResponse(Long id, Long transacaoId, Long transacaoItemId,
			com.financeiro.domain.model.StatusDespesaCompartilhada status, int participantes) {
	}
}
