package com.finisus.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finisus.domain.DomainException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImportacaoFinanceiraTest {
	@Test
	void exigeAlvoFinanceiroAntesDaConfirmacao() {
		ImportacaoFinanceira importacao = importacao(TipoDocumentoFinanceiro.EXTRATO_CONTA);

		assertThatThrownBy(importacao::confirmar).isInstanceOf(DomainException.class)
				.hasMessage("error.importacao.revisao.invalida");
	}

	@Test
	void confirmaExtratoRevisadoComConta() {
		ImportacaoFinanceira importacao = importacao(TipoDocumentoFinanceiro.EXTRATO_CONTA);
		importacao.revisar(10L, null, List.of(new ImportacaoFinanceira.RevisaoLancamento(20L,
				LocalDate.of(2026, 7, 10), "Mercado", new BigDecimal("80.00"), TipoTransacao.SAIDA, true, null, null)));

		importacao.confirmar();

		assertThat(importacao.getStatus()).isEqualTo(StatusImportacaoFinanceira.CONFIRMADA);
		assertThat(importacao.getLancamentos()).hasSize(1);
		assertThat(importacao.getLancamentos().getFirst().isPendenteConfirmacao()).isFalse();
	}

	@Test
	void permiteRevisaoParcialMasImpedeConfirmacaoComLancamentoPendenteParaImportar() {
		LancamentoImportado revisado = LancamentoImportado.reconstituir(20L, 1, null, "Texto lido", "Texto lido",
				null, null, true, "Confirme os dados.", true, null, null, null);
		LancamentoImportado pendente = LancamentoImportado.reconstituir(21L, 2, LocalDate.of(2026, 7, 11), "Outro texto",
				"Outro texto", new BigDecimal("25.00"), TipoTransacao.SAIDA, true, "Confirme os dados.", true, null, null, null);
		ImportacaoFinanceira importacao = ImportacaoFinanceira.reconstituir(1L, 2L, 3L, "extrato.pdf", "hash",
				"generico-pdf-v1", TipoDocumentoFinanceiro.EXTRATO_CONTA, null, null, null, null, null, null, null,
				StatusImportacaoFinanceira.PENDENTE_REVISAO, null, null, 0, List.of(revisado, pendente));

		importacao.revisar(10L, null, List.of(new ImportacaoFinanceira.RevisaoLancamento(20L,
				LocalDate.of(2026, 7, 10), "Mercado", new BigDecimal("80.00"), TipoTransacao.SAIDA, true, null, null)));

		assertThat(importacao.getLancamentos().getFirst().isPendenteConfirmacao()).isFalse();
		assertThat(importacao.getLancamentos().get(1).isPendenteConfirmacao()).isTrue();
		assertThatThrownBy(importacao::confirmar).isInstanceOf(DomainException.class)
				.hasMessage("error.importacao.revisao.invalida");
	}

	private ImportacaoFinanceira importacao(TipoDocumentoFinanceiro tipo) {
		LancamentoImportado lancamento = LancamentoImportado.reconstituir(20L, 1, null, "Texto lido", "Texto lido",
				null, null, true, "Confirme os dados.", true, null, null, null);
		return ImportacaoFinanceira.reconstituir(1L, 2L, 3L, "extrato.pdf", "hash", "generico-pdf-v1", tipo, null,
				null, null, null, null, null, null, StatusImportacaoFinanceira.PENDENTE_REVISAO, null, null, 0,
				List.of(lancamento));
	}
}
