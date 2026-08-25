package com.finisus.adapters.out.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import com.finisus.domain.model.TipoTransacao;
import org.junit.jupiter.api.Test;

class LeitorNubankPdfTest {
	private final LeitorNubankPdf leitor = new LeitorNubankPdf();

	@Test
	void leFaturaNubankComComprasEPagamentos() {
		var documento = leitor.ler("""
				Esta é a sua fatura de agosto
				Data de vencimento: 24 AGO 2026
				Total de compras de todos os cartões, 15 JUL a 15 AGO R$ 219,70
				Total a pagar R$ 0,00
				15 JUL •••• 5911 Loja Exemplo - Parcela 2/4 R$ 219,70
				12 AGO Pagamento em 12 AGO −R$ 219,70
				""");

		assertThat(documento.tipoDocumento()).isEqualTo(TipoDocumentoFinanceiro.FATURA_CARTAO);
		assertThat(documento.leitor()).isEqualTo("nubank-fatura-v1");
		assertThat(documento.dataVencimento()).hasToString("2026-08-24");
		assertThat(documento.valorTotal()).isEqualByComparingTo("219.70");
		assertThat(documento.lancamentos()).hasSize(2);
		assertThat(documento.lancamentos().getFirst().getTipo()).isEqualTo(TipoTransacao.SAIDA);
		assertThat(documento.lancamentos().get(1).getTipo()).isEqualTo(TipoTransacao.ENTRADA);
		assertThat(documento.lancamentos().get(1).isImportar()).isFalse();
	}

	@Test
	void leExtratoNubankComSaldosEMovimentacoes() {
		var documento = leitor.ler("""
				Conta
				97674443-9
				01 DE AGOSTO DE 2026 a 23 DE AGOSTO DE 2026 VALORES EM R$
				Saldo inicial 626,98
				Saldo final do período
				R$ 258,40
				Movimentações
				04 AGO 2026 Total de saídas - 61,28
				Transferência enviada pelo Pix Loja Exemplo 61,28
				06 AGO 2026 Total de entradas + 50,00
				Transferência recebida pelo Pix Pessoa Exemplo 50,00
				17 AGO 2026 Total de saídas - 39,92
				Débito em conta 39,92
				""");

		assertThat(documento.tipoDocumento()).isEqualTo(TipoDocumentoFinanceiro.EXTRATO_CONTA);
		assertThat(documento.leitor()).isEqualTo("nubank-extrato-v1");
		assertThat(documento.identificadorOrigem()).isEqualTo("conta-97674443-9");
		assertThat(documento.periodoInicio()).hasToString("2026-08-01");
		assertThat(documento.periodoFim()).hasToString("2026-08-23");
		assertThat(documento.saldoInicial()).isEqualByComparingTo("626.98");
		assertThat(documento.saldoFinal()).isEqualByComparingTo("258.40");
		assertThat(documento.lancamentos()).hasSize(3);
		assertThat(documento.lancamentos().getFirst().getTipo()).isEqualTo(TipoTransacao.SAIDA);
		assertThat(documento.lancamentos().get(1).getTipo()).isEqualTo(TipoTransacao.ENTRADA);
	}

	@Test
	void suportaApenasDocumentosNubankSelecionados() {
		var nubank = new LeitorDocumentoFinanceiroPort.ArquivoPdf(new byte[0], "arquivo.pdf", "260", "Nubank");
		var sicredi = new LeitorDocumentoFinanceiroPort.ArquivoPdf(new byte[0], "arquivo.pdf", "748", "Sicredi");

		assertThat(leitor.suporta(nubank, "Esta é a sua fatura. Transações de agosto")).isTrue();
		assertThat(leitor.suporta(sicredi, "Esta é a sua fatura. Transações de agosto")).isFalse();
	}
}
