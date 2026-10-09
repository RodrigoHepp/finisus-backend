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
@Order(20)
class LeitorNubankPdf implements LeitorPdfFinanceiro {
	private static final Map<String, Integer> MESES = Map.ofEntries(Map.entry("jan", 1), Map.entry("fev", 2),
			Map.entry("mar", 3), Map.entry("abr", 4), Map.entry("mai", 5), Map.entry("jun", 6), Map.entry("jul", 7),
			Map.entry("ago", 8), Map.entry("set", 9), Map.entry("out", 10), Map.entry("nov", 11), Map.entry("dez", 12));
	private static final String MES = "jan(?:eiro)?|fev(?:ereiro)?|mar(?:ço)?|abr(?:il)?|mai(?:o)?|jun(?:ho)?|jul(?:ho)?|ago(?:sto)?|set(?:embro)?|out(?:ubro)?|nov(?:embro)?|dez(?:embro)?";
	private static final Pattern FATURA_VENCIMENTO = Pattern.compile(
			"(?iu)Data de vencimento:\\s*(\\d{1,2})\\s+(" + MES + ")\\s+(\\d{4})");
	private static final Pattern FATURA_TOTAL_COMPRAS = Pattern.compile(
			"(?iu)Total de compras de todos os cart[õo]es[^\\r\\n]{0,1000}?R\\$\\s*+([\\d.]++,[\\d]{2})");
	private static final Pattern FATURA_TOTAL_A_PAGAR = Pattern.compile("(?iu)Total a pagar\\s+R\\$\\s*([\\d.]+,[\\d]{2})");
	private static final Pattern FATURA_LANCAMENTO = Pattern.compile(
			"(?imu)^(\\d{1,2})\\s++(" + MES + ")\\s++(.{1,1000}?)\\s++([−-]?R\\$\\s*+[\\d.]++,[\\d]{2})[ \\t]*+$");
	private static final Pattern EXTRATO_PERIODO = Pattern.compile("(?iu)(\\d{1,2}) DE (" + MES + ") DE (\\d{4})\\s+A\\s+"
			+ "(\\d{1,2}) DE (" + MES + ") DE (\\d{4})");
	private static final Pattern EXTRATO_CONTA = Pattern.compile("(?iu)Conta\\s+(\\d[\\d-]{4,})");
	private static final Pattern EXTRATO_SALDO_INICIAL = Pattern.compile("(?iu)Saldo inicial\\s+([\\d.]+,[\\d]{2})");
	private static final Pattern EXTRATO_SALDO_FINAL = Pattern.compile("(?iu)Saldo final do per[ií]odo\\s+(?:R\\$\\s*)?([\\d.]+,[\\d]{2})");
	private static final Pattern EXTRATO_DIA = Pattern.compile("(?iu)^(\\d{1,2})\\s+(" + MES + ")\\s+(\\d{4})\\s+Total de "
			+ "(entradas|sa[ií]das)\\s+[+-]\\s*([\\d.]+,[\\d]{2})$");
	private static final Pattern VALOR_NO_FIM = Pattern.compile("(.{1,1000}?)\\s++([\\d.]++,[\\d]{2})$");

	@Override
	public boolean suporta(LeitorDocumentoFinanceiroPort.ArquivoPdf arquivo, String texto) {
		String banco = (arquivo.bancoCodigo() + " " + arquivo.bancoNome()).toLowerCase(Locale.ROOT);
		String normalizado = texto.toLowerCase(Locale.ROOT);
		return (banco.contains("260") || banco.contains("nubank"))
				&& (normalizado.contains("sua fatura") || normalizado.contains("transações de")
						|| normalizado.contains("movimentações"));
	}

	@Override
	public LeitorDocumentoFinanceiroPort.DocumentoLido ler(String texto) {
		return ehFatura(texto) ? lerFatura(texto) : lerExtrato(texto);
	}

	private LeitorDocumentoFinanceiroPort.DocumentoLido lerFatura(String texto) {
		LocalDate vencimento = data(FATURA_VENCIMENTO.matcher(texto));
		List<LancamentoImportado> lancamentos = lancamentosFatura(texto, vencimento);
		return new LeitorDocumentoFinanceiroPort.DocumentoLido("nubank-fatura-v1", TipoDocumentoFinanceiro.FATURA_CARTAO,
				null, primeiraData(lancamentos), ultimaData(lancamentos), vencimento, null, null, valorFatura(texto),
				lancamentos);
	}

	private LeitorDocumentoFinanceiroPort.DocumentoLido lerExtrato(String texto) {
		LocalDate[] periodo = periodo(EXTRATO_PERIODO.matcher(texto));
		List<LancamentoImportado> lancamentos = lancamentosExtrato(texto);
		return new LeitorDocumentoFinanceiroPort.DocumentoLido("nubank-extrato-v1", TipoDocumentoFinanceiro.EXTRATO_CONTA,
				identificador(EXTRATO_CONTA.matcher(texto)), periodo[0], periodo[1], null, valor(EXTRATO_SALDO_INICIAL.matcher(texto)),
				valor(EXTRATO_SALDO_FINAL.matcher(texto)), null, lancamentos);
	}

	private List<LancamentoImportado> lancamentosFatura(String texto, LocalDate vencimento) {
		Matcher matcher = FATURA_LANCAMENTO.matcher(texto);
		List<LancamentoImportado> resultado = new ArrayList<>();
		while (matcher.find()) {
			BigDecimal valor = monetario(matcher.group(4));
			String descricao = normalizar(matcher.group(3));
			boolean pagamento = descricao.toLowerCase(Locale.ROOT).contains("pagamento");
			resultado.add(LancamentoImportado.novo(resultado.size() + 1, dataFatura(matcher.group(1), matcher.group(2), vencimento),
				descricao, limitar(matcher.group(0)), valor.abs(), valor.signum() < 0 ? TipoTransacao.ENTRADA : TipoTransacao.SAIDA,
				false, null, !pagamento));
		}
		return resultado;
	}

	private List<LancamentoImportado> lancamentosExtrato(String texto) {
		List<LancamentoImportado> resultado = new ArrayList<>();
		LocalDate dataAtual = null;
		TipoTransacao tipoAtual = null;
		for (String linha : texto.lines().map(String::trim).filter(linha -> !linha.isEmpty()).toList()) {
			Matcher dia = EXTRATO_DIA.matcher(linha);
			if (dia.matches()) {
				dataAtual = data(dia.group(1), dia.group(2), dia.group(3));
				tipoAtual = dia.group(4).toLowerCase(Locale.ROOT).startsWith("entrada") ? TipoTransacao.ENTRADA : TipoTransacao.SAIDA;
				continue;
			}
			Matcher valor = VALOR_NO_FIM.matcher(linha);
			if (dataAtual == null || tipoAtual == null || !valor.matches() || linha.toLowerCase(Locale.ROOT).startsWith("total de")) continue;
			String descricao = normalizar(valor.group(1));
			if (!ehMovimentacao(descricao)) continue;
			resultado.add(LancamentoImportado.novo(resultado.size() + 1, dataAtual, descricao, limitar(linha),
				monetario(valor.group(2)), tipoAtual, true,
				"Confirme a data, a descrição, o tipo e a classificação do lançamento.", true));
		}
		return resultado;
	}

	private boolean ehFatura(String texto) { return texto.toLowerCase(Locale.ROOT).contains("fatura"); }
	private boolean ehMovimentacao(String descricao) {
		String normalizado = descricao.toLowerCase(Locale.ROOT);
		return normalizado.startsWith("transferência") || normalizado.startsWith("débito em conta")
				|| normalizado.startsWith("compra no débito") || normalizado.startsWith("pagamento");
	}
	private LocalDate data(Matcher matcher) { return matcher.find() ? data(matcher.group(1), matcher.group(2), matcher.group(3)) : null; }
	private LocalDate data(String dia, String mes, String ano) { return LocalDate.of(Integer.parseInt(ano), numeroMes(mes), Integer.parseInt(dia)); }
	private LocalDate dataFatura(String dia, String mes, LocalDate vencimento) {
		int ano = vencimento == null ? LocalDate.now().getYear() : vencimento.getYear();
		int mesNumero = numeroMes(mes);
		if (vencimento != null && mesNumero > vencimento.getMonthValue()) ano--;
		return LocalDate.of(ano, mesNumero, Integer.parseInt(dia));
	}
	private LocalDate[] periodo(Matcher matcher) {
		return matcher.find() ? new LocalDate[] { data(matcher.group(1), matcher.group(2), matcher.group(3)),
				data(matcher.group(4), matcher.group(5), matcher.group(6)) } : new LocalDate[] { null, null };
	}
	private String identificador(Matcher matcher) { return matcher.find() ? "conta-" + matcher.group(1) : null; }
	private int numeroMes(String mes) { return MESES.get(mes.substring(0, 3).toLowerCase(Locale.ROOT)); }
	private BigDecimal valorFatura(String texto) {
		BigDecimal totalCompras = valor(FATURA_TOTAL_COMPRAS.matcher(texto));
		return totalCompras != null ? totalCompras : valor(FATURA_TOTAL_A_PAGAR.matcher(texto));
	}
	private BigDecimal valor(Matcher matcher) { return matcher.find() ? monetario(matcher.group(1)) : null; }
	private BigDecimal monetario(String valor) {
		return new BigDecimal(valor.replace('\u2212', '-').replaceAll("[^\\d,.-]", "").replace(".", "").replace(',', '.'));
	}
	private LocalDate primeiraData(List<LancamentoImportado> lancamentos) { return lancamentos.stream().map(LancamentoImportado::getData).min(LocalDate::compareTo).orElse(null); }
	private LocalDate ultimaData(List<LancamentoImportado> lancamentos) { return lancamentos.stream().map(LancamentoImportado::getData).max(LocalDate::compareTo).orElse(null); }
	private String normalizar(String valor) { return valor.replaceAll("\\s+", " ").trim(); }
	private String limitar(String valor) { return valor.substring(0, Math.min(valor.length(), 1000)); }
}
