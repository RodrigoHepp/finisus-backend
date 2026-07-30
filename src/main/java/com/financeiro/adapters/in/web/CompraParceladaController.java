package com.financeiro.adapters.in.web;

import com.financeiro.application.ports.in.CompraParceladaUseCase;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.CompraParcelada;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/compras-parceladas")
@Validated
public class CompraParceladaController {
    private final CompraParceladaUseCase useCase;
    public CompraParceladaController(CompraParceladaUseCase useCase) { this.useCase = useCase; }
    @GetMapping PaginaResponse<Response> listar(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) { return PaginaResponse.from(useCase.listar(userId(jwt), new Paginacao(pagina, tamanho)).map(Response::from)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) Response criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Request r) { return Response.from(useCase.criar(userId(jwt), new CompraParceladaUseCase.CriarCommand(r.descricao(), r.valorTotal(), r.numeroParcelas(), r.dataCompra(), r.categoriaId(), r.contaId()))); }
    @GetMapping("/{compraId}") Response buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long compraId) { return Response.from(useCase.buscar(userId(jwt), compraId)); }
    @PostMapping("/{compraId}/cancelar") Response cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long compraId) { return Response.from(useCase.cancelar(userId(jwt), compraId)); }
    private Long userId(Jwt jwt) { return Long.valueOf(jwt.getSubject()); }
    record Request(@NotBlank @Size(max = 300) String descricao, @NotNull @DecimalMin("0.01") BigDecimal valorTotal, @Min(1) int numeroParcelas, @NotNull LocalDate dataCompra, @Positive Long categoriaId, @NotNull @Positive Long contaId) {}
    record Response(Long id, String descricao, BigDecimal valorTotal, int numeroParcelas, LocalDate dataCompra, Long categoriaId, Long contaId, java.time.LocalDateTime canceladaEm) { static Response from(CompraParcelada c) { return new Response(c.getId(), c.getDescricao(), c.getValorTotal().valor(), c.getNumeroParcelas(), c.getDataCompra(), c.getCategoriaId(), c.getContaId(), c.getCanceladaEm()); } }
}
