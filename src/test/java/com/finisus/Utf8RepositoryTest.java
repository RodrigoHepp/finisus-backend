package com.finisus;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class Utf8RepositoryTest {

	private static final Set<String> DIRETORIOS_IGNORADOS = Set.of(".git", "target", "tmp", "node_modules", ".idea");
	private static final Set<String> EXTENSOES_TEXTUAIS = Set.of(
			"java", "properties", "sql", "xml", "md", "json", "yaml", "yml", "toml",
			"csv", "txt", "py", "ps1", "sh", "bat", "cmd");
	private static final List<String> MARCADORES_MOJIBAKE = List.of(
			par(0x00c3, 0x00a1), par(0x00c3, 0x00a9), par(0x00c3, 0x00ad),
			par(0x00c3, 0x00b3), par(0x00c3, 0x00ba), par(0x00c3, 0x00a3),
			par(0x00c3, 0x00b5), par(0x00c3, 0x00a7), par(0x00c2, 0x00ba),
			String.valueOf((char) 0xfffd));

	@Test
	void arquivosTextuaisDevemSerUtf8SemMojibake() throws IOException {
		Path raiz = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
		List<String> problemas = new ArrayList<>();

		try (Stream<Path> arquivos = Files.walk(raiz)) {
			arquivos.filter(Files::isRegularFile)
					.filter(arquivo -> !estaEmDiretorioIgnorado(raiz, arquivo))
					.filter(Utf8RepositoryTest::ehArquivoTextual)
					.forEach(arquivo -> validarArquivo(raiz, arquivo, problemas));
		}

		assertTrue(problemas.isEmpty(), () -> "Arquivos com codificação inválida ou mojibake:\n"
				+ String.join("\n", problemas));
	}

	private static boolean estaEmDiretorioIgnorado(Path raiz, Path arquivo) {
		for (Path parte : raiz.relativize(arquivo)) {
			if (DIRETORIOS_IGNORADOS.contains(parte.toString())) {
				return true;
			}
		}
		return false;
	}

	private static boolean ehArquivoTextual(Path arquivo) {
		String nome = arquivo.getFileName().toString();
		if (nome.equals(".gitignore") || nome.equals(".gitattributes") || nome.equals(".editorconfig")) {
			return true;
		}
		int separador = nome.lastIndexOf('.');
		return separador >= 0 && EXTENSOES_TEXTUAIS.contains(nome.substring(separador + 1).toLowerCase(Locale.ROOT));
	}

	private static void validarArquivo(Path raiz, Path arquivo, List<String> problemas) {
		String relativo = raiz.relativize(arquivo).toString();
		try {
			String conteudo = decodificarUtf8Estrito(Files.readAllBytes(arquivo));
			MARCADORES_MOJIBAKE.stream()
					.filter(conteudo::contains)
					.findFirst()
					.ifPresent(marcador -> problemas.add(relativo + " contém marcador de mojibake"));
		} catch (CharacterCodingException exception) {
			problemas.add(relativo + " não é UTF-8 válido");
		} catch (IOException exception) {
			problemas.add(relativo + " não pôde ser lido: " + exception.getMessage());
		}
	}

	private static String decodificarUtf8Estrito(byte[] bytes) throws CharacterCodingException {
		return StandardCharsets.UTF_8.newDecoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.decode(ByteBuffer.wrap(bytes))
				.toString();
	}

	private static String par(int primeiro, int segundo) {
		return new String(new char[] {(char) primeiro, (char) segundo});
	}
}
