package com.finisus.adapters.out.pdf;

import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.domain.model.LancamentoImportado;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import com.finisus.domain.model.TipoTransacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
class LeitorFaturaSicrediPdf implements LeitorPdfFinanceiro {
	private static final Map<String, Integer> MESES = Map.ofEntries(Map.entry("jan", 1), Map.entry("fev", 2),
			Map.entry("mar", 3), Map.entry("abr", 4), Map.entry("mai", 5), Map.entry("jun", 6), Map.entry("jul", 7),
			Map.entry("ago", 8), Map.entry("set", 9), Map.entry("out", 10), Map.entry("nov", 11), Map.entry("dez", 12));
	private static final Pattern VENCIMENTO = Pattern.compile("(?i)Vencimento\\s+(\\d{1,2})/(\\d{2})/(\\d{4})");
	private static final Pattern TOTAL = Pattern.compile("(?i)Total\\s+fatura(?:\\s+de\\s+\\p{L}+)?\\s+R\\$\\s*([\\d.]+,[\\d]{2})");
	private static final Pattern CARTAO_FINAL = Pattern.compile("(?i)(?:cart[aã]o|visa|mastercard).*?final\\s+(\\d{4})");
	private static final Pattern LANCAMENTO = Pattern.compile(
			"(?ms)^(\\d{1,2})/(jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez)(?:\\s+\\d{2}:\\d{2})?\\s*(.*?)\\s*(-?R\\$\\s*[\\d.]+,[\\d]{2})(?=\\s*(?:\\d{1,2}/(?:jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez)|Total\\s+cart|Legenda:|$))");

	@Override
	public boolean suporta(LeitorDocumentoFinanceiroPort.ArquivoPdf arquivo, String texto) {
		String banco = (arquivo.bancoCodigo() + " " + arquivo.bancoNome()).toLowerCase(Locale.ROOT);
		String normalizado = texto.toLowerCase(Locale.ROOT);
		return (banco.contains("748") || banco.contains("sicredi")) && normalizado.contains("sicredi")
				&& normalizado.contains("fatura");
	}

	@Override
	public LeitorDocumentoFinanceiroPort.DocumentoLido ler(String texto) {
		LocalDate vencimento = dataVencimento(texto);
		List<LancamentoImportado> lancamentos = lancamentos(texto, vencimento == null ? LocalDate.now().getYear() : vencimento.getYear());
		return new LeitorDocumentoFinanceiroPort.DocumentoLido("sicredi-fatura-v1", TipoDocumentoFinanceiro.FATURA_CARTAO,
				cartaoFinal(texto), lancamentos.stream().map(LancamentoImportado::getData).min(LocalDate::compareTo).orElse(null),
				lancamentos.stream().map(LancamentoImportado::getData).max(LocalDate::compareTo).orElse(null), vencimento, null, null,
				valor(TOTAL.matcher(texto)), lancamentos);
	}

	private List<LancamentoImportado> lancamentos(String texto, int ano) {
		Matcher matcher = LANCAMENTO.matcher(texto);
		List<LancamentoImportado> resultado = new ArrayList<>();
		while (matcher.find()) {
			String descricao = normalizar(matcher.group(3));
			BigDecimal valor = monetario(matcher.group(4));
			boolean pagamento = descricao.toLowerCase(Locale.ROOT).contains("pagamento");
			resultado.add(LancamentoImportado.novo(resultado.size() + 1,
					LocalDate.of(ano, MESES.get(matcher.group(2).toLowerCase(Locale.ROOT)), Integer.parseInt(matcher.group(1))),
					descricao, limitar(matcher.group(0)), valor.abs(), valor.signum() < 0 ? TipoTransacao.ENTRADA : TipoTransacao.SAIDA,
					false, null, !pagamento));

		}
		return resultado;
	}

	private LocalDate dataVencimento(String texto) {
		Matcher matcher = VENCIMENTO.matcher(texto);
		return matcher.find() ? LocalDate.of(Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(2)),
				Integer.parseInt(matcher.group(1))) : null;
	}

	private String cartaoFinal(String texto) { Matcher matcher = CARTAO_FINAL.matcher(texto); return matcher.find() ? "cartao-final-" + matcher.group(1) : null; }

	private BigDecimal valor(Matcher matcher) { return matcher.find() ? monetario(matcher.group(1)) : null; }
	private BigDecimal monetario(String valor) {
		String normalizado = valor.replace('\u2212', '-').replaceAll("[^\\d,.-]", "").replace(".", "").replace(',', '.');
		return new BigDecimal(normalizado);
	}
	private String normalizar(String valor) { return valor.replaceAll("\\s+", " ").trim(); }
	private String limitar(String valor) { return valor.substring(0, Math.min(valor.length(), 1000)); }
}
