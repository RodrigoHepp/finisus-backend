package com.finisus.adapters.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import com.finisus.adapters.in.web.security.UsuarioAtual;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.FaturaUseCase;
import com.finisus.application.ports.in.TransacaoUseCase;
import com.finisus.domain.model.Fatura;
import com.finisus.domain.model.StatusFatura;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Faturas", description = "Faturas de cartão, gastos e pagamentos.")
@SecurityRequirement(name = "bearerAuth")
public class FaturaController {
	private final FaturaUseCase useCase;

	public FaturaController(FaturaUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping("/cartoes/{cartaoId}/faturas")
	PaginaResponse<Response> listar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long cartaoId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
		return PaginaResponse
				.from(useCase.listar(usuarioId, cartaoId, new Paginacao(pagina, tamanho)).map(Response::from));
	}

	@PostMapping("/cartoes/faturas")
	@ResponseStatus(HttpStatus.CREATED)
	Response criar(@UsuarioAtual Long usuarioId, @Valid @RequestBody Request request) {
		return Response.from(useCase.criar(usuarioId, new FaturaUseCase.CriarCommand(request.cartaoId(),
				request.anoMes(), request.dataFechamento(), request.dataVencimento(), request.contaPagamentoId())));
	}

	@GetMapping("/cartoes/faturas/{faturaId}")
	DetalheResponse buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long faturaId) {
		return DetalheResponse.from(useCase.buscarDetalhe(usuarioId, faturaId));
	}

	@PostMapping("/cartoes/faturas/{faturaId}/gastos")
	@ResponseStatus(HttpStatus.CREATED)
	TransacaoResponse gasto(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long faturaId,
			@Valid @RequestBody GastoRequest request) {
		List<TransacaoUseCase.ItemCommand> itens = request.itens() == null ? List.of()
				: request.itens().stream().map(item -> new TransacaoUseCase.ItemCommand(item.itemId(), item.descricao(),
						item.quantidade(), item.valor(), item.categoriaId())).toList();
		return TransacaoResponse
				.from(useCase.lancarGasto(usuarioId, new FaturaUseCase.LancarGastoCommand(faturaId, request.valor(),
						request.data(), request.descricao(), request.contaId(), request.categoriaId(), itens)));
	}

	@PostMapping("/cartoes/faturas/{faturaId}/fechar")
	Response fechar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long faturaId) {
		return Response.from(useCase.fechar(usuarioId, faturaId));
	}

	@PostMapping("/cartoes/faturas/processar-ciclos")
	CicloResponse processarCiclos(@UsuarioAtual Long usuarioId, @Valid @RequestBody CicloRequest request) {
		return CicloResponse.from(useCase.processarCiclos(usuarioId, request.dataReferencia()));
	}

	@PostMapping("/cartoes/faturas/{faturaId}/pagar")
	Response pagar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long faturaId,
			@RequestHeader("Idempotency-Key") @NotBlank @Size(max = 100) String chaveIdempotencia,
			@Valid @RequestBody PagamentoRequest request) {
		return Response.from(useCase.pagar(usuarioId, faturaId, chaveIdempotencia,
				new FaturaUseCase.PagamentoCommand(request.valor(), request.dataPagamento(), request.contaId())));
	}

	@PostMapping("/cartoes/faturas/{faturaId}/estornar-pagamento")
	Response estornarPagamento(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long faturaId) {
		return Response.from(useCase.estornarPagamento(usuarioId, faturaId));
	}

	@PatchMapping("/cartoes/faturas/{faturaId}")
	Response atualizar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long faturaId,
			@Valid @RequestBody AtualizarRequest request) {
		return Response.from(useCase.atualizar(usuarioId, faturaId, new FaturaUseCase.AtualizarCommand(
				request.dataFechamento(), request.dataVencimento(), request.contaPagamentoId())));
	}

	@PostMapping("/cartoes/faturas/{faturaId}/cancelar")
	Response cancelar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long faturaId) {
		return Response.from(useCase.cancelar(usuarioId, faturaId));
	}

	record Request(@NotNull @Positive Long cartaoId, @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}") String anoMes,
			@NotNull LocalDate dataFechamento, @NotNull LocalDate dataVencimento,
			@NotNull @Positive Long contaPagamentoId) {
	}

	record GastoRequest(@NotNull @DecimalMin("0.01") BigDecimal valor, @NotNull LocalDate data,
			@NotBlank @Size(max = 500) String descricao, @NotNull @Positive Long contaId, @Positive Long categoriaId,
			List<@Valid ItemRequest> itens) {
	}

	record ItemRequest(@Positive Long itemId, @Size(max = 300) String descricao,
			@DecimalMin(value = "0.000001") BigDecimal quantidade,
			@NotNull @DecimalMin("0.01") BigDecimal valor, @Positive Long categoriaId) {
	}

	record PagamentoRequest(@DecimalMin("0.01") BigDecimal valor, @NotNull LocalDate dataPagamento,
			@Positive Long contaId) {
	}

	record CicloRequest(@NotNull LocalDate dataReferencia) {
	}

	record AtualizarRequest(@NotNull LocalDate dataFechamento, @NotNull LocalDate dataVencimento,
			@NotNull @Positive Long contaPagamentoId) {
	}

	record Response(Long id, Long cartaoId, String anoMes, LocalDate fechamento, LocalDate vencimento,
			StatusFatura status, Long contaPagamentoId) {
		static Response from(Fatura fatura) {
			return new Response(fatura.getId(), fatura.getCartaoId(), fatura.getMesReferencia().formatado(),
					fatura.getDataFechamento(), fatura.getDataVencimento(), fatura.getStatus(),
					fatura.getContaPagamentoId());
		}
	}

	record DetalheResponse(Long id, Long cartaoId, String anoMes, LocalDate fechamento, LocalDate vencimento,
			StatusFatura status, Long contaPagamentoId, BigDecimal valorTotal, BigDecimal valorPago,
			BigDecimal creditoAplicado, BigDecimal valorEmAberto, BigDecimal credito,
			List<TransacaoResponse> transacoes) {
		static DetalheResponse from(FaturaUseCase.Detalhe detalhe) {
			Fatura fatura = detalhe.fatura();
			return new DetalheResponse(fatura.getId(), fatura.getCartaoId(), fatura.getMesReferencia().formatado(),
					fatura.getDataFechamento(), fatura.getDataVencimento(), fatura.getStatus(),
					fatura.getContaPagamentoId(), detalhe.valorTotal(), detalhe.valorPago(), detalhe.creditoAplicado(),
					detalhe.valorEmAberto(), detalhe.credito(),
					detalhe.transacoes().stream().map(TransacaoResponse::from).toList());
		}
	}

	record CicloResponse(List<Response> fechadas, List<Response> criadas) {
		static CicloResponse from(FaturaUseCase.ResultadoProcessamentoCiclo resultado) {
			return new CicloResponse(resultado.fechadas().stream().map(Response::from).toList(),
					resultado.criadas().stream().map(Response::from).toList());
		}
	}
}
