package com.finisus.adapters.in.web;

import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.FinanciamentoUseCase;
import com.finisus.domain.model.Financiamento;
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
import org.springframework.http.HttpStatus;
import com.finisus.adapters.in.web.security.UsuarioAtual;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/financiamentos")
@Validated
@Tag(name = "Financiamentos", description = "Cadastro e consulta de financiamentos.")
@SecurityRequirement(name = "bearerAuth")
public class FinanciamentoController {
	private final FinanciamentoUseCase useCase;

	public FinanciamentoController(FinanciamentoUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping
	PaginaResponse<Response> listar(@UsuarioAtual Long usuarioId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
		return PaginaResponse.from(useCase.listar(usuarioId, new Paginacao(pagina, tamanho)).map(Response::from));
	}

	@GetMapping("/{financiamentoId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long financiamentoId) {
		return Response.from(useCase.buscar(usuarioId, financiamentoId));
	}

	@GetMapping("/{financiamentoId}/cronogramas/{versao}")
	List<ParcelaHistoricaResponse> consultarCronogramaHistorico(@UsuarioAtual Long usuarioId,
			@PathVariable @Positive Long financiamentoId, @PathVariable @Positive int versao) {
		return useCase.consultarCronogramaHistorico(usuarioId, financiamentoId, versao).stream()
				.map(ParcelaHistoricaResponse::from).toList();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	Response criar(@UsuarioAtual Long usuarioId, @Valid @RequestBody Request request) {
		return Response.from(
				useCase.criar(usuarioId, new FinanciamentoUseCase.CriarCommand(request.descricao(), request.principal(),
						request.taxaJurosMensal(), request.numeroParcelas(), request.dataInicio(), request.contaId())));
	}

	@PostMapping("/{financiamentoId}/cancelar")
	Response cancelar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long financiamentoId) {
		return Response.from(useCase.cancelar(usuarioId, financiamentoId));
	}

	@PostMapping("/{financiamentoId}/refinanciar")
	@ResponseStatus(HttpStatus.CREATED)
	RefinanciamentoResponse refinanciar(@UsuarioAtual Long usuarioId,
			@PathVariable @Positive Long financiamentoId, @Valid @RequestBody RefinanciamentoRequest request) {
		var novo = request.novoFinanciamento();
		var resultado = useCase.refinanciar(usuarioId, financiamentoId,
				new FinanciamentoUseCase.RefinanciarCommand(request.parcelaId(),
						new FinanciamentoUseCase.CriarCommand(novo.descricao(), novo.principal(),
								novo.taxaJurosMensal(), novo.numeroParcelas(), novo.dataInicio(), novo.contaId())));
		return new RefinanciamentoResponse(Response.from(resultado.financiamentoOrigem()),
				Response.from(resultado.novoFinanciamento()), resultado.parcelasExcluidas());
	}

	@PostMapping("/{financiamentoId}/amortizar")
	AmortizacaoResponse amortizar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long financiamentoId,
			@Valid @RequestBody AmortizacaoRequest request) {
		var resultado = useCase.amortizar(usuarioId, financiamentoId,
				new FinanciamentoUseCase.AmortizarCommand(request.valor(), request.dataPagamento(),
						request.numeroParcelasRestantes(), request.modalidade()));
		return new AmortizacaoResponse(Response.from(resultado.financiamento()), resultado.transacaoId(),
				resultado.saldoDevedorAnterior(), resultado.saldoDevedorAtual(), resultado.parcelasRestantes());
	}

	record Request(@NotBlank @Size(max = 300) String descricao, @NotNull @DecimalMin("0.01") BigDecimal principal,
			@NotNull @DecimalMin("0.00") BigDecimal taxaJurosMensal, @Min(1) int numeroParcelas,
			@NotNull LocalDate dataInicio, @NotNull @Positive Long contaId) {
	}

	record RefinanciamentoRequest(@NotNull @Positive Long parcelaId, @NotNull @Valid Request novoFinanciamento) { }

	record RefinanciamentoResponse(Response financiamentoOrigem, Response novoFinanciamento,
			int parcelasExcluidas) { }

	record AmortizacaoRequest(@NotNull @DecimalMin("0.01") BigDecimal valor, @NotNull LocalDate dataPagamento,
			@PositiveOrZero Integer numeroParcelasRestantes,
			com.finisus.domain.model.ModalidadeAmortizacaoFinanciamento modalidade) { }

	record AmortizacaoResponse(Response financiamento, Long transacaoId, BigDecimal saldoDevedorAnterior,
			BigDecimal saldoDevedorAtual, int parcelasRestantes) { }

	record ParcelaHistoricaResponse(Long parcelaIdOrigem, int numero, BigDecimal valor, BigDecimal principal,
			BigDecimal juros, BigDecimal encargos, BigDecimal saldoDevedorInicial, BigDecimal saldoDevedorFinal,
			LocalDate vencimento, com.finisus.domain.model.StatusParcelaFinanciamento status, Long transacaoId) {
		static ParcelaHistoricaResponse from(
				com.finisus.application.ports.out.ParcelaFinanciamentoRepositoryPort.ParcelaHistorica parcela) {
			return new ParcelaHistoricaResponse(parcela.parcelaIdOrigem(), parcela.numero(), parcela.valor(),
					parcela.principal(), parcela.juros(), parcela.encargos(), parcela.saldoDevedorInicial(),
					parcela.saldoDevedorFinal(), parcela.vencimento(), parcela.status(), parcela.transacaoId());
		}
	}

	record Response(Long id, String descricao, BigDecimal principal, BigDecimal taxaJurosMensal, int numeroParcelas,
			LocalDate dataInicio, Long contaId, com.finisus.domain.model.StatusFinanciamento status,
			int cronogramaVersao, Long financiamentoOrigemId) {
		static Response from(Financiamento financiamento) {
			return new Response(financiamento.getId(), financiamento.getDescricao(),
					financiamento.getPrincipal().valor(), financiamento.getTaxaJurosMensal(),
					financiamento.getNumeroParcelas(), financiamento.getDataInicio(), financiamento.getContaId(),
					financiamento.getStatus(), financiamento.getCronogramaVersao(),
					financiamento.getFinanciamentoOrigemId());
		}
	}
}
