package com.finisus.adapters.in.web;

import com.finisus.adapters.in.web.security.UsuarioAtual;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.ObrigacaoFinanceiraUseCase;
import com.finisus.domain.model.ObrigacaoFinanceira;
import com.finisus.domain.model.StatusObrigacaoFinanceira;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/obrigacoes-financeiras")
@Validated
@Tag(name = "Obrigações financeiras", description = "Contas a pagar e suas liquidações.")
@SecurityRequirement(name = "bearerAuth")
public class ObrigacaoFinanceiraController {
	private final ObrigacaoFinanceiraUseCase useCase;

	public ObrigacaoFinanceiraController(ObrigacaoFinanceiraUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping
	PaginaResponse<Response> listar(@UsuarioAtual Long usuarioId, @RequestParam(required = false) StatusObrigacaoFinanceira status,
			@RequestParam(required = false) LocalDate inicio, @RequestParam(required = false) LocalDate fim,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Positive @Max(100) int tamanho) {
		return PaginaResponse.from(useCase.listar(usuarioId,
				new ObrigacaoFinanceiraUseCase.FiltroListagem(status, inicio, fim), new Paginacao(pagina, tamanho))
				.map(Response::from));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	Response criar(@UsuarioAtual Long usuarioId, @Valid @RequestBody Request request) {
		return Response.from(useCase.criar(usuarioId, new ObrigacaoFinanceiraUseCase.CriarCommand(request.descricao(),
				request.credor(), request.valor(), request.dataVencimento(), request.contaPagamentoId(),
				request.categoriaId())));
	}

	@GetMapping("/{obrigacaoId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long obrigacaoId) {
		return Response.from(useCase.buscar(usuarioId, obrigacaoId));
	}

	@PostMapping("/{obrigacaoId}/pagar")
	Response pagar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long obrigacaoId,
			@Valid @RequestBody PagamentoRequest request) {
		return Response.from(useCase.pagar(usuarioId, obrigacaoId, new ObrigacaoFinanceiraUseCase.PagamentoCommand(
				request.dataPagamento(), request.valor(), request.juros(), request.encargos(), request.desconto())));
	}

	@GetMapping("/{obrigacaoId}/pagamentos")
	java.util.List<PagamentoResponse> listarPagamentos(@UsuarioAtual Long usuarioId,
			@PathVariable @Positive Long obrigacaoId) {
		return useCase.listarPagamentos(usuarioId, obrigacaoId).stream().map(PagamentoResponse::from).toList();
	}

	@PostMapping("/{obrigacaoId}/pagamentos/{pagamentoId}/estornar")
	Response estornarPagamento(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long obrigacaoId,
			@PathVariable @Positive Long pagamentoId) {
		return Response.from(useCase.estornarPagamento(usuarioId, obrigacaoId, pagamentoId));
	}

	@PostMapping("/{obrigacaoId}/estornar-pagamento")
	Response estornarPagamento(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long obrigacaoId) {
		return Response.from(useCase.estornarPagamento(usuarioId, obrigacaoId));
	}

	@PostMapping("/{obrigacaoId}/cancelar")
	Response cancelar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long obrigacaoId) {
		return Response.from(useCase.cancelar(usuarioId, obrigacaoId));
	}

	record Request(@NotBlank @Size(max = 300) String descricao, @NotBlank @Size(max = 150) String credor,
			@NotNull @DecimalMin("0.01") BigDecimal valor, @NotNull LocalDate dataVencimento,
			@NotNull @Positive Long contaPagamentoId, @Positive Long categoriaId) { }
	record PagamentoRequest(@NotNull LocalDate dataPagamento, @DecimalMin("0.01") BigDecimal valor,
			@PositiveOrZero BigDecimal juros, @PositiveOrZero BigDecimal encargos,
			@PositiveOrZero BigDecimal desconto) { }
	record Response(Long id, String descricao, String credor, BigDecimal valor, LocalDate dataVencimento,
			Long contaPagamentoId, Long categoriaId, StatusObrigacaoFinanceira status, LocalDate dataLiquidacao,
			Long transacaoId, BigDecimal valorPago, BigDecimal saldoPendente) {
		static Response from(ObrigacaoFinanceira obrigacao) {
			return new Response(obrigacao.getId(), obrigacao.getDescricao(), obrigacao.getCredor(),
					obrigacao.getValor().valor(), obrigacao.getDataVencimento(), obrigacao.getContaPagamentoId(),
					obrigacao.getCategoriaId(), obrigacao.getStatus(), obrigacao.getDataLiquidacao(),
					obrigacao.getTransacaoId(), obrigacao.getValorPago().valor(), obrigacao.getSaldoPendente().valor());
		}
	}
	record PagamentoResponse(Long id, Long transacaoId, BigDecimal valor, BigDecimal juros, BigDecimal encargos,
			BigDecimal desconto, BigDecimal valorAbatido, BigDecimal valorCaixa, LocalDate dataPagamento,
			java.time.LocalDateTime estornadoEm) {
		static PagamentoResponse from(com.finisus.domain.model.PagamentoObrigacao p) {
			return new PagamentoResponse(p.id(), p.transacaoId(), p.valor().valor(), p.juros().valor(),
					p.encargos().valor(), p.desconto().valor(), p.valorAbatido().valor(), p.valorCaixa().valor(),
					p.dataPagamento(), p.estornadoEm());
		}
	}
}
