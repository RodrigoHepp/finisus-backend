package com.finisus.adapters.out.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finisus.adapters.out.pdf.LeitorDocumentoFinanceiroPdfAdapter;
import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import com.finisus.domain.model.TipoTransacao;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class LeitorDocumentoFinanceiroConteudoAdapterTest {
	@Test
	void leFaturaCsvComValoresBrasileirosAspasEDelimitadorNaDescricao() {
		var adapter = adapter();
		var arquivo = arquivo("Data de Compra;Descrição;Valor (em R$)\n"
				+ "01/10/2026;\"Mercado; compra mensal\";1.234,56\n"
				+ "02/10/2026;\"Estorno \"\"Loja\"\"\";-34,56\n");

		var documento = adapter.ler(arquivo);

		assertThat(documento.leitor()).isEqualTo("fatura-csv-v1");
		assertThat(documento.tipoDocumento()).isEqualTo(TipoDocumentoFinanceiro.FATURA_CARTAO);
		assertThat(documento.periodoInicio()).hasToString("2026-10-01");
		assertThat(documento.periodoFim()).hasToString("2026-10-02");
		assertThat(documento.valorTotal()).isNull();
		assertThat(documento.lancamentos()).satisfiesExactly(
				lancamento -> {
					assertThat(lancamento.getDescricao()).isEqualTo("Mercado; compra mensal");
					assertThat(lancamento.getValor()).isEqualByComparingTo("1234.56");
					assertThat(lancamento.getTipo()).isEqualTo(TipoTransacao.SAIDA);
				},
				lancamento -> {
					assertThat(lancamento.getDescricao()).isEqualTo("Estorno \"Loja\"");
					assertThat(lancamento.getValor()).isEqualByComparingTo("34.56");
					assertThat(lancamento.getTipo()).isEqualTo(TipoTransacao.ENTRADA);
				});
	}

	@Test
	void leExtratoSicrediComBomCreditoDebitoESaldoFinal() {
		var adapter = adapter();
		String csv = "\ufeffData;Descricao;CodTransacao;Identificador;Tipo;Valor;Saldo\r\n"
				+ "03/10/2026;Crédito recebido;10;A1;CREDITO;+2.000,00;2.500,00\r\n"
				+ "04/10/2026;Débito realizado;20;B2;DEBITO;-150,25;2.349,75\r\n";

		var documento = adapter.ler(arquivo(csv));

		assertThat(documento.leitor()).isEqualTo("sicredi-extrato-csv-v1");
		assertThat(documento.tipoDocumento()).isEqualTo(TipoDocumentoFinanceiro.EXTRATO_CONTA);
		assertThat(documento.saldoFinal()).isEqualByComparingTo("2349.75");
		assertThat(documento.lancamentos()).extracting(l -> l.getTipo())
				.containsExactly(TipoTransacao.ENTRADA, TipoTransacao.SAIDA);
		assertThat(documento.lancamentos()).extracting(l -> l.getValor().toPlainString())
				.containsExactly("2000.00", "150.25");
	}

	@Test
	void recusaSinalIncompativelComTipoDoExtrato() {
		var adapter = adapter();

		assertThatThrownBy(() -> adapter.ler(arquivo("""
				Data;Descricao;CodTransacao;Identificador;Tipo;Valor;Saldo
				03/10/2026;Lançamento;10;A1;CREDITO;-10,00;90,00
				"""))).isInstanceOf(DomainException.class)
				.extracting("messageKey").isEqualTo("error.importacao.arquivo.invalido");
	}

	@Test
	void recusaFormatoDesconhecidoEMalformado() {
		var adapter = adapter();

		assertThatThrownBy(() -> adapter.ler(arquivo("Data;Texto\n01/10/2026;Linha\n")))
				.isInstanceOf(DomainException.class).extracting("messageKey")
				.isEqualTo("error.importacao.documento.nao.reconhecido");
		assertThatThrownBy(() -> adapter.ler(arquivo("Data;Descrição;Valor (em R$)\n01/10/2026;\"aberto;10,00\n")))
				.isInstanceOf(DomainException.class).extracting("messageKey")
				.isEqualTo("error.importacao.arquivo.invalido");
	}

	@Test
	void recusaCampoAcimaDoLimite() {
		var adapter = adapter();
		String descricao = "x".repeat(4_097);

		assertThatThrownBy(() -> adapter.ler(arquivo(
				"Data de Compra;Descrição;Valor (em R$)\n01/10/2026;" + descricao + ";10,00\n")))
				.isInstanceOf(DomainException.class).extracting("messageKey")
				.isEqualTo("error.importacao.arquivo.invalido");
	}

	@Test
	void mantemPdfDelegadoAoLeitorExistente() {
		LeitorDocumentoFinanceiroPdfAdapter pdf = mock(LeitorDocumentoFinanceiroPdfAdapter.class);
		var adapter = new LeitorDocumentoFinanceiroConteudoAdapter(pdf,
				List.of(new LeitorFaturaCsv(), new LeitorExtratoSicrediCsv()));
		var arquivo = new LeitorDocumentoFinanceiroPort.ArquivoPdf("%PDF-sintetico".getBytes(StandardCharsets.US_ASCII),
				"arquivo.pdf", "748", "Sicredi");
		var esperado = new LeitorDocumentoFinanceiroPort.DocumentoLido("pdf", TipoDocumentoFinanceiro.EXTRATO_CONTA,
				null, null, null, null, null, null, null, List.of());
		when(pdf.ler(arquivo)).thenReturn(esperado);

		assertThat(adapter.ler(arquivo)).isSameAs(esperado);
		verify(pdf).ler(arquivo);
	}

	private LeitorDocumentoFinanceiroConteudoAdapter adapter() {
		return new LeitorDocumentoFinanceiroConteudoAdapter(mock(LeitorDocumentoFinanceiroPdfAdapter.class),
				List.of(new LeitorFaturaCsv(), new LeitorExtratoSicrediCsv()));
	}

	private LeitorDocumentoFinanceiroPort.ArquivoPdf arquivo(String csv) {
		return new LeitorDocumentoFinanceiroPort.ArquivoPdf(csv.getBytes(StandardCharsets.UTF_8), "arquivo.csv",
				"748", "Sicredi");
	}
}
