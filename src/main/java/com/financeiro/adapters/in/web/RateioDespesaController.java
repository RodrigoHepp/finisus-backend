package com.financeiro.adapters.in.web;

import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.RateioDespesaUseCase;
import com.financeiro.domain.model.RateioDespesa;
import com.financeiro.domain.model.StatusRateio;
import com.financeiro.domain.model.TipoParticipante;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.financeiro.adapters.in.web.security.UsuarioAtual;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/compartilhamentos")
@Tag(name = "Compartilhamentos", description = "Despesas compartilhadas, rateios e opt-in.")
@SecurityRequirement(name = "bearerAuth")
public class RateioDespesaController {
	private final RateioDespesaUseCase useCase;

	public RateioDespesaController(RateioDespesaUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping("/{despesaId}/rateios")
	PaginaResponse<RateioResponse> listar(@UsuarioAtual Long usuarioId,
			@PathVariable @jakarta.validation.constraints.Positive Long despesaId,
			@RequestParam(defaultValue = "0") @jakarta.validation.constraints.PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) int tamanho) {
		return PaginaResponse
				.from(useCase.listar(usuarioId, despesaId, new Paginacao(pagina, tamanho)).map(RateioResponse::from));
	}

	@PostMapping("/rateios/{rateioId}/resposta")
	RateioResponse responder(@UsuarioAtual Long usuarioId,
			@PathVariable @jakarta.validation.constraints.Positive Long rateioId,
			@Valid @RequestBody RespostaRequest request) {
		return RateioResponse.from(useCase.responder(usuarioId, rateioId, request.aceita()));
	}

	@PostMapping("/rateios/{rateioId}/pagar")
	RateioResponse pagar(@UsuarioAtual Long usuarioId,
			@PathVariable @jakarta.validation.constraints.Positive Long rateioId) {
		return RateioResponse.from(useCase.marcarPago(usuarioId, rateioId));
	}

	@GetMapping("/rateios/recebidos")
	PaginaResponse<RateioRecebidoResponse> listarRecebidos(@UsuarioAtual Long usuarioId,
			@RequestParam(defaultValue = "0") @jakarta.validation.constraints.PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) int tamanho) {
		return PaginaResponse.from(
				useCase.listarRecebidos(usuarioId, new Paginacao(pagina, tamanho)).map(RateioRecebidoResponse::from));
	}

	record RespostaRequest(boolean aceita) {
	}

	record RateioResponse(Long id, TipoParticipante participante, Long usuarioId, String nomeExterno,
			BigDecimal valorFixo, BigDecimal percentual, StatusRateio status) {
		static RateioResponse from(RateioDespesa rateio) {
			return new RateioResponse(rateio.getId(), rateio.getTipoParticipante(), rateio.getUsuarioId(),
					rateio.getNomeExterno(), rateio.getValorFixo() == null ? null : rateio.getValorFixo().valor(),
					rateio.getPercentual(), rateio.getStatus());
		}
	}

	record RateioRecebidoResponse(Long id, StatusRateio status, Long despesaId, Long transacaoId, Long transacaoItemId,
			BigDecimal valorFixo, BigDecimal percentual) {
		static RateioRecebidoResponse from(RateioDespesaUseCase.RateioRecebido recebido) {
			RateioDespesa rateio = recebido.rateio();
			var despesa = recebido.despesa();
			return new RateioRecebidoResponse(rateio.getId(), rateio.getStatus(), despesa.getId(),
					despesa.getTransacaoId(), despesa.getTransacaoItemId(),
					rateio.getValorFixo() == null ? null : rateio.getValorFixo().valor(), rateio.getPercentual());
		}
	}
}
