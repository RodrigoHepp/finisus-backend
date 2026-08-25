package com.finisus.adapters.in.web;

import com.finisus.adapters.in.web.security.UsuarioAtual;
import com.finisus.application.ports.in.ImportacaoFinanceiraUseCase;
import com.finisus.domain.model.ImportacaoFinanceira;
import com.finisus.domain.model.LancamentoImportado;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import com.finisus.domain.model.TipoTransacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.finisus.domain.DomainException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/importacoes-financeiras")
@Validated
@Tag(name = "Importações financeiras", description = "Leitura de PDFs, revisão humana e confirmação de lançamentos.")
@SecurityRequirement(name = "bearerAuth")
public class ImportacaoFinanceiraController {
	private final ImportacaoFinanceiraUseCase useCase;

	public ImportacaoFinanceiraController(ImportacaoFinanceiraUseCase useCase) {
		this.useCase = useCase;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	RevisaoResponse iniciar(@UsuarioAtual Long usuarioId, @RequestParam @Positive Long bancoId,
			@RequestPart("arquivo") MultipartFile arquivo) {
		try {
			return RevisaoResponse.from(useCase.iniciar(usuarioId, new ImportacaoFinanceiraUseCase.IniciarCommand(bancoId,
					arquivo.getOriginalFilename(), arquivo.getContentType(), arquivo.getBytes())));
		} catch (java.io.IOException exception) {
			throw new DomainException("error.importacao.arquivo.invalido");
		}
	}

	@GetMapping("/{importacaoId}")
	RevisaoResponse buscar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long importacaoId) {
		return RevisaoResponse.from(useCase.buscar(usuarioId, importacaoId));
	}

	@PutMapping("/{importacaoId}/revisao")
	RevisaoResponse revisar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long importacaoId,
			@Valid @org.springframework.web.bind.annotation.RequestBody RevisarRequest request) {
		return RevisaoResponse.from(useCase.revisar(usuarioId, importacaoId,
				new ImportacaoFinanceiraUseCase.RevisarCommand(request.contaId(), request.faturaId(),
						request.lancamentos().stream().map(LancamentoRequest::toCommand).toList())));
	}

	@PostMapping("/{importacaoId}/confirmar")
	ImportacaoResponse confirmar(@UsuarioAtual Long usuarioId, @PathVariable @Positive Long importacaoId) {
		return ImportacaoResponse.from(useCase.confirmar(usuarioId, importacaoId));
	}

	record RevisarRequest(@Positive Long contaId, @Positive Long faturaId,
			@NotEmpty List<@Valid LancamentoRequest> lancamentos) { }

	record LancamentoRequest(@NotNull @Positive Long id, LocalDate data, String descricao, BigDecimal valor,
			TipoTransacao tipo, boolean importar, @Positive Long categoriaId, @Positive Long itemId) {
		ImportacaoFinanceiraUseCase.LancamentoCommand toCommand() {
			return new ImportacaoFinanceiraUseCase.LancamentoCommand(id, data, descricao, valor, tipo, importar,
					categoriaId, itemId);
		}
	}

	record RevisaoResponse(ImportacaoResponse importacao, List<PossivelDuplicidadeResponse> possiveisDuplicidades) {
		static RevisaoResponse from(ImportacaoFinanceiraUseCase.Revisao revisao) {
			return new RevisaoResponse(ImportacaoResponse.from(revisao.importacao()), revisao.possiveisDuplicidades().stream()
					.map(PossivelDuplicidadeResponse::from).toList());
		}
	}

	record PossivelDuplicidadeResponse(Long lancamentoImportadoId, Long transacaoId, LocalDate data, BigDecimal valor,
			String descricao) {
		static PossivelDuplicidadeResponse from(ImportacaoFinanceiraUseCase.PossivelDuplicidade duplicidade) {
			return new PossivelDuplicidadeResponse(duplicidade.lancamentoImportadoId(), duplicidade.transacaoId(),
					duplicidade.data(), duplicidade.valor(), duplicidade.descricao());
		}
	}

	record ImportacaoResponse(Long id, Long bancoId, String nomeArquivo, String leitor,
			TipoDocumentoFinanceiro tipoDocumento, String identificadorOrigem, LocalDate periodoInicio, LocalDate periodoFim, LocalDate dataVencimento,
			BigDecimal saldoInicial, BigDecimal saldoFinal, BigDecimal valorTotal, String status, Long contaId,
			Long faturaId, List<LancamentoResponse> lancamentos) {
		static ImportacaoResponse from(ImportacaoFinanceira importacao) {
			return new ImportacaoResponse(importacao.getId(), importacao.getBancoId(), importacao.getNomeArquivo(),
					importacao.getLeitor(), importacao.getTipoDocumento(), importacao.getIdentificadorOrigem(), importacao.getPeriodoInicio(),
					importacao.getPeriodoFim(), importacao.getDataVencimento(), importacao.getSaldoInicial(),
					importacao.getSaldoFinal(), importacao.getValorTotal(), importacao.getStatus().name(),
					importacao.getContaId(), importacao.getFaturaId(), importacao.getLancamentos().stream()
							.map(LancamentoResponse::from).toList());
		}
	}

	record LancamentoResponse(Long id, int ordem, LocalDate data, String descricao, String conteudoOriginal,
			BigDecimal valor, TipoTransacao tipo, boolean pendenteConfirmacao, String motivoPendencia, boolean importar,
			Long categoriaId, Long itemId, Long transacaoId) {
		static LancamentoResponse from(LancamentoImportado lancamento) {
			return new LancamentoResponse(lancamento.getId(), lancamento.getOrdem(), lancamento.getData(),
					lancamento.getDescricao(), lancamento.getConteudoOriginal(), lancamento.getValor(), lancamento.getTipo(),
					lancamento.isPendenteConfirmacao(), lancamento.getMotivoPendencia(), lancamento.isImportar(),
					lancamento.getCategoriaId(), lancamento.getItemId(), lancamento.getTransacaoId());
		}
	}
}
