package com.finisus.adapters.out.pdf;

import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.domain.DomainException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.function.LongSupplier;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LeitorDocumentoFinanceiroPdfAdapter implements LeitorDocumentoFinanceiroPort {
	private final List<LeitorPdfFinanceiro> leitores;
	private final int maximoPaginas;
	private final int maximoCaracteresExtraidos;
	private final Duration tempoMaximoProcessamento;
	private final long maximoMemoriaPrincipalBytes;
	private final LongSupplier nanoTime;

	@Autowired
	public LeitorDocumentoFinanceiroPdfAdapter(List<LeitorPdfFinanceiro> leitores,
			@Value("${app.importacao.pdf.max-pages:100}") int maximoPaginas,
			@Value("${app.importacao.pdf.max-extracted-characters:2000000}") int maximoCaracteresExtraidos,
			@Value("${app.importacao.pdf.max-processing-time:5s}") Duration tempoMaximoProcessamento,
			@Value("${app.importacao.pdf.max-main-memory-bytes:8388608}") long maximoMemoriaPrincipalBytes) {
		this(leitores, maximoPaginas, maximoCaracteresExtraidos, tempoMaximoProcessamento,
				maximoMemoriaPrincipalBytes, System::nanoTime);
	}

	LeitorDocumentoFinanceiroPdfAdapter(List<LeitorPdfFinanceiro> leitores, int maximoPaginas,
			int maximoCaracteresExtraidos, Duration tempoMaximoProcessamento, long maximoMemoriaPrincipalBytes,
			LongSupplier nanoTime) {
		this.leitores = leitores;
		this.maximoPaginas = maximoPaginas;
		this.maximoCaracteresExtraidos = maximoCaracteresExtraidos;
		this.tempoMaximoProcessamento = tempoMaximoProcessamento;
		this.maximoMemoriaPrincipalBytes = maximoMemoriaPrincipalBytes;
		this.nanoTime = nanoTime;
	}

	@Override
	public DocumentoLido ler(ArquivoPdf arquivo) {
		String texto = extrairTexto(arquivo.conteudo());
		return leitores.stream().filter(leitor -> leitor.suporta(arquivo, texto)).findFirst()
				.orElseThrow(() -> new DomainException("error.importacao.documento.nao.reconhecido")).ler(texto);
	}

	private String extrairTexto(byte[] conteudo) {
		long limiteTempo = nanoTime.getAsLong() + tempoMaximoProcessamento.toNanos();
		try (RandomAccessReadBuffer origem = new RandomAccessReadBuffer(conteudo);
				PDDocument documento = Loader.loadPDF(origem,
						MemoryUsageSetting.setupMixed(maximoMemoriaPrincipalBytes).streamCache)) {
			if (documento.isEncrypted()) throw new DomainException("error.importacao.arquivo.invalido");
			if (documento.getNumberOfPages() > maximoPaginas)
				throw new DomainException("error.importacao.pdf.limite.paginas");
			String texto = new ExtratorTextoLimitado(maximoCaracteresExtraidos, limiteTempo, nanoTime).getText(documento);
			if (texto == null || texto.isBlank()) throw new DomainException("error.importacao.ocr.nao.suportado");
			return texto;
		} catch (LimitePdfException exception) {
			throw new DomainException(exception.messageKey());
		} catch (DomainException exception) {
			throw exception;
		} catch (IOException exception) {
			throw new DomainException("error.importacao.arquivo.invalido");
		}
	}

	private static final class ExtratorTextoLimitado extends PDFTextStripper {
		private final int maximoCaracteres;
		private final long limiteTempo;
		private final LongSupplier nanoTime;
		private int caracteresExtraidos;

		private ExtratorTextoLimitado(int maximoCaracteres, long limiteTempo, LongSupplier nanoTime) {
			this.maximoCaracteres = maximoCaracteres;
			this.limiteTempo = limiteTempo;
			this.nanoTime = nanoTime;
		}

		@Override
		protected void startPage(PDPage page) throws IOException {
			verificarTempo();
			super.startPage(page);
		}

		@Override
		protected void writeString(String texto, List<TextPosition> posicoes) throws IOException {
			verificarTempo();
			if (texto.length() > maximoCaracteres - caracteresExtraidos)
				throw new LimitePdfException("error.importacao.pdf.limite.texto");
			caracteresExtraidos += texto.length();
			super.writeString(texto, posicoes);
		}

		private void verificarTempo() throws LimitePdfException {
			if (nanoTime.getAsLong() - limiteTempo >= 0)
				throw new LimitePdfException("error.importacao.pdf.limite.tempo");
		}
	}

	private static final class LimitePdfException extends IOException {
		private final String messageKey;

		private LimitePdfException(String messageKey) {
			this.messageKey = messageKey;
		}

		private String messageKey() {
			return messageKey;
		}
	}
}
