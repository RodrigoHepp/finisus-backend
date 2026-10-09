package com.finisus.adapters.out.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import com.finisus.domain.model.TipoTransacao;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LeitorExtratoC6PdfTest {
	private final LeitorExtratoC6Pdf leitor = new LeitorExtratoC6Pdf();

	@Test
	void reconheceExtratoC6ELeEntradasSaidasPeriodoESaldo() {
		String texto = """
				C6 BANK
				Extrato da conta
				Período: 28 de setembro de 2026 até 04 de outubro de 2026
				28/09 28/09 Transferência recebida R$ 1.500,00
				02/10 02/10 Pagamento de conta -R$ 250,75
				Saldo do dia 04 de outubro de 2026 R$ 1.249,25
				""";
		var arquivo = arquivo("336", "Banco C6");

		assertThat(leitor.suporta(arquivo, texto)).isTrue();
		var documento = leitor.ler(texto);

		assertThat(documento.leitor()).isEqualTo("c6-extrato-pdf-v1");
		assertThat(documento.tipoDocumento()).isEqualTo(TipoDocumentoFinanceiro.EXTRATO_CONTA);
		assertThat(documento.periodoInicio()).hasToString("2026-09-28");
		assertThat(documento.periodoFim()).hasToString("2026-10-04");
		assertThat(documento.saldoFinal()).isEqualByComparingTo("1249.25");
		assertThat(documento.lancamentos()).extracting(l -> l.getTipo())
				.containsExactly(TipoTransacao.ENTRADA, TipoTransacao.SAIDA);
		assertThat(documento.lancamentos().get(1).getValor()).isEqualByComparingTo("250.75");
	}

	@Test
	void naoReconheceDocumentoSemIdentidadeDoBancoOuPeriodo() {
		String texto = "Extrato\n01/10 01/10 Movimento R$ 10,00";

		assertThat(leitor.suporta(arquivo("001", "Outro banco"), texto)).isFalse();
	}

	@Test
	void recusaDataDeLancamentoForaDoPeriodo() {
		String texto = """
				C6 BANK
				Extrato
				Período: 01 de outubro de 2026 até 05 de outubro de 2026
				30/09 30/09 Movimento R$ 10,00
				""";

		assertThatThrownBy(() -> leitor.ler(texto)).isInstanceOf(DomainException.class)
				.extracting("messageKey").isEqualTo("error.importacao.arquivo.invalido");
	}

	private LeitorDocumentoFinanceiroPort.ArquivoPdf arquivo(String codigo, String nome) {
		return new LeitorDocumentoFinanceiroPort.ArquivoPdf("%PDF".getBytes(StandardCharsets.US_ASCII),
				"extrato.pdf", codigo, nome);
	}
}
