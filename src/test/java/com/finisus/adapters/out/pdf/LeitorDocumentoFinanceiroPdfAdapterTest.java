package com.finisus.adapters.out.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import com.finisus.domain.model.TipoTransacao;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

class LeitorDocumentoFinanceiroPdfAdapterTest {
	@Test
	void identificaEFazPreviaDeFaturaSicrediSemUsarNomeDoArquivo() throws Exception {
		LeitorDocumentoFinanceiroPdfAdapter adapter = new LeitorDocumentoFinanceiroPdfAdapter(
				List.of(new LeitorFaturaSicrediPdf(), new LeitorPdfFinanceiroGenerico()), 10, 100_000,
				Duration.ofSeconds(5), 1024 * 1024, System::nanoTime);

		var documento = adapter.ler(new LeitorDocumentoFinanceiroPort.ArquivoPdf(pdf("""
				Sicredi Visa Gold
				Total fatura de julho R$ 150,00
				Vencimento 13/07/2026
				28/jun 20:54 Farmacia Central R$ 50,00
				27/jun 12:00 Mercado do Bairro R$ 100,00
				03/jun 21:26 Pagamento da fatura -R$ 150,00
				Total cartao R$ 150,00
				"""), "arquivo-sem-padrao.pdf", "748", "Sicredi"));

		assertThat(documento.tipoDocumento()).isEqualTo(TipoDocumentoFinanceiro.FATURA_CARTAO);
		assertThat(documento.leitor()).isEqualTo("sicredi-fatura-v1");
		assertThat(documento.dataVencimento()).hasToString("2026-07-13");
		assertThat(documento.lancamentos()).hasSize(3);
		assertThat(documento.lancamentos().getFirst().getDescricao()).contains("Farmacia Central");
		assertThat(documento.lancamentos().get(2).getValor()).isEqualByComparingTo("150.00");
		assertThat(documento.lancamentos().get(2).getTipo()).isEqualTo(TipoTransacao.ENTRADA);
	}

	@Test
	void recusaPdfSemCamadaDeTextoComErroEspecificoDeOcr() throws Exception {
		LeitorDocumentoFinanceiroPdfAdapter adapter = new LeitorDocumentoFinanceiroPdfAdapter(
				List.of(new LeitorPdfFinanceiroGenerico()), 10, 100_000, Duration.ofSeconds(5), 1024 * 1024,
				System::nanoTime);

		assertThatThrownBy(() -> adapter.ler(new LeitorDocumentoFinanceiroPort.ArquivoPdf(
				pdfSemTexto(), "documento-digitalizado.pdf", "748", "Sicredi")))
				.isInstanceOf(DomainException.class)
				.extracting("messageKey")
				.isEqualTo("error.importacao.ocr.nao.suportado");
	}

	@Test
	void preservaLancamentoSemExigirItemDeCatalogo() throws Exception {
		LeitorDocumentoFinanceiroPdfAdapter adapter = new LeitorDocumentoFinanceiroPdfAdapter(
				List.of(new LeitorPdfFinanceiroGenerico()), 10, 100_000, Duration.ofSeconds(5), 1024 * 1024,
				System::nanoTime);

		var documento = adapter.ler(new LeitorDocumentoFinanceiroPort.ArquivoPdf(pdf("""
				Extrato
				01/09/2026 Mercado do Bairro -R$ 10,00
				"""), "extrato.pdf", "748", "Sicredi"));

		assertThat(documento.lancamentos()).singleElement().satisfies(lancamento -> {
			assertThat(lancamento.getDescricao()).isEqualTo("Mercado do Bairro");
			assertThat(lancamento.getItemId()).isNull();
		});
	}

	@Test
	void recusaPdfAcimaDoLimiteDePaginas() throws Exception {
		LeitorDocumentoFinanceiroPdfAdapter adapter = new LeitorDocumentoFinanceiroPdfAdapter(
				List.of(new LeitorPdfFinanceiroGenerico()), 1, 100_000, Duration.ofSeconds(5), 1024 * 1024,
				System::nanoTime);

		assertThatThrownBy(() -> adapter.ler(new LeitorDocumentoFinanceiroPort.ArquivoPdf(
				pdfComPaginas(2), "extrato.pdf", "748", "Sicredi")))
				.isInstanceOf(DomainException.class).extracting("messageKey")
				.isEqualTo("error.importacao.pdf.limite.paginas");
	}

	@Test
	void recusaTextoExtraidoAcimaDoLimite() throws Exception {
		LeitorDocumentoFinanceiroPdfAdapter adapter = new LeitorDocumentoFinanceiroPdfAdapter(
				List.of(new LeitorPdfFinanceiroGenerico()), 10, 5, Duration.ofSeconds(5), 1024 * 1024,
				System::nanoTime);

		assertThatThrownBy(() -> adapter.ler(new LeitorDocumentoFinanceiroPort.ArquivoPdf(
				pdf("Extrato com texto acima do limite"), "extrato.pdf", "748", "Sicredi")))
				.isInstanceOf(DomainException.class).extracting("messageKey")
				.isEqualTo("error.importacao.pdf.limite.texto");
	}

	@Test
	void interrompeExtracaoAoUltrapassarTempoConfigurado() throws Exception {
		AtomicInteger leiturasTempo = new AtomicInteger();
		LeitorDocumentoFinanceiroPdfAdapter adapter = new LeitorDocumentoFinanceiroPdfAdapter(
				List.of(new LeitorPdfFinanceiroGenerico()), 10, 100_000, Duration.ofNanos(1), 1024 * 1024,
				() -> leiturasTempo.getAndIncrement() == 0 ? 0 : 2);

		assertThatThrownBy(() -> adapter.ler(new LeitorDocumentoFinanceiroPort.ArquivoPdf(
				pdf("Extrato"), "extrato.pdf", "748", "Sicredi")))
				.isInstanceOf(DomainException.class).extracting("messageKey")
				.isEqualTo("error.importacao.pdf.limite.tempo");
	}

	private byte[] pdf(String texto) throws Exception {
		try (PDDocument documento = new PDDocument(); ByteArrayOutputStream destino = new ByteArrayOutputStream()) {
			documento.addPage(new PDPage());
			try (PDPageContentStream conteudo = new PDPageContentStream(documento, documento.getPage(0))) {
				conteudo.beginText();
				conteudo.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				conteudo.setLeading(16);
				conteudo.newLineAtOffset(50, 700);
				for (String linha : texto.strip().split("\\R")) {
					conteudo.showText(linha);
					conteudo.newLine();
				}
				conteudo.endText();
			}
			documento.save(destino);
			return destino.toByteArray();
		}
	}

	private byte[] pdfSemTexto() throws Exception {
		try (PDDocument documento = new PDDocument(); ByteArrayOutputStream destino = new ByteArrayOutputStream()) {
			documento.addPage(new PDPage());
			documento.save(destino);
			return destino.toByteArray();
		}
	}

	private byte[] pdfComPaginas(int quantidade) throws Exception {
		try (PDDocument documento = new PDDocument(); ByteArrayOutputStream destino = new ByteArrayOutputStream()) {
			for (int pagina = 0; pagina < quantidade; pagina++) documento.addPage(new PDPage());
			documento.save(destino);
			return destino.toByteArray();
		}
	}
}
