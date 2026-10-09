package com.finisus.adapters.out.csv;

import com.finisus.adapters.out.pdf.LeitorDocumentoFinanceiroPdfAdapter;
import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.domain.DomainException;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class LeitorDocumentoFinanceiroConteudoAdapter implements LeitorDocumentoFinanceiroPort {
	private final LeitorDocumentoFinanceiroPdfAdapter pdf;
	private final List<LeitorCsvFinanceiro> leitoresCsv;
	private final ParserCsvLimitado parserCsv = new ParserCsvLimitado();

	public LeitorDocumentoFinanceiroConteudoAdapter(LeitorDocumentoFinanceiroPdfAdapter pdf,
			List<LeitorCsvFinanceiro> leitoresCsv) {
		this.pdf = pdf;
		this.leitoresCsv = leitoresCsv;
	}

	@Override
	public DocumentoLido ler(ArquivoPdf arquivo) {
		if (ehPdf(arquivo.conteudo())) return pdf.ler(arquivo);
		TabelaCsv tabela = parserCsv.ler(arquivo.conteudo());
		List<LeitorCsvFinanceiro> compativeis = leitoresCsv.stream().filter(leitor -> leitor.suporta(arquivo, tabela)).toList();
		if (compativeis.size() != 1)
			throw new DomainException("error.importacao.documento.nao.reconhecido");
		return compativeis.getFirst().ler(tabela);
	}

	private boolean ehPdf(byte[] conteudo) {
		return conteudo.length >= 5 && conteudo[0] == '%' && conteudo[1] == 'P' && conteudo[2] == 'D'
				&& conteudo[3] == 'F' && conteudo[4] == '-';
	}
}
