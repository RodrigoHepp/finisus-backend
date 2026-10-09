package com.finisus.adapters.out.csv;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

record TabelaCsv(List<String> cabecalhos, List<Linha> linhas) {
	TabelaCsv {
		cabecalhos = List.copyOf(cabecalhos);
		linhas = List.copyOf(linhas);
	}

	boolean possuiCabecalhos(String... esperados) {
		Set<String> existentes = cabecalhos.stream().map(TabelaCsv::normalizarCabecalho).collect(Collectors.toSet());
		for (String esperado : esperados) {
			if (!existentes.contains(normalizarCabecalho(esperado))) return false;
		}
		return true;
	}

	static String normalizarCabecalho(String valor) {
		return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
				.replaceAll("\\p{M}+", "").replace('\u00a0', ' ').trim().toLowerCase(Locale.ROOT)
				.replaceAll("\\s+", " ");
	}

	record Linha(int numero, String original, Map<String, String> valores) {
		Linha(int numero, String original, List<String> cabecalhos, List<String> campos) {
			this(numero, original, mapear(cabecalhos, campos));
		}

		String valor(String cabecalho) {
			return valores.get(TabelaCsv.normalizarCabecalho(cabecalho));
		}

		private static Map<String, String> mapear(List<String> cabecalhos, List<String> campos) {
			Map<String, String> resultado = new LinkedHashMap<>();
			for (int indice = 0; indice < cabecalhos.size(); indice++) {
				resultado.put(TabelaCsv.normalizarCabecalho(cabecalhos.get(indice)), campos.get(indice).trim());
			}
			return Map.copyOf(resultado);
		}
	}
}
