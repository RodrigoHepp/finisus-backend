package com.finisus.adapters.out.csv;

import com.finisus.domain.DomainException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ParserCsvLimitado {
	private static final int MAXIMO_LINHAS = 10_000;
	private static final int MAXIMO_COLUNAS = 64;
	private static final int MAXIMO_CARACTERES_CAMPO = 4_096;

	TabelaCsv ler(byte[] conteudo) {
		String texto = decodificar(conteudo);
		if (texto.startsWith("\ufeff")) texto = texto.substring(1);
		if (texto.indexOf('\0') >= 0) throw invalido();
		List<List<String>> registros = separar(texto);
		if (registros.size() < 2) throw invalido();
		List<String> cabecalhos = registros.getFirst();
		validarCabecalhos(cabecalhos);
		List<TabelaCsv.Linha> linhas = new ArrayList<>();
		for (int indice = 1; indice < registros.size(); indice++) {
			List<String> campos = registros.get(indice);
			if (campos.stream().allMatch(String::isBlank)) continue;
			if (campos.size() != cabecalhos.size()) throw invalido();
			linhas.add(new TabelaCsv.Linha(indice + 1, original(campos), cabecalhos, campos));
		}
		if (linhas.isEmpty()) throw invalido();
		return new TabelaCsv(cabecalhos, linhas);
	}

	private String decodificar(byte[] conteudo) {
		try {
			return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(conteudo)).toString();
		} catch (CharacterCodingException exception) {
			throw invalido();
		}
	}

	private List<List<String>> separar(String texto) {
		List<List<String>> registros = new ArrayList<>();
		List<String> registro = new ArrayList<>();
		StringBuilder campo = new StringBuilder();
		boolean aspas = false;
		for (int indice = 0; indice < texto.length(); indice++) {
			char atual = texto.charAt(indice);
			if (atual == '"') {
				if (aspas && indice + 1 < texto.length() && texto.charAt(indice + 1) == '"') {
					adicionar(campo, '"');
					indice++;
				} else {
					aspas = !aspas;
				}
			} else if (atual == ';' && !aspas) {
				adicionarCampo(registro, campo);
			} else if ((atual == '\n' || atual == '\r') && !aspas) {
				if (atual == '\r' && indice + 1 < texto.length() && texto.charAt(indice + 1) == '\n') indice++;
				adicionarCampo(registro, campo);
				adicionarRegistro(registros, registro);
				registro = new ArrayList<>();
			} else {
				if (atual < 0x20 && atual != '\t' && atual != '\r' && atual != '\n') throw invalido();
				adicionar(campo, atual);
			}
		}
		if (aspas) throw invalido();
		if (!registro.isEmpty() || campo.length() > 0) {
			adicionarCampo(registro, campo);
			adicionarRegistro(registros, registro);
		}
		return registros;
	}

	private void adicionar(StringBuilder campo, char caractere) {
		if (campo.length() >= MAXIMO_CARACTERES_CAMPO) throw invalido();
		campo.append(caractere);
	}

	private void adicionarCampo(List<String> registro, StringBuilder campo) {
		if (registro.size() >= MAXIMO_COLUNAS) throw invalido();
		registro.add(campo.toString());
		campo.setLength(0);
	}

	private void adicionarRegistro(List<List<String>> registros, List<String> registro) {
		if (registros.size() >= MAXIMO_LINHAS) throw invalido();
		registros.add(List.copyOf(registro));
	}

	private void validarCabecalhos(List<String> cabecalhos) {
		if (cabecalhos.isEmpty() || cabecalhos.size() > MAXIMO_COLUNAS) throw invalido();
		Set<String> unicos = new HashSet<>();
		for (String cabecalho : cabecalhos) {
			String normalizado = TabelaCsv.normalizarCabecalho(cabecalho);
			if (normalizado.isBlank() || !unicos.add(normalizado)) throw invalido();
		}
	}

	private String original(List<String> campos) {
		String texto = String.join(";", campos);
		return texto.substring(0, Math.min(texto.length(), 1_000));
	}

	private DomainException invalido() {
		return new DomainException("error.importacao.arquivo.invalido");
	}
}
