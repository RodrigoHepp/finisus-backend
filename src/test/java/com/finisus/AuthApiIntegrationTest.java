package com.finisus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.finisus.application.ports.out.PasswordEncoderPort;
import com.finisus.application.ports.out.TokenPort;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.domain.model.PermissaoUsuario;
import com.finisus.domain.model.Usuario;
import com.finisus.domain.vo.Email;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
@Transactional
class AuthApiIntegrationTest {
	@Autowired MockMvc mockMvc;
	@Autowired UsuarioRepositoryPort usuarios;
	@Autowired PasswordEncoderPort senhas;
	@Autowired TokenPort tokens;

	@Test
	void cadastroAnonimoRetornaNaoAutorizado() throws Exception {
		mockMvc.perform(post("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
				.content(cadastro("Novo", emailUnico("novo")))).andExpect(status().isUnauthorized());
	}

	@Test
	void cadastroAutenticadoSemPermissaoRetornaProibido() throws Exception {
		Usuario solicitante = criarUsuario("Comum", emailUnico("comum"), Set.of());
		mockMvc.perform(post("/api/v1/auth/cadastro").header("Authorization", bearer(solicitante))
				.contentType(MediaType.APPLICATION_JSON).content(cadastro("Novo", emailUnico("novo"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void cadastroComPermissaoCriaUsuarioSemPermissoesAdministrativas() throws Exception {
		Usuario administrador = criarUsuario("Administrador", emailUnico("admin"),
				Set.of(PermissaoUsuario.USUARIO_CADASTRAR));
		String emailNovo = emailUnico("novo");
		mockMvc.perform(post("/api/v1/auth/cadastro").header("Authorization", bearer(administrador))
				.contentType(MediaType.APPLICATION_JSON).content(cadastro("Novo", emailNovo)))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.email").value(emailNovo));
		assertThat(usuarios.buscarPorEmail(emailNovo).orElseThrow().getPermissoes()).isEmpty();
	}

	@Test
	void cincoFalhasBloqueiamELoginCorretoPermaneceRecusado() throws Exception {
		Usuario usuario = criarUsuario("Bloqueado", emailUnico("bloqueado"), Set.of());
		for (int tentativa = 1; tentativa <= 4; tentativa++) {
			autenticacaoRecusada(login(usuario.getEmail().valor(), "senha-incorreta"));
		}
		autenticacaoRecusada(login(usuario.getEmail().valor(), "senha-incorreta"));
		Usuario bloqueado = usuarios.buscarPorEmail(usuario.getEmail().valor()).orElseThrow();
		assertThat(bloqueado.isBloqueado()).isTrue();
		assertThat(bloqueado.getTentativasLoginInvalidas()).isEqualTo(5);
		autenticacaoRecusada(login(usuario.getEmail().valor(), "senha-segura"));
		autenticacaoRecusada(login(emailUnico("inexistente"), "senha-incorreta"));
	}

	@Test
	void desbloqueioExigePermissaoERevogaAccessTokenAnterior() throws Exception {
		Usuario semPermissao = criarUsuario("Comum", emailUnico("comum"), Set.of());
		Usuario administrador = criarUsuario("Administrador", emailUnico("admin"),
				Set.of(PermissaoUsuario.USUARIO_DESBLOQUEAR));
		Usuario alvo = criarUsuario("Alvo", emailUnico("alvo"), Set.of());
		String tokenAnterior = tokens.gerarAccessToken(alvo.getId(), alvo.getEmail().valor(), alvo.getSessaoVersao(),
				alvo.getPermissoes());

		mockMvc.perform(post("/api/v1/usuarios/{id}/desbloquear", alvo.getId()).header("Authorization",
				bearer(semPermissao))).andExpect(status().isForbidden());
		mockMvc.perform(post("/api/v1/usuarios/{id}/desbloquear", alvo.getId()).header("Authorization",
				bearer(administrador))).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + tokenAnterior))
				.andExpect(status().isUnauthorized());
	}

	private ResultActions login(String email, String senha) throws Exception {
		return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"));
	}

	private void autenticacaoRecusada(ResultActions resultado) throws Exception {
		resultado.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("error.auth.invalid"))
				.andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
	}

	private Usuario criarUsuario(String nome, String email, Set<PermissaoUsuario> permissoes) {
		return usuarios.salvar(Usuario.novo(nome, new Email(email), senhas.encode("senha-segura"),
				LocalDateTime.of(2026, 10, 8, 9, 0), permissoes));
	}

	private String bearer(Usuario usuario) {
		return "Bearer " + tokens.gerarAccessToken(usuario.getId(), usuario.getEmail().valor(),
				usuario.getSessaoVersao(), usuario.getPermissoes());
	}

	private String cadastro(String nome, String email) {
		return "{\"nome\":\"" + nome + "\",\"email\":\"" + email + "\",\"senha\":\"senha-segura\"}";
	}

	private String emailUnico(String prefixo) {
		return prefixo + "." + UUID.randomUUID() + "@example.com";
	}
}
