package com.financeiro.adapters.in.web;

import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.FinanciamentoUseCase;
import com.financeiro.domain.model.Financiamento;
import com.financeiro.domain.model.ParcelaFinanciamento;
import com.financeiro.domain.model.StatusParcelaFinanciamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

@RestController
@RequestMapping("/api/v1/financiamentos")
@Validated
public class FinanciamentoController {
    private final FinanciamentoUseCase useCase;
    public FinanciamentoController(FinanciamentoUseCase useCase) { this.useCase = useCase; }

    @GetMapping
    PaginaResponse<Response> listar(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
        return PaginaResponse.from(useCase.listar(id(jwt), new Paginacao(pagina, tamanho)).map(Response::from));
    }
    @GetMapping("/{financiamentoId}") Response buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long financiamentoId) { return Response.from(useCase.buscar(id(jwt), financiamentoId)); }
    @GetMapping("/{financiamentoId}/parcelas")
    PaginaResponse<ParcelaResponse> parcelas(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long financiamentoId, @RequestParam(defaultValue = "0") @PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
        return PaginaResponse.from(useCase.listarParcelas(id(jwt), financiamentoId, new Paginacao(pagina, tamanho)).map(ParcelaResponse::from));
    }
    @PostMapping("/{financiamentoId}/parcelas/{parcelaId}/pagar") ParcelaResponse pagar(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long financiamentoId, @PathVariable @Positive Long parcelaId, @Valid @RequestBody PagamentoRequest request) { return ParcelaResponse.from(useCase.pagarParcela(id(jwt), financiamentoId, parcelaId, request.dataPagamento())); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) Response criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Request request) { return Response.from(useCase.criar(id(jwt), new FinanciamentoUseCase.CriarCommand(request.descricao(), request.principal(), request.taxaJurosMensal(), request.numeroParcelas(), request.dataInicio(), request.contaId()))); }
    private Long id(Jwt jwt) { return Long.valueOf(jwt.getSubject()); }

    record Request(@NotBlank @Size(max = 300) String descricao, @NotNull @DecimalMin("0.01") BigDecimal principal, @NotNull @DecimalMin("0.00") BigDecimal taxaJurosMensal, @Min(1) int numeroParcelas, @NotNull LocalDate dataInicio, @NotNull @Positive Long contaId) {}
    record PagamentoRequest(@NotNull LocalDate dataPagamento) {}
    record Response(Long id, String descricao, BigDecimal principal, BigDecimal taxaJurosMensal, int numeroParcelas, LocalDate dataInicio, Long contaId) { static Response from(Financiamento financiamento) { return new Response(financiamento.getId(), financiamento.getDescricao(), financiamento.getPrincipal().valor(), financiamento.getTaxaJurosMensal(), financiamento.getNumeroParcelas(), financiamento.getDataInicio(), financiamento.getContaId()); } }
    record ParcelaResponse(Long id, int numero, BigDecimal valor, LocalDate vencimento, StatusParcelaFinanciamento status) { static ParcelaResponse from(ParcelaFinanciamento parcela) { return new ParcelaResponse(parcela.getId(), parcela.getNumero(), parcela.getValor().valor(), parcela.getDataVencimento(), parcela.getStatus()); } }
}
