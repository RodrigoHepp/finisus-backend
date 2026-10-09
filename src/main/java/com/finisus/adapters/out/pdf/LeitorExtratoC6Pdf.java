package com.finisus.adapters.out.pdf;

import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.domain.DomainException;
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
@Order(15)
class LeitorExtratoC6Pdf implements LeitorPdfFinanceiro {
	private static final Map<String, Integer> MESES = Map.ofEntries(Map.entry("janeiro", 1),
			Map.entry("fevereiro", 2), Map.entry("março", 3), Map.entry("abril", 4), Map.entry("maio", 5),
			Map.entry("junho", 6), Map.entry("julho", 7), Map.entry("agosto", 8), Map.entry("setembro", 9),
			Map.entry("outubro", 10), Map.entry("novembro", 11), Map.entry("dezembro", 12));
	private static final String MES = "janeiro|fevereiro|março|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro";
	private static final Pattern PERIODO = Pattern.compile("(?iu)per[ií]odo[^\\d]{0,5}(\\d{1,2})\\s+de\\s+(" + MES
			+ ")\\s+de\\s+(\\d{4})\\s+at[eé]\\s+(\\d{1,2})\\s+de\\s+(" + MES + ")\\s+de\\s+(\\d{4})");
	private static final Pattern SALDO_FINAL = Pattern.compile("(?iu)saldo\\s+do\\s+dia[^\\d]{0,5}\\d{1,2}\\s+de\\s+(?:" + MES
			+ ")\\s+de\\s+\\d{4}[^-R\\d]{0,5}(-?R\\$[^\\d]{0,5}[\\d.]+,[\\d]{2})");
	private static final Pattern LANCAMENTO = Pattern.compile(
			"(?m)^(\\d{2})/(\\d{2})\\s++\\d{2}/\\d{2}\\s++(.{1,1000}?)\\s++(-?R\\$[^\\d]{0,5}[\\d.]++,[\\d]{2})[ \\t]*+$");

	@Override
	public boolean suporta(LeitorDocumentoFinanceiroPort.ArquivoPdf arquivo, String texto) {
		String banco = (arquivo.bancoCodigo() + " " + arquivo.bancoNome()).toLowerCase(Locale.ROOT);
		String normalizado = texto.toLowerCase(Locale.ROOT);
		return (banco.contains("336") || banco.contains("c6")) && normalizado.contains("c6 bank")
				&& normalizado.contains("extrato") && PERIODO.matcher(texto).find();
	}

	@Override
	public LeitorDocumentoFinanceiroPort.DocumentoLido ler(String texto) {
		LocalDate[] periodo = periodo(texto);
		List<LancamentoImportado> lancamentos = lancamentos(texto, periodo);
		if (lancamentos.isEmpty()) throw new DomainException("error.importacao.documento.nao.reconhecido");
		return new LeitorDocumentoFinanceiroPort.DocumentoLido("c6-extrato-pdf-v1",
				TipoDocumentoFinanceiro.EXTRATO_CONTA, null, periodo[0], periodo[1], null, null, saldoFinal(texto),
				null, lancamentos);
	}

	private LocalDate[] periodo(String texto) {
		Matcher matcher = PERIODO.matcher(texto);
		if (!matcher.find()) throw new DomainException("error.importacao.documento.nao.reconhecido");
		return new LocalDate[] { data(matcher.group(1), matcher.group(2), matcher.group(3)),
				data(matcher.group(4), matcher.group(5), matcher.group(6)) };
	}

	private List<LancamentoImportado> lancamentos(String texto, LocalDate[] periodo) {
		Matcher matcher = LANCAMENTO.matcher(texto);
		List<LancamentoImportado> resultado = new ArrayList<>();
		while (matcher.find()) {
			LocalDate data = dataNoPeriodo(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), periodo);
			BigDecimal valorComSinal = monetario(matcher.group(4));
			if (valorComSinal.signum() == 0) throw new DomainException("error.importacao.arquivo.invalido");
			String descricao = normalizar(matcher.group(3));
			resultado.add(LancamentoImportado.novo(resultado.size() + 1, data, descricao, limitar(matcher.group(0)),
					valorComSinal.abs(), valorComSinal.signum() < 0 ? TipoTransacao.SAIDA : TipoTransacao.ENTRADA,
					false, null, true));
		}
		return resultado;
	}

	private LocalDate dataNoPeriodo(int dia, int mes, LocalDate[] periodo) {
		for (int ano = periodo[0].getYear(); ano <= periodo[1].getYear(); ano++) {
			try {
				LocalDate candidata = LocalDate.of(ano, mes, dia);
				if (!candidata.isBefore(periodo[0]) && !candidata.isAfter(periodo[1])) return candidata;
			} catch (java.time.DateTimeException exception) {
				throw new DomainException("error.importacao.arquivo.invalido");
			}
		}
		throw new DomainException("error.importacao.arquivo.invalido");
	}

	private LocalDate data(String dia, String mes, String ano) {
		return LocalDate.of(Integer.parseInt(ano), MESES.get(mes.toLowerCase(Locale.ROOT)), Integer.parseInt(dia));
	}

	private BigDecimal saldoFinal(String texto) {
		Matcher matcher = SALDO_FINAL.matcher(texto);
		return matcher.find() ? monetario(matcher.group(1)) : null;
	}

	private BigDecimal monetario(String valor) {
		return new BigDecimal(valor.replace('\u2212', '-').replaceAll("[^\\d,.-]", "").replace(".", "")
				.replace(',', '.'));
	}

	private String normalizar(String valor) { return valor.replaceAll("\\s+", " ").trim(); }
	private String limitar(String valor) { return valor.substring(0, Math.min(valor.length(), 1_000)); }
}
