package com.finisus;

import static org.assertj.core.api.Assertions.assertThat;

import com.finisus.application.ports.in.GerenciarPerfilUseCase;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.domain.model.Usuario;
import com.finisus.domain.vo.Email;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
class PrivacidadeIntegrationTest {
	@Autowired
	private UsuarioRepositoryPort usuarios;

	@Autowired
	private GerenciarPerfilUseCase perfil;

	@Test
	void exportaTodasAsSecoesSemExporCredenciais() {
		Long usuarioId = novoUsuario();

		var exportacao = perfil.exportarDados(usuarioId);

		assertThat(exportacao.versaoFormato()).isEqualTo(1);
		assertThat(exportacao.geradaEm()).isNotNull();
		assertThat(exportacao.secoes())
				.containsKeys("perfil", "transacoes", "importacoes", "divisoesCompartilhadas",
						"solicitacoesPrivacidade");
		assertThat(exportacao.secoes().get("perfil")).singleElement()
				.satisfies(registro -> assertThat(registro.values()).contains("Titular"));
		assertThat(exportacao.secoes().values()).allSatisfy(registros -> registros.forEach(registro ->
				assertThat(registro.keySet()).allSatisfy(chave -> {
					String normalizada = chave.toLowerCase(Locale.ROOT);
					assertThat(normalizada).doesNotContain("senha", "token", "hash_idempotencia");
				})));
	}

	@Test
	void reutilizaSolicitacaoDeAnonimizacaoAindaAberta() {
		Long usuarioId = novoUsuario();

		var primeira = perfil.solicitarAnonimizacao(usuarioId, "Não desejo mais manter a conta");
		var repetida = perfil.solicitarAnonimizacao(usuarioId, "Motivo repetido");

		assertThat(primeira.id()).isNotNull();
		assertThat(repetida.id()).isEqualTo(primeira.id());
		assertThat(perfil.listarSolicitacoesPrivacidade(usuarioId)).singleElement()
				.satisfies(solicitacao -> assertThat(solicitacao.motivo())
						.isEqualTo("Não desejo mais manter a conta"));
	}

	private Long novoUsuario() {
		String sufixo = UUID.randomUUID().toString();
		return usuarios.salvar(Usuario.novo("Titular", new Email("privacidade-" + sufixo + "@teste.local"),
				"hash-sintetico", LocalDateTime.now())).getId();
	}
}
