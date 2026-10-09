package com.finisus.adapters.in.web;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import com.finisus.adapters.in.web.security.UsuarioAtual;
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

import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.RecorrenciaUseCase;
import com.finisus.domain.model.Recorrencia;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.OcorrenciaRecorrencia;
import com.finisus.domain.model.StatusOcorrenciaRecorrencia;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/recorrencias")
@Validated
@Tag(name = "Recorrências", description = "Lançamentos recorrentes e geração mensal.")
@SecurityRequirement(name = "bearerAuth")
public class RecorrenciaController {
	private final RecorrenciaUseCase useCase;

	public RecorrenciaController(RecorrenciaUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping
	PaginaResponse<Response> listar(@UsuarioAtual Long usuarioId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
		return PaginaResponse.from(useCase.listar(usuarioId, new Paginacao(pagina, tamanho)).map(Response::from));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	Response criar(@UsuarioAtual Long usuarioId, @Valid @RequestBody Request r) {
		return Response.from(useCase.criar(usuarioId, new RecorrenciaUseCase.CriarCommand(r.nome(), r.tipo(),
				r.valorEsperado(), r.diaDoMes(), r.categoriaId(), r.contaId(), r.meioPagamentoId())));
	}

	@GetMapping("/{recorrenciaId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long recorrenciaId) {
		return Response.from(useCase.buscar(usuarioId, recorrenciaId));
	}

	@PatchMapping("/{recorrenciaId}")
	Response atualizar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long recorrenciaId,
			@Valid @RequestBody Request r) {
		return Response.from(useCase.atualizar(usuarioId, recorrenciaId, new RecorrenciaUseCase.CriarCommand(r.nome(),
				r.tipo(), r.valorEsperado(), r.diaDoMes(), r.categoriaId(), r.contaId(), r.meioPagamentoId())));
	}

	@DeleteMapping("/{recorrenciaId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void inativar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long recorrenciaId) {
		useCase.inativar(usuarioId, recorrenciaId);
	}

	@PostMapping("/geracoes/{anoMes}")
	List<OcorrenciaResponse> gerar(@UsuarioAtual Long usuarioId,
			@PathVariable @Pattern(regexp = "\\d{4}-\\d{2}") String anoMes) {
		return useCase.gerarMes(usuarioId, anoMes).stream().map(OcorrenciaResponse::from).toList();
	}

	@GetMapping("/ocorrencias/{anoMes}")
	List<OcorrenciaResponse> listarOcorrencias(@UsuarioAtual Long usuarioId,
			@PathVariable @Pattern(regexp = "\\d{4}-\\d{2}") String anoMes) {
		return useCase.listarOcorrencias(usuarioId, anoMes).stream().map(OcorrenciaResponse::from).toList();
	}

	@PostMapping("/ocorrencias/{ocorrenciaId}/realizar")
	OcorrenciaResponse realizar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long ocorrenciaId) {
		return OcorrenciaResponse.from(useCase.realizar(usuarioId, ocorrenciaId));
	}

	record Request(@NotBlank @Size(max = 150) String nome, @NotNull TipoTransacao tipo,
			@NotNull @DecimalMin("0.01") BigDecimal valorEsperado, @Min(1) @Max(31) int diaDoMes,
			@Positive Long categoriaId, @NotNull @Positive Long contaId, @Positive Long meioPagamentoId) {
	}

	record Response(Long id, String nome, TipoTransacao tipo, BigDecimal valorEsperado, int diaDoMes, Long categoriaId,
			Long contaId, Long meioPagamentoId, boolean ativo) {
		static Response from(Recorrencia r) {
			return new Response(r.getId(), r.getNome(), r.getTipo(), r.getValorEsperado().valor(), r.getDiaDoMes(),
					r.getCategoriaId(), r.getContaId(), r.getMeioPagamentoId(), r.isAtivo());
		}
	}

	record OcorrenciaResponse(Long id, Long recorrenciaId, String anoMes, LocalDate vencimento, TipoTransacao tipo,
			BigDecimal valor, String descricao, Long contaId, Long categoriaId, Long meioPagamentoId,
			StatusOcorrenciaRecorrencia status, Long transacaoId) {
		static OcorrenciaResponse from(OcorrenciaRecorrencia o) {
			return new OcorrenciaResponse(o.id(), o.recorrenciaId(), o.anoMes(), o.vencimento(), o.tipo(),
					o.valor().valor(), o.descricao(), o.contaId(), o.categoriaId(), o.meioPagamentoId(),
					o.status(), o.transacaoId());
		}
	}
}
