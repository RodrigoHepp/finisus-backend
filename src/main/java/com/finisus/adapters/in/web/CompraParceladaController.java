package com.finisus.adapters.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import com.finisus.adapters.in.web.security.UsuarioAtual;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.CompraParceladaUseCase;
import com.finisus.domain.model.CompraParcelada;

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
@RequestMapping("/api/v1/compras-parceladas")
@Validated
@Tag(name = "Compras parceladas", description = "Compras parceladas e seus lançamentos futuros.")
@SecurityRequirement(name = "bearerAuth")
public class CompraParceladaController {
	private final CompraParceladaUseCase useCase;

	public CompraParceladaController(CompraParceladaUseCase useCase) {
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
		return Response.from(useCase.criar(usuarioId, new CompraParceladaUseCase.CriarCommand(r.descricao(),
				r.valorTotal(), r.numeroParcelas(), r.dataCompra(), r.categoriaId(), r.contaId())));
	}

	@GetMapping("/{compraId}")
	Response buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long compraId) {
		return Response.from(useCase.buscar(usuarioId, compraId));
	}

	@PostMapping("/{compraId}/cancelar")
	Response cancelar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long compraId) {
		return Response.from(useCase.cancelar(usuarioId, compraId));
	}

	record Request(@NotBlank @Size(max = 300) String descricao, @NotNull @DecimalMin("0.01") BigDecimal valorTotal,
			@Min(1) int numeroParcelas, @NotNull LocalDate dataCompra, @Positive Long categoriaId,
			@NotNull @Positive Long contaId) {
	}

	record Response(Long id, String descricao, BigDecimal valorTotal, int numeroParcelas, LocalDate dataCompra,
			Long categoriaId, Long contaId, java.time.LocalDateTime canceladaEm) {
		static Response from(CompraParcelada c) {
			return new Response(c.getId(), c.getDescricao(), c.getValorTotal().valor(), c.getNumeroParcelas(),
					c.getDataCompra(), c.getCategoriaId(), c.getContaId(), c.getCanceladaEm());
		}
	}
}
