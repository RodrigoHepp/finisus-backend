package com.finisus.adapters.out.csv;

import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.domain.model.LancamentoImportado;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import com.finisus.domain.model.TipoTransacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
class LeitorExtratoSicrediCsv implements LeitorCsvFinanceiro {
	@Override
	public boolean suporta(LeitorDocumentoFinanceiroPort.ArquivoPdf arquivo, TabelaCsv tabela) {
		String banco = (arquivo.bancoCodigo() + " " + arquivo.bancoNome()).toLowerCase(Locale.ROOT);
		return (banco.contains("748") || banco.contains("sicredi"))
				&& tabela.possuiCabecalhos("Data", "Descricao", "CodTransacao", "Identificador", "Tipo", "Valor", "Saldo");
	}

	@Override
	public LeitorDocumentoFinanceiroPort.DocumentoLido ler(TabelaCsv tabela) {
		List<LancamentoImportado> lancamentos = new ArrayList<>();
		BigDecimal saldoFinal = null;
		for (TabelaCsv.Linha linha : tabela.linhas()) {
			LocalDate data = ConversoesCsv.data(linha.valor("Data"));
			String descricao = ConversoesCsv.descricao(linha.valor("Descricao"));
			BigDecimal valorComSinal = ConversoesCsv.monetario(linha.valor("Valor"));
			TipoTransacao tipo = tipo(linha.valor("Tipo"), valorComSinal);
			lancamentos.add(LancamentoImportado.novo(lancamentos.size() + 1, data, descricao, linha.original(),
					valorComSinal.abs(), tipo, false, null, true));
			saldoFinal = ConversoesCsv.monetario(linha.valor("Saldo"));
		}
		return new LeitorDocumentoFinanceiroPort.DocumentoLido("sicredi-extrato-csv-v1",
				TipoDocumentoFinanceiro.EXTRATO_CONTA, null, primeira(lancamentos), ultima(lancamentos), null,
				null, saldoFinal, null, lancamentos);
	}

	private TipoTransacao tipo(String valor, BigDecimal monetario) {
		String normalizado = valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
		if ("CREDITO".equals(normalizado) && monetario.signum() > 0) return TipoTransacao.ENTRADA;
		if ("DEBITO".equals(normalizado) && monetario.signum() < 0) return TipoTransacao.SAIDA;
		throw ConversoesCsv.invalido();
	}

	private LocalDate primeira(List<LancamentoImportado> itens) {
		return itens.stream().map(LancamentoImportado::getData).min(LocalDate::compareTo).orElse(null);
	}

	private LocalDate ultima(List<LancamentoImportado> itens) {
		return itens.stream().map(LancamentoImportado::getData).max(LocalDate::compareTo).orElse(null);
	}
}
