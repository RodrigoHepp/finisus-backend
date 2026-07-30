package com.financeiro.adapters.in.web;

import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.CompartilhamentoUseCase;
import com.financeiro.domain.model.RateioDespesa;
import com.financeiro.domain.model.StatusRateio;
import com.financeiro.domain.model.TipoParticipante;
import com.financeiro.domain.model.TipoRateio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/compartilhamentos")
@Validated
public class CompartilhamentoController {
    private final CompartilhamentoUseCase useCase;
    public CompartilhamentoController(CompartilhamentoUseCase useCase) { this.useCase = useCase; }
    @GetMapping("/opt-in") ConfigResponse consultarOptIn(@AuthenticationPrincipal Jwt jwt) { var configuracao = useCase.consultarOptIn(id(jwt)); return new ConfigResponse(configuracao.isAceitaCompartilhamento()); }
    @PutMapping("/opt-in") ConfigResponse optIn(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OptInRequest request) { var configuracao = useCase.atualizarOptIn(id(jwt), request.aceita()); return new ConfigResponse(configuracao.isAceitaCompartilhamento()); }
    @GetMapping PaginaResponse<DespesaResponse> listar(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @jakarta.validation.constraints.PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) int tamanho) { return PaginaResponse.from(useCase.listarDespesas(id(jwt), new Paginacao(pagina, tamanho)).map(despesa -> new DespesaResponse(despesa.getId(), despesa.getTransacaoId(), 0))); }
    @GetMapping("/{despesaId}/rateios") PaginaResponse<RateioResponse> rateios(@AuthenticationPrincipal Jwt jwt, @PathVariable @jakarta.validation.constraints.Positive Long despesaId, @RequestParam(defaultValue = "0") @jakarta.validation.constraints.PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) int tamanho) { return PaginaResponse.from(useCase.listarRateios(id(jwt), despesaId, new Paginacao(pagina, tamanho)).map(RateioResponse::from)); }
    @PostMapping("/rateios/{rateioId}/resposta") RateioResponse responder(@AuthenticationPrincipal Jwt jwt, @PathVariable @jakarta.validation.constraints.Positive Long rateioId, @Valid @RequestBody RespostaRequest request) { return RateioResponse.from(useCase.responderRateio(id(jwt), rateioId, request.aceita())); }
    @PostMapping("/rateios/{rateioId}/pagar") RateioResponse pagar(@AuthenticationPrincipal Jwt jwt, @PathVariable @jakarta.validation.constraints.Positive Long rateioId) { return RateioResponse.from(useCase.marcarRateioPago(id(jwt), rateioId)); }
    @PostMapping DespesaResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CriarRequest request) { var participantes = request.participantes().stream().map(p -> new CompartilhamentoUseCase.ParticipanteCommand(p.usuarioId(), p.nomeExterno(), p.emailExterno(), p.valorFixo(), p.percentual())).toList(); var resultado = useCase.criarDespesa(id(jwt), new CompartilhamentoUseCase.CriarCommand(request.transacaoId(), request.tipoRateio(), participantes)); return new DespesaResponse(resultado.despesa().getId(), resultado.despesa().getTransacaoId(), resultado.rateios().size()); }
    private Long id(Jwt jwt) { return Long.valueOf(jwt.getSubject()); }
    record OptInRequest(boolean aceita) {} record RespostaRequest(boolean aceita) {} record CriarRequest(@NotNull @jakarta.validation.constraints.Positive Long transacaoId, @NotNull TipoRateio tipoRateio, @NotEmpty List<@Valid ParticipanteRequest> participantes) {} record ParticipanteRequest(@jakarta.validation.constraints.Positive Long usuarioId, String nomeExterno, String emailExterno, @DecimalMin("0.01") BigDecimal valorFixo, @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal percentual) {} record ConfigResponse(boolean aceita) {} record DespesaResponse(Long id, Long transacaoId, int participantes) {} record RateioResponse(Long id, TipoParticipante participante, Long usuarioId, String nomeExterno, BigDecimal valorFixo, BigDecimal percentual, StatusRateio status) { static RateioResponse from(RateioDespesa rateio) { return new RateioResponse(rateio.getId(), rateio.getTipoParticipante(), rateio.getUsuarioId(), rateio.getNomeExterno(), rateio.getValorFixo() == null ? null : rateio.getValorFixo().valor(), rateio.getPercentual(), rateio.getStatus()); } }
}
