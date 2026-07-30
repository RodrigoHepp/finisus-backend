package com.financeiro.adapters.in.web;

import com.financeiro.application.ports.in.FinanceiroCoreUseCase;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.*;
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
@RequestMapping("/api/v1")
@Validated
public class FinanceiroCoreController {
    private final FinanceiroCoreUseCase useCase;
    public FinanceiroCoreController(FinanceiroCoreUseCase useCase) { this.useCase = useCase; }
    @GetMapping("/bancos") PaginaResponse<BancoResponse> listarBancos(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) { return PaginaResponse.from(useCase.listarBancos(userId(jwt), new Paginacao(pagina, tamanho)).map(BancoResponse::from)); }
    @PostMapping("/bancos") @ResponseStatus(HttpStatus.CREATED) BancoResponse criarBanco(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody BancoRequest r) { return BancoResponse.from(useCase.criarBanco(userId(jwt), new FinanceiroCoreUseCase.CriarBancoCommand(r.nome(), r.codigo()))); }
    @GetMapping("/bancos/{bancoId}") BancoResponse buscarBanco(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long bancoId) { return BancoResponse.from(useCase.buscarBanco(userId(jwt), bancoId)); }
    @PatchMapping("/bancos/{bancoId}") BancoResponse atualizarBanco(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long bancoId, @Valid @RequestBody BancoRequest r) { return BancoResponse.from(useCase.atualizarBanco(userId(jwt), bancoId, new FinanceiroCoreUseCase.CriarBancoCommand(r.nome(), r.codigo()))); }
    @DeleteMapping("/bancos/{bancoId}") @ResponseStatus(HttpStatus.NO_CONTENT) void inativarBanco(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long bancoId) { useCase.inativarBanco(userId(jwt), bancoId); }
    @GetMapping("/contas") PaginaResponse<ContaResponse> listarContas(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) { return PaginaResponse.from(useCase.listarContas(userId(jwt), new Paginacao(pagina, tamanho)).map(ContaResponse::from)); }
    @PostMapping("/contas") @ResponseStatus(HttpStatus.CREATED) ContaResponse criarConta(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ContaRequest r) { return ContaResponse.from(useCase.criarConta(userId(jwt), new FinanceiroCoreUseCase.CriarContaCommand(r.nome(), r.tipo(), r.bancoId()))); }
    @GetMapping("/contas/{contaId}") ContaResponse buscarConta(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long contaId) { return ContaResponse.from(useCase.buscarConta(userId(jwt), contaId)); }
    @PatchMapping("/contas/{contaId}") ContaResponse atualizarConta(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long contaId, @Valid @RequestBody ContaRequest r) { return ContaResponse.from(useCase.atualizarConta(userId(jwt), contaId, new FinanceiroCoreUseCase.CriarContaCommand(r.nome(), r.tipo(), r.bancoId()))); }
    @DeleteMapping("/contas/{contaId}") @ResponseStatus(HttpStatus.NO_CONTENT) void inativarConta(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long contaId) { useCase.inativarConta(userId(jwt), contaId); }
    @GetMapping("/categorias") PaginaResponse<CategoriaResponse> listarCategorias(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) { return PaginaResponse.from(useCase.listarCategorias(userId(jwt), new Paginacao(pagina, tamanho)).map(CategoriaResponse::from)); }
    @PostMapping("/categorias") @ResponseStatus(HttpStatus.CREATED) CategoriaResponse criarCategoria(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CategoriaRequest r) { return CategoriaResponse.from(useCase.criarCategoria(userId(jwt), new FinanceiroCoreUseCase.CriarCategoriaCommand(r.nome(), r.categoriaPaiId()))); }
    @GetMapping("/categorias/{categoriaId}") CategoriaResponse buscarCategoria(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long categoriaId) { return CategoriaResponse.from(useCase.buscarCategoria(userId(jwt), categoriaId)); }
    @PatchMapping("/categorias/{categoriaId}") CategoriaResponse atualizarCategoria(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long categoriaId, @Valid @RequestBody CategoriaRequest r) { return CategoriaResponse.from(useCase.atualizarCategoria(userId(jwt), categoriaId, new FinanceiroCoreUseCase.CriarCategoriaCommand(r.nome(), r.categoriaPaiId()))); }
    @DeleteMapping("/categorias/{categoriaId}") @ResponseStatus(HttpStatus.NO_CONTENT) void inativarCategoria(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long categoriaId) { useCase.inativarCategoria(userId(jwt), categoriaId); }
    @GetMapping("/meios-pagamento") PaginaResponse<MeioPagamentoResponse> listarMeiosPagamento(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) { return PaginaResponse.from(useCase.listarMeiosPagamento(userId(jwt), new Paginacao(pagina, tamanho)).map(MeioPagamentoResponse::from)); }
    @PostMapping("/meios-pagamento") @ResponseStatus(HttpStatus.CREATED) MeioPagamentoResponse criarMeioPagamento(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MeioPagamentoRequest r) { return MeioPagamentoResponse.from(useCase.criarMeioPagamento(userId(jwt), new FinanceiroCoreUseCase.CriarMeioPagamentoCommand(r.nome()))); }
    @GetMapping("/meios-pagamento/{meioPagamentoId}") MeioPagamentoResponse buscarMeioPagamento(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long meioPagamentoId) { return MeioPagamentoResponse.from(useCase.buscarMeioPagamento(userId(jwt), meioPagamentoId)); }
    @PatchMapping("/meios-pagamento/{meioPagamentoId}") MeioPagamentoResponse atualizarMeioPagamento(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long meioPagamentoId, @Valid @RequestBody MeioPagamentoRequest r) { return MeioPagamentoResponse.from(useCase.atualizarMeioPagamento(userId(jwt), meioPagamentoId, new FinanceiroCoreUseCase.CriarMeioPagamentoCommand(r.nome()))); }
    @DeleteMapping("/meios-pagamento/{meioPagamentoId}") @ResponseStatus(HttpStatus.NO_CONTENT) void inativarMeioPagamento(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long meioPagamentoId) { useCase.inativarMeioPagamento(userId(jwt), meioPagamentoId); }
    @GetMapping("/transacoes") PaginaResponse<TransacaoResponse> listarTransacoes(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) { return PaginaResponse.from(useCase.listarTransacoes(userId(jwt), new Paginacao(pagina, tamanho)).map(TransacaoResponse::from)); }
    @PostMapping("/transacoes") @ResponseStatus(HttpStatus.CREATED) TransacaoResponse registrarTransacao(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TransacaoRequest r) {
        var itens = r.itens() == null ? List.<FinanceiroCoreUseCase.ItemCommand>of() : r.itens().stream().map(i -> new FinanceiroCoreUseCase.ItemCommand(i.descricao(), i.valor(), i.categoriaId())).toList();
        return TransacaoResponse.from(useCase.registrarTransacao(userId(jwt), new FinanceiroCoreUseCase.RegistrarTransacaoCommand(r.tipo(), r.valor(), r.data(), r.descricao(), r.contaId(), r.categoriaId(), r.meioPagamentoId(), itens)));
    }
    @GetMapping("/transacoes/{transacaoId}") TransacaoResponse buscarTransacao(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long transacaoId) { return TransacaoResponse.from(useCase.buscarTransacao(userId(jwt), transacaoId)); }
    @PatchMapping("/transacoes/{transacaoId}") TransacaoResponse corrigirTransacao(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long transacaoId, @Valid @RequestBody TransacaoRequest r) { return TransacaoResponse.from(useCase.corrigirTransacao(userId(jwt), transacaoId, command(r))); }
    @PostMapping("/transacoes/{transacaoId}/estorno") TransacaoResponse estornarTransacao(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long transacaoId) { return TransacaoResponse.from(useCase.estornarTransacao(userId(jwt), transacaoId)); }
    @GetMapping("/transacoes/{transacaoId}/historico") PaginaResponse<HistoricoResponse> historico(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long transacaoId, @RequestParam(defaultValue = "0") @PositiveOrZero int pagina, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) { return PaginaResponse.from(useCase.listarHistoricoTransacao(userId(jwt), transacaoId, new Paginacao(pagina, tamanho)).map(HistoricoResponse::from)); }
    private FinanceiroCoreUseCase.RegistrarTransacaoCommand command(TransacaoRequest r) { var itens = r.itens() == null ? List.<FinanceiroCoreUseCase.ItemCommand>of() : r.itens().stream().map(i -> new FinanceiroCoreUseCase.ItemCommand(i.descricao(), i.valor(), i.categoriaId())).toList(); return new FinanceiroCoreUseCase.RegistrarTransacaoCommand(r.tipo(), r.valor(), r.data(), r.descricao(), r.contaId(), r.categoriaId(), r.meioPagamentoId(), itens); }
    private Long userId(Jwt jwt) { return Long.valueOf(jwt.getSubject()); }
    record BancoRequest(@NotBlank @Size(max = 150) String nome, @NotBlank @Size(max = 20) String codigo) {}
    record ContaRequest(@NotBlank @Size(max = 150) String nome, @NotNull TipoConta tipo, @Positive Long bancoId) {}
    record CategoriaRequest(@NotBlank @Size(max = 100) String nome, @Positive Long categoriaPaiId) {}
    record MeioPagamentoRequest(@NotBlank @Size(max = 100) String nome) {}
    record TransacaoRequest(@NotNull TipoTransacao tipo, @NotNull @DecimalMin("0.01") BigDecimal valor, @NotNull LocalDate data, @NotBlank @Size(max = 500) String descricao, @NotNull @Positive Long contaId, @Positive Long categoriaId, @Positive Long meioPagamentoId, List<@Valid ItemRequest> itens) {}
    record ItemRequest(@NotBlank @Size(max = 300) String descricao, @NotNull @DecimalMin("0.01") BigDecimal valor, @Positive Long categoriaId) {}
    record BancoResponse(Long id, String nome, String codigo, boolean sistema) { static BancoResponse from(Banco b) { return new BancoResponse(b.getId(), b.getNome(), b.getCodigo(), b.isSistema()); } }
    record ContaResponse(Long id, String nome, TipoConta tipo, Long bancoId, BigDecimal saldo, boolean ativo) { static ContaResponse from(Conta c) { return new ContaResponse(c.getId(), c.getNome(), c.getTipo(), c.getBancoId(), c.getSaldo().valor(), c.isAtivo()); } }
    record CategoriaResponse(Long id, String nome, Long categoriaPaiId, boolean ativo) { static CategoriaResponse from(Categoria c) { return new CategoriaResponse(c.getId(), c.getNome(), c.getCategoriaPaiId(), c.isAtivo()); } }
    record MeioPagamentoResponse(Long id, String nome, boolean ativo) { static MeioPagamentoResponse from(MeioPagamento m) { return new MeioPagamentoResponse(m.getId(), m.getNome(), m.isAtivo()); } }
    record TransacaoResponse(Long id, TipoTransacao tipo, BigDecimal valor, LocalDate data, String descricao, Long contaId, Long categoriaId, Long meioPagamentoId, List<ItemResponse> itens) { static TransacaoResponse from(Transacao t) { return new TransacaoResponse(t.getId(), t.getTipo(), t.getValor().valor(), t.getData(), t.getDescricao(), t.getContaId(), t.getCategoriaId(), t.getMeioPagamentoId(), t.getItens().stream().map(ItemResponse::from).toList()); } }
    record ItemResponse(Long id, String descricao, BigDecimal valor, Long categoriaId) { static ItemResponse from(TransacaoItem i) { return new ItemResponse(i.getId(), i.getDescricao(), i.getValor().valor(), i.getCategoriaId()); } }
    record HistoricoResponse(Long id, String campoAlterado, String valorAnterior, String valorNovo, Long alteradoPor, java.time.LocalDateTime alteradoEm) { static HistoricoResponse from(TransacaoHistorico h) { return new HistoricoResponse(h.getId(), h.getCampoAlterado(), h.getValorAnterior(), h.getValorNovo(), h.getAlteradoPor(), h.getAlteradoEm()); } }
}
