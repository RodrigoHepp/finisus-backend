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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
class LeitorPdfFinanceiroGenerico implements LeitorPdfFinanceiro {
	private static final Pattern DATA_COMPLETA = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})");
	private static final Pattern VENCIMENTO = Pattern.compile("(?iu)Vencimento\\s*+:?\\s*+(\\d{2}/\\d{2}/\\d{4})");
	private static final Pattern VALOR_TOTAL = Pattern.compile("(?iu)(?:valor\\s++do\\s++documento|total\\s++(?:da\\s++)?fatura|valor\\s++total)\\s*+:?\\s*+R\\$\\s*+([\\d.]++,[\\d]{2})");
	private static final Pattern SALDO_INICIAL = Pattern.compile("(?iu)saldo\\s++inicial\\s*+:?\\s*+R\\$\\s*+([\\d.]++,[\\d]{2})");
	private static final Pattern SALDO_FINAL = Pattern.compile("(?iu)saldo\\s++(?:final|atual|dispon[ií]vel)\\s*+:?\\s*+R\\$\\s*+([\\d.]++,[\\d]{2})");
	private static final Pattern LANCAMENTO = Pattern.compile("(?m)^(\\d{2}/\\d{2}/\\d{4})\\s++(.{1,1000}?)\\s++(-?R\\$\\s*+[\\d.]++,[\\d]{2})[ \\t]*+$");
	private static final Pattern IDENTIFICADOR_CONTA = Pattern.compile("(?iu)conta\\s*+(?:n[ºo.]*+)?\\s*+([\\d.-]{4,})");

	@Override
	public boolean suporta(LeitorDocumentoFinanceiroPort.ArquivoPdf arquivo, String texto) {
		String normalizado = texto.toLowerCase(Locale.ROOT);
		return normalizado.contains("extrato") || normalizado.contains("fatura") || normalizado.contains("boleto")
				|| normalizado.contains("valor do documento") || normalizado.contains("cobrança");
	}

	@Override
	public LeitorDocumentoFinanceiroPort.DocumentoLido ler(String texto) {
		TipoDocumentoFinanceiro tipo = tipo(texto);
		LocalDate vencimento = data(VENCIMENTO.matcher(texto));
		List<LancamentoImportado> lancamentos = lancamentos(texto);
		BigDecimal total = valor(VALOR_TOTAL.matcher(texto));
		if (tipo == TipoDocumentoFinanceiro.COBRANCA && lancamentos.isEmpty() && total != null) {
			lancamentos.add(LancamentoImportado.novo(1, vencimento, "Cobrança importada", limitar("Cobrança importada"), total,
					TipoTransacao.SAIDA, true, "Confirme a data, a descrição e a classificação da cobrança.", true));
		}
		return new LeitorDocumentoFinanceiroPort.DocumentoLido("generico-pdf-v1", tipo, identificador(texto), primeiraData(texto), ultimaData(texto),
				vencimento, valor(SALDO_INICIAL.matcher(texto)), valor(SALDO_FINAL.matcher(texto)), total, lancamentos);
	}

	private TipoDocumentoFinanceiro tipo(String texto) {
		String normalizado = texto.toLowerCase(Locale.ROOT);
		if (normalizado.contains("extrato")) return TipoDocumentoFinanceiro.EXTRATO_CONTA;
		if (normalizado.contains("fatura")) return TipoDocumentoFinanceiro.FATURA_CARTAO;
		if (normalizado.contains("boleto") || normalizado.contains("valor do documento") || normalizado.contains("cobrança"))
			return TipoDocumentoFinanceiro.COBRANCA;
		throw new DomainException("error.importacao.documento.nao.reconhecido");
	}

	private List<LancamentoImportado> lancamentos(String texto) {
		Matcher matcher = LANCAMENTO.matcher(texto);
		List<LancamentoImportado> resultado = new ArrayList<>();
		while (matcher.find()) {
			BigDecimal valor = monetario(matcher.group(3));
			resultado.add(LancamentoImportado.novo(resultado.size() + 1, data(matcher.group(1)), normalizar(matcher.group(2)),
					limitar(matcher.group(0)), valor.abs(), valor.signum() < 0 ? TipoTransacao.SAIDA : TipoTransacao.ENTRADA,
					true, "Confirme a data, o tipo e a classificação do lançamento.", true));
		}
		return resultado;
	}

	private LocalDate primeiraData(String texto) { Matcher m = DATA_COMPLETA.matcher(texto); return m.find() ? data(m.group()) : null; }
	private LocalDate ultimaData(String texto) { Matcher m = DATA_COMPLETA.matcher(texto); LocalDate ultima = null; while (m.find()) ultima = data(m.group()); return ultima; }
	private LocalDate data(Matcher matcher) { return matcher.find() ? data(matcher.group(1)) : null; }
	private LocalDate data(String valor) { String[] partes = valor.split("/"); return LocalDate.of(Integer.parseInt(partes[2]), Integer.parseInt(partes[1]), Integer.parseInt(partes[0])); }
	private BigDecimal valor(Matcher matcher) { return matcher.find() ? monetario(matcher.group(1)) : null; }
	private String identificador(String texto) { Matcher matcher = IDENTIFICADOR_CONTA.matcher(texto); return matcher.find() ? "conta-" + matcher.group(1) : null; }
	private BigDecimal monetario(String valor) {
		String normalizado = valor.replace('\u2212', '-').replaceAll("[^\\d,.-]", "").replace(".", "").replace(',', '.');
		return new BigDecimal(normalizado);
	}
	private String normalizar(String valor) { return valor.replaceAll("\\s+", " ").trim(); }
	private String limitar(String valor) { return valor.substring(0, Math.min(valor.length(), 1000)); }
}
