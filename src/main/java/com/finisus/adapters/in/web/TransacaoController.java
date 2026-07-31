package com.finisus.adapters.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import com.finisus.adapters.in.web.security.UsuarioAtual;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
		return PaginaResponse
				.from(transacoes.listar(usuarioId, new Paginacao(pagina, tamanho)).map(TransacaoResponse::from));
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
			@Valid @RequestBody Request request) {
		return TransacaoResponse.from(transacoes.corrigir(usuarioId, transacaoId, command(request)));
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
				: request.itens().stream().map(item -> new TransacaoUseCase.ItemCommand(item.itemId(), item.valor()))
						.toList();
		return new TransacaoUseCase.RegistrarCommand(request.tipo(), request.valor(), request.data(),
				request.descricao(), request.contaId(), request.categoriaId(), request.meioPagamentoId(), itens);
	}

	record Request(@NotNull TipoTransacao tipo, @NotNull @DecimalMin("0.01") BigDecimal valor, @NotNull LocalDate data,
			@NotBlank @Size(max = 500) String descricao, @NotNull @Positive Long contaId, @Positive Long categoriaId,
			@Positive Long meioPagamentoId, List<@Valid ItemRequest> itens) {
	}

	record ItemRequest(@NotNull @Positive Long itemId, @NotNull @DecimalMin("0.01") BigDecimal valor) {
	}

	record HistoricoResponse(Long id, String campoAlterado, String valorAnterior, String valorNovo, Long alteradoPor,
			LocalDateTime alteradoEm) {
		static HistoricoResponse from(TransacaoHistorico historico) {
			return new HistoricoResponse(historico.getId(), historico.getCampoAlterado(), historico.getValorAnterior(),
					historico.getValorNovo(), historico.getAlteradoPor(), historico.getAlteradoEm());
		}
	}
}
