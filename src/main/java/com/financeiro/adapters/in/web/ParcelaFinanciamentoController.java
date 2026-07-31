package com.financeiro.adapters.in.web;

import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.FinanciamentoUseCase;
import com.financeiro.application.ports.in.ParcelaFinanciamentoUseCase;
import com.financeiro.domain.model.ParcelaFinanciamento;
import com.financeiro.domain.model.StatusParcelaFinanciamento;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import com.financeiro.adapters.in.web.security.UsuarioAtual;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/financiamentos/{financiamentoId}/parcelas")
@Validated
@Tag(name = "Parcelas de financiamento", description = "Operações de parcela, refinanciamento e correção de lançamento.")
@SecurityRequirement(name = "bearerAuth")
public class ParcelaFinanciamentoController {
	private final ParcelaFinanciamentoUseCase parcelas;
	private final FinanciamentoUseCase financiamentos;

	public ParcelaFinanciamentoController(ParcelaFinanciamentoUseCase parcelas, FinanciamentoUseCase financiamentos) {
		this.parcelas = parcelas;
		this.financiamentos = financiamentos;
	}

	@GetMapping
	PaginaResponse<Response> listar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long financiamentoId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
		return PaginaResponse
				.from(parcelas.listar(usuarioId, financiamentoId, new Paginacao(pagina, tamanho)).map(Response::from));
	}

	@PostMapping("/{parcelaId}/pagar")
	Response pagar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long financiamentoId,
			@PathVariable @Positive Long parcelaId, @Valid @RequestBody PagamentoRequest request) {
		return Response.from(parcelas.pagarParcela(usuarioId, financiamentoId, parcelaId, request.dataPagamento()));
	}

	@PostMapping("/{parcelaId}/refinanciamento")
	RefinanciamentoResponse refinanciar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long financiamentoId,
			@PathVariable @Positive Long parcelaId) {
		return RefinanciamentoResponse
				.from(financiamentos.excluirPorRefinanciamento(usuarioId, financiamentoId, parcelaId));
	}

	@DeleteMapping("/{parcelaId}/erro-de-lancamento")
	List<Response> excluirPorErroDeLancamento(@UsuarioAtual Long usuarioId,
			@PathVariable @Positive Long financiamentoId, @PathVariable @Positive Long parcelaId) {
		financiamentos.excluirPorErroDeLancamento(usuarioId, financiamentoId, parcelaId);
		return parcelas.listar(usuarioId, financiamentoId).stream().map(Response::from).toList();
	}

	record RefinanciamentoResponse(Long financiamentoId, LocalDateTime finalizadoEm, int parcelasExcluidas,
			String mensagem) {
		static RefinanciamentoResponse from(FinanciamentoUseCase.RefinanciamentoResult result) {
			return new RefinanciamentoResponse(result.financiamento().getId(), result.financiamento().getFinalizadoEm(),
					result.parcelasExcluidas(), "Financiamento finalizado com sucesso.");
		}
	}

	record PagamentoRequest(@NotNull LocalDate dataPagamento) {
	}

	record Response(Long id, int numero, BigDecimal valor, LocalDate vencimento, StatusParcelaFinanciamento status) {
		static Response from(ParcelaFinanciamento parcela) {
			return new Response(parcela.getId(), parcela.getNumero(), parcela.getValor().valor(),
					parcela.getDataVencimento(), parcela.getStatus());
		}
	}
}
