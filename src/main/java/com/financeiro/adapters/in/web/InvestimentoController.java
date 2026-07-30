package com.financeiro.adapters.in.web;

import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.InvestimentoUseCase;
import com.financeiro.domain.model.Investimento;
import com.financeiro.domain.model.MovimentoInvestimento;
import com.financeiro.domain.model.TipoInvestimento;
import com.financeiro.domain.model.TipoMovimentoInvestimento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/investimentos")
@Validated
public class InvestimentoController {
    private final InvestimentoUseCase useCase;

    public InvestimentoController(InvestimentoUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    PaginaResponse<Response> listar(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @jakarta.validation.constraints.PositiveOrZero int pagina,
                                    @RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) int tamanho) {
        return PaginaResponse.from(useCase.listar(id(jwt), new Paginacao(pagina, tamanho)).map(Response::from));
    }

    @GetMapping("/{investimentoId}")
    Response buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable @jakarta.validation.constraints.Positive Long investimentoId) {
        return Response.from(useCase.buscar(id(jwt), investimentoId));
    }

    @PatchMapping("/{investimentoId}")
    Response atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable @jakarta.validation.constraints.Positive Long investimentoId, @Valid @RequestBody CriarRequest request) {
        return Response.from(useCase.atualizar(id(jwt), investimentoId, new InvestimentoUseCase.CriarCommand(request.nome(), request.tipo(), request.contaOrigemId())));
    }

    @DeleteMapping("/{investimentoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void inativar(@AuthenticationPrincipal Jwt jwt, @PathVariable @jakarta.validation.constraints.Positive Long investimentoId) {
        useCase.inativar(id(jwt), investimentoId);
    }

    @GetMapping("/{investimentoId}/movimentos")
    PaginaResponse<MovimentoResponse> movimentos(@AuthenticationPrincipal Jwt jwt, @PathVariable @jakarta.validation.constraints.Positive Long investimentoId,
                                                 @RequestParam(defaultValue = "0") @jakarta.validation.constraints.PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) int tamanho) {
        return PaginaResponse.from(useCase.listarMovimentos(id(jwt), investimentoId, new Paginacao(pagina, tamanho)).map(MovimentoResponse::from));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Response criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CriarRequest request) {
        return Response.from(useCase.criar(id(jwt), new InvestimentoUseCase.CriarCommand(request.nome(), request.tipo(), request.contaOrigemId())));
    }

    @PostMapping("/movimentos")
    @ResponseStatus(HttpStatus.CREATED)
    MovimentoResponse movimentar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MovimentoRequest request) {
        return MovimentoResponse.from(useCase.movimentar(id(jwt), new InvestimentoUseCase.MovimentoCommand(request.investimentoId(), request.tipo(), request.valor(), request.data())));
    }

    private Long id(Jwt jwt) { return Long.valueOf(jwt.getSubject()); }

    record CriarRequest(@NotBlank String nome, @NotNull TipoInvestimento tipo, @NotNull @jakarta.validation.constraints.Positive Long contaOrigemId) {}
    record MovimentoRequest(@NotNull @jakarta.validation.constraints.Positive Long investimentoId, @NotNull TipoMovimentoInvestimento tipo, @NotNull @DecimalMin("0.01") BigDecimal valor, @NotNull LocalDate data) {}
    record Response(Long id, String nome, TipoInvestimento tipo, Long contaOrigemId, boolean ativo) { static Response from(Investimento investimento) { return new Response(investimento.getId(), investimento.getNome(), investimento.getTipo(), investimento.getContaOrigemId(), investimento.isAtivo()); } }
    record MovimentoResponse(Long id, Long investimentoId, TipoMovimentoInvestimento tipo, BigDecimal valor, LocalDate data, Long transacaoId) { static MovimentoResponse from(MovimentoInvestimento movimento) { return new MovimentoResponse(movimento.getId(), movimento.getInvestimentoId(), movimento.getTipo(), movimento.getValor().valor(), movimento.getData(), movimento.getTransacaoId()); } }
}
