package com.finisus.adapters.out.csv;

import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.domain.model.LancamentoImportado;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import com.finisus.domain.model.TipoTransacao;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
class LeitorFaturaCsv implements LeitorCsvFinanceiro {
	@Override
	public boolean suporta(LeitorDocumentoFinanceiroPort.ArquivoPdf arquivo, TabelaCsv tabela) {
		return tabela.possuiCabecalhos("Data de Compra", "Descrição", "Valor (em R$)");
	}

	@Override
	public LeitorDocumentoFinanceiroPort.DocumentoLido ler(TabelaCsv tabela) {
		List<LancamentoImportado> lancamentos = new ArrayList<>();
		for (TabelaCsv.Linha linha : tabela.linhas()) {
			LocalDate data = ConversoesCsv.data(linha.valor("Data de Compra"));
			String descricao = ConversoesCsv.descricao(linha.valor("Descrição"));
			var valorComSinal = ConversoesCsv.monetario(linha.valor("Valor (em R$)"));
			if (valorComSinal.signum() == 0) throw ConversoesCsv.invalido();
			TipoTransacao tipo = valorComSinal.signum() < 0 ? TipoTransacao.ENTRADA : TipoTransacao.SAIDA;
			boolean pagamento = descricao.toLowerCase(Locale.ROOT).contains("pagamento");
			lancamentos.add(LancamentoImportado.novo(lancamentos.size() + 1, data, descricao, linha.original(),
					valorComSinal.abs(), tipo, false, null, !pagamento));
		}
		return new LeitorDocumentoFinanceiroPort.DocumentoLido("fatura-csv-v1",
				TipoDocumentoFinanceiro.FATURA_CARTAO, null, primeira(lancamentos), ultima(lancamentos), null,
				null, null, null, lancamentos);
	}

	private LocalDate primeira(List<LancamentoImportado> itens) {
		return itens.stream().map(LancamentoImportado::getData).min(LocalDate::compareTo).orElse(null);
	}

	private LocalDate ultima(List<LancamentoImportado> itens) {
		return itens.stream().map(LancamentoImportado::getData).max(LocalDate::compareTo).orElse(null);
	}
}
