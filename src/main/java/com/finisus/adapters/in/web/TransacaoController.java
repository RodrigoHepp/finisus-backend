package com.finisus.adapters.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DateTimeException;
import java.util.List;

import org.springframework.http.HttpStatus;
import com.finisus.adapters.in.web.security.UsuarioAtual;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.ConsultarHistoricoTransacaoUseCase;
import com.finisus.application.ports.in.TransacaoUseCase;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.TransacaoHistorico;
import com.finisus.domain.DomainException;
import com.finisus.domain.vo.AnoMes;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/transacoes")
@Validated
@Tag(name = "Transações", description = "Lançamentos financeiros e seu histórico imutável.")
@SecurityRequirement(name = "bearerAuth")
public class TransacaoController {
	private final TransacaoUseCase transacoes;
	private final ConsultarHistoricoTransacaoUseCase historicos;

	public TransacaoController(TransacaoUseCase transacoes, ConsultarHistoricoTransacaoUseCase historicos) {
		this.transacoes = transacoes;
		this.historicos = historicos;
	}

	@GetMapping
	PaginaResponse<TransacaoResponse> listar(@UsuarioAtual Long usuarioId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho,
			@RequestParam(required = false) @Pattern(regexp = "\\d{4}-\\d{2}") String mes,
			@RequestParam(required = false) TipoTransacao tipo,
			@RequestParam(required = false) @Positive Long categoriaId) {
		var filtro = new TransacaoUseCase.FiltroListagem(mes == null ? null : anoMes(mes), tipo, categoriaId);
		return PaginaResponse.from(
				transacoes.listar(usuarioId, new Paginacao(pagina, tamanho), filtro).map(TransacaoResponse::from));
	}

	private AnoMes anoMes(String mes) {
		try {
			return AnoMes.parse(mes);
		} catch (DateTimeException exception) {
			throw new DomainException("error.anomes.invalid");
		}
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	TransacaoResponse registrar(@UsuarioAtual Long usuarioId, @Valid @RequestBody Request request) {
		return TransacaoResponse.from(transacoes.registrar(usuarioId, command(request)));
	}

	@GetMapping("/{transacaoId}")
	TransacaoResponse buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long transacaoId) {
		return TransacaoResponse.from(transacoes.buscar(usuarioId, transacaoId));
	}

	@PatchMapping("/{transacaoId}")
	TransacaoResponse corrigir(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long transacaoId,
			@Valid @RequestBody CorrecaoRequest request) {
		return TransacaoResponse.from(transacoes.corrigir(usuarioId, transacaoId,
				new TransacaoUseCase.CorrigirCommand(command(request), request.motivo())));
	}

	@PutMapping("/{transacaoId}/itens")
	TransacaoResponse detalhar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long transacaoId,
			@Valid @RequestBody DetalhamentoRequest request) {
		List<TransacaoUseCase.ItemCommand> itens = request.itens().stream()
				.map(this::itemCommand).toList();
		return TransacaoResponse.from(transacoes.detalhar(usuarioId, transacaoId,
				new TransacaoUseCase.DetalharCommand(itens, request.motivo())));
	}

	@PostMapping("/{transacaoId}/estorno")
	TransacaoResponse estornar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long transacaoId) {
		return TransacaoResponse.from(transacoes.estornar(usuarioId, transacaoId));
	}

	@GetMapping("/{transacaoId}/historico")
	PaginaResponse<HistoricoResponse> historico(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long transacaoId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
		return PaginaResponse.from(
				historicos.listar(usuarioId, transacaoId, new Paginacao(pagina, tamanho)).map(HistoricoResponse::from));
	}

	private TransacaoUseCase.RegistrarCommand command(Request request) {
		List<TransacaoUseCase.ItemCommand> itens = request.itens() == null ? List.of()
				: request.itens().stream().map(this::itemCommand).toList();
		return new TransacaoUseCase.RegistrarCommand(request.tipo(), request.valor(), request.data(),
				request.descricao(), request.contaId(), request.categoriaId(), request.meioPagamentoId(), itens);
	}

	private TransacaoUseCase.RegistrarCommand command(CorrecaoRequest request) {
		List<TransacaoUseCase.ItemCommand> itens = request.itens() == null ? List.of()
				: request.itens().stream().map(this::itemCommand).toList();
		return new TransacaoUseCase.RegistrarCommand(request.tipo(), request.valor(), request.data(),
				request.descricao(), request.contaId(), request.categoriaId(), request.meioPagamentoId(), itens);
	}

	record Request(@NotNull TipoTransacao tipo, @NotNull @DecimalMin("0.01") BigDecimal valor, @NotNull LocalDate data,
			@NotBlank @Size(max = 500) String descricao, @NotNull @Positive Long contaId, @Positive Long categoriaId,
			@Positive Long meioPagamentoId, List<@Valid ItemRequest> itens) {
	}

	record CorrecaoRequest(@NotNull TipoTransacao tipo, @NotNull @DecimalMin("0.01") BigDecimal valor,
			@NotNull LocalDate data, @NotBlank @Size(max = 500) String descricao, @NotNull @Positive Long contaId,
			@Positive Long categoriaId, @Positive Long meioPagamentoId, List<@Valid ItemRequest> itens,
			@NotBlank @Size(max = 500) String motivo) {
	}

	record DetalhamentoRequest(@NotEmpty List<@Valid ItemRequest> itens,
			@NotBlank @Size(max = 500) String motivo) {
	}

	private TransacaoUseCase.ItemCommand itemCommand(ItemRequest item) {
		return new TransacaoUseCase.ItemCommand(item.itemId(), item.descricao(), item.quantidade(), item.valor(),
				item.categoriaId());
	}

	record ItemRequest(@Positive Long itemId, @Size(max = 300) String descricao,
			@DecimalMin(value = "0.000001") BigDecimal quantidade,
			@NotNull @DecimalMin("0.01") BigDecimal valor, @Positive Long categoriaId) {
	}

	record HistoricoResponse(Long id, String campoAlterado, String valorAnterior, String valorNovo, Long alteradoPor,
			LocalDateTime alteradoEm, String motivo, String correlacaoId, String snapshotAnterior, String snapshotNovo) {
		static HistoricoResponse from(TransacaoHistorico historico) {
			return new HistoricoResponse(historico.getId(), historico.getCampoAlterado(), historico.getValorAnterior(),
					historico.getValorNovo(), historico.getAlteradoPor(), historico.getAlteradoEm(), historico.getMotivo(),
					historico.getCorrelacaoId(), historico.getSnapshotAnterior(), historico.getSnapshotNovo());
		}
	}
}
