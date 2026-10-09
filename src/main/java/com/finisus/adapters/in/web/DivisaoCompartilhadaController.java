package com.finisus.adapters.in.web;

import com.finisus.adapters.in.web.security.UsuarioAtual;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.AssociarTransacaoDivisaoUseCase;
import com.finisus.application.ports.in.ConsultarResumoDivisaoUseCase;
import com.finisus.application.ports.in.DivisaoCompartilhadaUseCase;
import com.finisus.domain.model.DivisaoCompartilhada;
import com.finisus.domain.model.ResumoDivisao;
import com.finisus.domain.model.ResumoParticipanteDivisao;
import com.finisus.domain.model.StatusDivisaoCompartilhada;
import com.finisus.domain.model.StatusPagamentoDivisao;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.PutMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/divisoes-compartilhadas")
@Validated
@Tag(name = "Divisões compartilhadas", description = "Agrupa contas compartilhadas e calcula o saldo por transações reais.")
@SecurityRequirement(name = "bearerAuth")
public class DivisaoCompartilhadaController {
    private final DivisaoCompartilhadaUseCase divisoes;
    private final AssociarTransacaoDivisaoUseCase vinculos;
    private final ConsultarResumoDivisaoUseCase resumos;

    public DivisaoCompartilhadaController(DivisaoCompartilhadaUseCase divisoes,
            AssociarTransacaoDivisaoUseCase vinculos, ConsultarResumoDivisaoUseCase resumos) {
        this.divisoes = divisoes;
        this.vinculos = vinculos;
        this.resumos = resumos;
    }

    @GetMapping
    PaginaResponse<DivisaoResponse> listar(@UsuarioAtual Long usuarioId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
        return PaginaResponse.from(divisoes.listar(usuarioId, new Paginacao(pagina, tamanho)).map(DivisaoResponse::from));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DivisaoResponse criar(@UsuarioAtual Long usuarioId, @Valid @RequestBody CriarRequest request) {
        return DivisaoResponse.from(divisoes.criar(usuarioId, new DivisaoCompartilhadaUseCase.CriarCommand(request.nome(),
                participantes(request.participantes()))));
    }

    @GetMapping("/{divisaoId}")
    DivisaoResponse buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long divisaoId) {
        return DivisaoResponse.from(divisoes.buscar(usuarioId, divisaoId));
    }

    @GetMapping("/{divisaoId}/participantes/historico")
    List<HistoricoParticipanteResponse> listarHistoricoParticipantes(@UsuarioAtual Long usuarioId,
            @PathVariable @Positive Long divisaoId) {
        return divisoes.listarHistoricoParticipantes(usuarioId, divisaoId).stream()
                .map(h -> new HistoricoParticipanteResponse(h.usuarioId(), h.percentual(), h.vigenteDesde(),
                        h.vigenteAte()))
                .toList();
    }

    @PatchMapping("/{divisaoId}/participantes")
    DivisaoResponse atualizarParticipantes(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long divisaoId,
            @Valid @RequestBody ParticipantesRequest request) {
        return DivisaoResponse.from(divisoes.atualizarParticipantes(usuarioId, divisaoId,
                participantes(request.participantes())));
    }

    @DeleteMapping("/{divisaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void inativar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long divisaoId) {
        divisoes.inativar(usuarioId, divisaoId);
    }

    @PostMapping("/{divisaoId}/transacoes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void associar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long divisaoId,
            @Valid @RequestBody AssociarTransacaoRequest request) {
        vinculos.associar(usuarioId, divisaoId, request.transacaoId(), request.baseCompartilhada(),
                request.responsabilidades() == null ? null : request.responsabilidades().stream()
                        .map(r -> new AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand(r.usuarioId(),
                                r.percentual(), r.valorDevido()))
                        .toList());
    }

    @DeleteMapping("/{divisaoId}/transacoes/{transacaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void desassociar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long divisaoId,
            @PathVariable @Positive Long transacaoId) {
        vinculos.desassociar(usuarioId, divisaoId, transacaoId);
    }

    @GetMapping("/{divisaoId}/transacoes/{transacaoId}/alocacoes")
    List<AlocacaoPagamentoResponse> listarAlocacoes(@UsuarioAtual Long usuarioId,
            @PathVariable @Positive Long divisaoId, @PathVariable @Positive Long transacaoId) {
        return vinculos.listarAlocacoes(usuarioId, divisaoId, transacaoId).stream()
                .map(a -> new AlocacaoPagamentoResponse(a.id(), a.transacaoId(), a.pagadorId(), a.valor().valor(),
                        a.criadaEm(), a.canceladaEm(), a.canceladaPor()))
                .toList();
    }

    @PutMapping("/{divisaoId}/transacoes/{transacaoId}/alocacoes")
    List<AlocacaoPagamentoResponse> substituirAlocacoes(@UsuarioAtual Long usuarioId,
            @PathVariable @Positive Long divisaoId, @PathVariable @Positive Long transacaoId,
            @Valid @RequestBody AlocacoesPagamentoRequest request) {
        return vinculos.substituirAlocacoes(usuarioId, divisaoId, transacaoId, request.alocacoes().stream()
                .map(a -> new AssociarTransacaoDivisaoUseCase.AlocacaoCommand(a.transacaoId(), a.valor())).toList())
                .stream().map(a -> new AlocacaoPagamentoResponse(a.id(), a.transacaoId(), a.pagadorId(),
                        a.valor().valor(), a.criadaEm(), a.canceladaEm(), a.canceladaPor()))
                .toList();
    }

    @DeleteMapping("/{divisaoId}/transacoes/{transacaoId}/alocacoes/{alocacaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancelarAlocacao(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long divisaoId,
            @PathVariable @Positive Long transacaoId, @PathVariable @Positive Long alocacaoId) {
        vinculos.cancelarAlocacao(usuarioId, divisaoId, transacaoId, alocacaoId);
    }

    @PostMapping("/{divisaoId}/reembolsos")
    @ResponseStatus(HttpStatus.CREATED)
    ReembolsoResponse registrarReembolso(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long divisaoId,
            @Valid @RequestBody ReembolsoRequest request) {
        return ReembolsoResponse.from(vinculos.registrarReembolso(usuarioId, divisaoId, request.transacaoId(),
                request.recebedorId(), request.valor()));
    }

    @GetMapping("/{divisaoId}/reembolsos")
    List<ReembolsoResponse> listarReembolsos(@UsuarioAtual Long usuarioId,
            @PathVariable @Positive Long divisaoId) {
        return vinculos.listarReembolsos(usuarioId, divisaoId).stream().map(ReembolsoResponse::from).toList();
    }

    @DeleteMapping("/{divisaoId}/reembolsos/{reembolsoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancelarReembolso(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long divisaoId,
            @PathVariable @Positive Long reembolsoId) {
        vinculos.cancelarReembolso(usuarioId, divisaoId, reembolsoId);
    }

    @GetMapping("/{divisaoId}/transacoes/{transacaoId}/pagamento")
    ResumoPagamentoResponse consultarPagamento(@UsuarioAtual Long usuarioId,
            @PathVariable @Positive Long divisaoId, @PathVariable @Positive Long transacaoId) {
        var resumo = vinculos.consultarPagamento(usuarioId, divisaoId, transacaoId);
        return new ResumoPagamentoResponse(resumo.divisaoId(), resumo.transacaoId(),
                resumo.baseCompartilhada().valor(), resumo.pago().valor(), resumo.pendente().valor(), resumo.status(),
                resumo.alocacoes().stream().map(a -> new AlocacaoPagamentoResponse(a.id(), a.transacaoId(),
                        a.pagadorId(), a.valor().valor(), a.criadaEm(), a.canceladaEm(), a.canceladaPor())).toList());
    }

    @GetMapping("/{divisaoId}/transacoes/pendentes-revisao")
    List<VinculoPendenteResponse> listarPendentes(@UsuarioAtual Long usuarioId,
            @PathVariable @Positive Long divisaoId) {
        return vinculos.listarPendentes(usuarioId, divisaoId).stream().map(VinculoPendenteResponse::from).toList();
    }

    @PutMapping("/{divisaoId}/transacoes/{transacaoId}/responsabilidades")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revisar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long divisaoId,
            @PathVariable @Positive Long transacaoId, @Valid @RequestBody RevisarResponsabilidadesRequest request) {
        vinculos.revisar(usuarioId, divisaoId, transacaoId, request.responsabilidades().stream()
                .map(r -> new AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand(r.usuarioId(),
                        r.percentual(), r.valorDevido())).toList());
    }

    @GetMapping("/{divisaoId}/resumo")
    ResumoResponse resumo(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long divisaoId,
            @RequestParam @NotNull LocalDate inicio, @RequestParam @NotNull LocalDate fim) {
        return ResumoResponse.from(resumos.consultar(usuarioId, divisaoId, inicio, fim));
    }

    private List<DivisaoCompartilhadaUseCase.ParticipanteCommand> participantes(List<ParticipanteRequest> participantes) {
        return participantes.stream().map(participante -> new DivisaoCompartilhadaUseCase.ParticipanteCommand(
                participante.usuarioId(), participante.percentual())).toList();
    }

    record CriarRequest(@NotBlank @Size(max = 120) String nome, @NotEmpty List<@Valid ParticipanteRequest> participantes) { }
    record ParticipantesRequest(@NotEmpty List<@Valid ParticipanteRequest> participantes) { }
    record ParticipanteRequest(@Positive Long usuarioId, @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal percentual) { }
    record AssociarTransacaoRequest(@Positive Long transacaoId,
            @DecimalMin("0.01") BigDecimal baseCompartilhada,
            List<@Valid ResponsabilidadeRequest> responsabilidades) { }
    record RevisarResponsabilidadesRequest(
            @NotEmpty List<@Valid ResponsabilidadeRequest> responsabilidades) { }
    record ResponsabilidadeRequest(@NotNull @Positive Long usuarioId,
            @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal percentual,
            @DecimalMin("0.01") BigDecimal valorDevido) { }
    record VinculoPendenteResponse(Long transacaoId, Long pagadorId, LocalDate data, String descricao,
            BigDecimal valor) {
        static VinculoPendenteResponse from(AssociarTransacaoDivisaoUseCase.VinculoPendente vinculo) {
            return new VinculoPendenteResponse(vinculo.transacaoId(), vinculo.pagadorId(), vinculo.data(),
                    vinculo.descricao(), vinculo.valor());
        }
    }

    record DivisaoResponse(Long id, String nome, Long criadorId, StatusDivisaoCompartilhada status,
            List<ParticipanteResponse> participantes) {
        static DivisaoResponse from(DivisaoCompartilhada divisao) {
            return new DivisaoResponse(divisao.getId(), divisao.getNome(), divisao.getCriadorId(), divisao.getStatus(),
                    divisao.getParticipantes().stream().map(participante -> new ParticipanteResponse(
                            participante.usuarioId(), participante.percentual())).toList());
        }
    }

    record ParticipanteResponse(Long usuarioId, BigDecimal percentual) { }
    record HistoricoParticipanteResponse(Long usuarioId, BigDecimal percentual,
            LocalDateTime vigenteDesde, LocalDateTime vigenteAte) { }
    record AlocacaoPagamentoResponse(Long id, Long transacaoId, Long pagadorId, BigDecimal valor,
            LocalDateTime criadaEm, LocalDateTime canceladaEm, Long canceladaPor) { }
    record AlocacoesPagamentoRequest(@NotEmpty List<@Valid AlocacaoPagamentoRequest> alocacoes) { }
    record AlocacaoPagamentoRequest(@NotNull @Positive Long transacaoId,
            @NotNull @DecimalMin("0.01") BigDecimal valor) { }
    record ResumoPagamentoResponse(Long divisaoId, Long transacaoId, BigDecimal baseCompartilhada,
            BigDecimal pago, BigDecimal pendente, StatusPagamentoDivisao status,
            List<AlocacaoPagamentoResponse> alocacoes) { }
    record ReembolsoRequest(@NotNull @Positive Long transacaoId, @NotNull @Positive Long recebedorId,
            @NotNull @DecimalMin("0.01") BigDecimal valor) { }
    record ReembolsoResponse(Long id, Long transacaoId, Long pagadorId, Long recebedorId, BigDecimal valor,
            LocalDate data, LocalDateTime criadoEm, LocalDateTime canceladoEm, Long canceladoPor) {
        static ReembolsoResponse from(com.finisus.domain.model.ReembolsoDivisao r) {
            return new ReembolsoResponse(r.id(), r.transacaoId(), r.pagadorId(), r.recebedorId(), r.valor().valor(),
                    r.data(), r.criadoEm(), r.canceladoEm(), r.canceladoPor());
        }
    }
    record ResumoResponse(Long divisaoId, String divisao, BigDecimal total,
            List<ResumoParticipanteResponse> participantes, long lancamentosPendentesRevisao) {
        static ResumoResponse from(ResumoDivisao resumo) {
            return new ResumoResponse(resumo.divisaoId(), resumo.divisao(), resumo.total().valor(),
                    resumo.participantes().stream().map(ResumoParticipanteResponse::from).toList(),
                    resumo.lancamentosPendentesRevisao());
        }
    }
    record ResumoParticipanteResponse(Long usuarioId, BigDecimal percentual, BigDecimal pago, BigDecimal devido,
            BigDecimal saldo) {
        static ResumoParticipanteResponse from(ResumoParticipanteDivisao participante) {
            return new ResumoParticipanteResponse(participante.usuarioId(), participante.percentual(),
                    participante.pago().valor(), participante.devido().valor(), participante.saldo());
        }
    }
}
