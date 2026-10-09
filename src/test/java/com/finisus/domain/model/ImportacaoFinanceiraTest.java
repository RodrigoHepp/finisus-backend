package com.finisus.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finisus.domain.DomainException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
		importacao.revisar(2L, 10L, null, List.of(new ImportacaoFinanceira.RevisaoLancamento(20L,
				LocalDate.of(2026, 7, 10), "Mercado", new BigDecimal("80.00"), TipoTransacao.SAIDA, true, null, null,
				null, null, "Dados conferidos no PDF")), LocalDateTime.of(2026, 9, 29, 10, 0));

		importacao.confirmar();

		assertThat(importacao.getStatus()).isEqualTo(StatusImportacaoFinanceira.CONFIRMADA);
		assertThat(importacao.getLancamentos()).hasSize(1);
		assertThat(importacao.getLancamentos().getFirst().isPendenteConfirmacao()).isFalse();
		assertThat(importacao.getLancamentos().getFirst().getEstado()).isEqualTo(EstadoLancamentoImportado.PENDENTE);
		assertThat(importacao.getLancamentos().getFirst().getDescricaoOriginal()).isEqualTo("Texto lido");
		assertThat(importacao.getLancamentos().getFirst().getRevisoes()).singleElement().satisfies(revisao -> {
			assertThat(revisao.decisao()).isEqualTo(DecisaoRevisaoImportacao.CRIAR);
			assertThat(revisao.motivoIncerteza()).isEqualTo("Confirme os dados.");
			assertThat(revisao.justificativa()).isEqualTo("Dados conferidos no PDF");
			assertThat(revisao.anterior().descricao()).isEqualTo("Texto lido");
			assertThat(revisao.novo().descricao()).isEqualTo("Mercado");
		});
	}

	@Test
	void revisaoDefineEstadosIgnoradaEAssociada() {
		LancamentoImportado ignorado = LancamentoImportado.reconstituir(20L, 1, LocalDate.of(2026, 7, 10),
				"Ignorar", "Ignorar", new BigDecimal("10.00"), TipoTransacao.SAIDA, false, null, true,
				null, null, null);
		LancamentoImportado associado = LancamentoImportado.reconstituir(21L, 2, LocalDate.of(2026, 7, 11),
				"Associar", "Associar", new BigDecimal("20.00"), TipoTransacao.SAIDA, false, null, true,
				null, null, null);
		ImportacaoFinanceira importacao = ImportacaoFinanceira.reconstituir(1L, 2L, 3L, "extrato.pdf", "hash",
				"leitor", TipoDocumentoFinanceiro.EXTRATO_CONTA, null, null, null, null, null, null, null,
				StatusImportacaoFinanceira.PENDENTE_REVISAO, 10L, null, 0, List.of(ignorado, associado));

		importacao.revisar(2L, 10L, null, List.of(
				new ImportacaoFinanceira.RevisaoLancamento(20L, LocalDate.of(2026, 7, 10), "Ignorar",
						new BigDecimal("10.00"), TipoTransacao.SAIDA, false, null, null, null, null,
						"Não pertence ao extrato"),
				new ImportacaoFinanceira.RevisaoLancamento(21L, LocalDate.of(2026, 7, 11), "Associar",
						new BigDecimal("20.00"), TipoTransacao.SAIDA, true, null, null, 99L, null,
						"Transação já registrada")), LocalDateTime.of(2026, 9, 29, 10, 0));

		assertThat(ignorado.getEstado()).isEqualTo(EstadoLancamentoImportado.IGNORADA);
		assertThat(associado.getEstado()).isEqualTo(EstadoLancamentoImportado.ASSOCIADA);
		associado.marcarCriadaComTransacao(100L);
		assertThat(associado.getEstado()).isEqualTo(EstadoLancamentoImportado.CRIADA);
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

		importacao.revisar(2L, 10L, null, List.of(new ImportacaoFinanceira.RevisaoLancamento(20L,
				LocalDate.of(2026, 7, 10), "Mercado", new BigDecimal("80.00"), TipoTransacao.SAIDA, true, null, null,
				null, null, "Linha conferida")), LocalDateTime.of(2026, 9, 29, 10, 0));

		assertThat(importacao.getLancamentos().getFirst().isPendenteConfirmacao()).isFalse();
		assertThat(importacao.getLancamentos().get(1).isPendenteConfirmacao()).isTrue();
		assertThatThrownBy(importacao::confirmar).isInstanceOf(DomainException.class)
				.hasMessage("error.importacao.revisao.invalida");
	}

	@Test
	void exigeJustificativaParaRegistrarDecisaoHumana() {
		ImportacaoFinanceira importacao = importacao(TipoDocumentoFinanceiro.EXTRATO_CONTA);

		assertThatThrownBy(() -> importacao.revisar(2L, 10L, null,
				List.of(new ImportacaoFinanceira.RevisaoLancamento(20L, LocalDate.of(2026, 7, 10), "Mercado",
						new BigDecimal("80.00"), TipoTransacao.SAIDA, true, null, null, null, null, " ")),
				LocalDateTime.of(2026, 9, 29, 10, 0)))
				.isInstanceOf(DomainException.class)
				.hasMessage("error.importacao.justificativa.obrigatoria");
	}

	private ImportacaoFinanceira importacao(TipoDocumentoFinanceiro tipo) {
		LancamentoImportado lancamento = LancamentoImportado.reconstituir(20L, 1, null, "Texto lido", "Texto lido",
				null, null, true, "Confirme os dados.", true, null, null, null);
		return ImportacaoFinanceira.reconstituir(1L, 2L, 3L, "extrato.pdf", "hash", "generico-pdf-v1", tipo, null,
				null, null, null, null, null, null, StatusImportacaoFinanceira.PENDENTE_REVISAO, null, null, 0,
				List.of(lancamento));
	}
}
