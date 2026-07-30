package com.financeiro.adapters.in.web;

import com.financeiro.application.ports.in.RecorrenciaUseCase;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Recorrencia;
import com.financeiro.domain.model.TipoTransacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/recorrencias")
@Validated
public class RecorrenciaController {
    private final RecorrenciaUseCase useCase;
    public RecorrenciaController(RecorrenciaUseCase useCase) { this.useCase = useCase; }
    @GetMapping PaginaResponse<Response> listar(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) { return PaginaResponse.from(useCase.listar(userId(jwt), new Paginacao(pagina, tamanho)).map(Response::from)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) Response criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Request r) { return Response.from(useCase.criar(userId(jwt), new RecorrenciaUseCase.CriarCommand(r.nome(), r.tipo(), r.valorEsperado(), r.diaDoMes(), r.categoriaId(), r.contaId(), r.meioPagamentoId()))); }
    @GetMapping("/{recorrenciaId}") Response buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long recorrenciaId) { return Response.from(useCase.buscar(userId(jwt), recorrenciaId)); }
    @PatchMapping("/{recorrenciaId}") Response atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long recorrenciaId, @Valid @RequestBody Request r) { return Response.from(useCase.atualizar(userId(jwt), recorrenciaId, new RecorrenciaUseCase.CriarCommand(r.nome(), r.tipo(), r.valorEsperado(), r.diaDoMes(), r.categoriaId(), r.contaId(), r.meioPagamentoId()))); }
    @DeleteMapping("/{recorrenciaId}") @ResponseStatus(HttpStatus.NO_CONTENT) void inativar(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long recorrenciaId) { useCase.inativar(userId(jwt), recorrenciaId); }
    @PostMapping("/geracoes/{anoMes}") List<FinanceiroCoreController.TransacaoResponse> gerar(@AuthenticationPrincipal Jwt jwt, @PathVariable @Pattern(regexp = "\\d{4}-\\d{2}") String anoMes) { return useCase.gerarMes(userId(jwt), anoMes).stream().map(FinanceiroCoreController.TransacaoResponse::from).toList(); }
    private Long userId(Jwt jwt) { return Long.valueOf(jwt.getSubject()); }
    record Request(@NotBlank @Size(max = 150) String nome, @NotNull TipoTransacao tipo, @NotNull @DecimalMin("0.01") BigDecimal valorEsperado, @Min(1) @Max(31) int diaDoMes, @Positive Long categoriaId, @NotNull @Positive Long contaId, @Positive Long meioPagamentoId) {}
    record Response(Long id, String nome, TipoTransacao tipo, BigDecimal valorEsperado, int diaDoMes, Long categoriaId, Long contaId, Long meioPagamentoId, boolean ativo) { static Response from(Recorrencia r) { return new Response(r.getId(), r.getNome(), r.getTipo(), r.getValorEsperado().valor(), r.getDiaDoMes(), r.getCategoriaId(), r.getContaId(), r.getMeioPagamentoId(), r.isAtivo()); } }
}
