package com.financeiro.adapters.in.web;

import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.PrevisaoFluxoCaixaUseCase;
import com.financeiro.domain.model.PrevisaoMensal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/previsoes")
@Validated
public class PrevisaoFluxoCaixaController {
    private final PrevisaoFluxoCaixaUseCase useCase;
    public PrevisaoFluxoCaixaController(PrevisaoFluxoCaixaUseCase useCase) { this.useCase = useCase; }
    @PostMapping("/recalcular") List<Response> recalcular(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "3") @Min(1) @Max(12) int meses) { return useCase.recalcular(userId(jwt), meses).stream().map(Response::from).toList(); }
    @GetMapping("/{anoMes}") PaginaResponse<Response> consultar(@AuthenticationPrincipal Jwt jwt, @PathVariable @Pattern(regexp = "\\d{4}-\\d{2}") String anoMes, @RequestParam(defaultValue = "0") @PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) { return PaginaResponse.from(useCase.consultar(userId(jwt), anoMes, new Paginacao(pagina, tamanho)).map(Response::from)); }
    private Long userId(Jwt jwt) { return Long.valueOf(jwt.getSubject()); }
    record Response(String anoMes, Long categoriaId, BigDecimal entrada, BigDecimal saida) { static Response from(PrevisaoMensal previsao) { return new Response(previsao.getAnoMes().formatado(), previsao.getCategoriaId(), previsao.getValorProjetadoEntrada().valor(), previsao.getValorProjetadoSaida().valor()); } }
}
