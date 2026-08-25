package com.finisus.adapters.out.pdf;

import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.domain.DomainException;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
public class LeitorDocumentoFinanceiroPdfAdapter implements LeitorDocumentoFinanceiroPort {
	private final List<LeitorPdfFinanceiro> leitores;

	public LeitorDocumentoFinanceiroPdfAdapter(List<LeitorPdfFinanceiro> leitores) {
		this.leitores = leitores;
	}

	@Override
	public DocumentoLido ler(ArquivoPdf arquivo) {
		String texto = extrairTexto(arquivo.conteudo());
		return leitores.stream().filter(leitor -> leitor.suporta(arquivo, texto)).findFirst()
				.orElseThrow(() -> new DomainException("error.importacao.documento.nao.reconhecido")).ler(texto);
	}

	private String extrairTexto(byte[] conteudo) {
		try (PDDocument documento = Loader.loadPDF(conteudo)) {
			if (documento.isEncrypted()) throw new DomainException("error.importacao.arquivo.invalido");
			String texto = new PDFTextStripper().getText(documento);
			if (texto == null || texto.isBlank()) throw new DomainException("error.importacao.documento.nao.reconhecido");
			return texto;
		} catch (DomainException exception) {
			throw exception;
		} catch (IOException exception) {
			throw new DomainException("error.importacao.arquivo.invalido");
		}
	}
}
