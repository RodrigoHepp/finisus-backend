package com.finisus.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.domain.model.PermissaoUsuario;
import com.finisus.domain.model.Usuario;
import com.finisus.domain.vo.Email;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class UsuarioSessaoJwtValidatorTest {
	private final UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
	private final UsuarioSessaoJwtValidator validator = new UsuarioSessaoJwtValidator(usuarios);

	@Test
	void aceitaAccessTokenDeUsuarioAtivoComVersaoAtual() {
		when(usuarios.buscarPorId(42L)).thenReturn(Optional.of(usuario(true, 3)));

		assertThat(validator.validate(jwt("42", "access", 3)).hasErrors()).isFalse();
	}

	@Test
	void rejeitaUsuarioInativo() {
		when(usuarios.buscarPorId(42L)).thenReturn(Optional.of(usuario(false, 3)));

		assertThat(validator.validate(jwt("42", "access", 3L)).hasErrors()).isTrue();
	}

	@Test
	void rejeitaSessaoRevogadaPorMudancaDeVersao() {
		when(usuarios.buscarPorId(42L)).thenReturn(Optional.of(usuario(true, 4)));

		assertThat(validator.validate(jwt("42", "access", 3L)).hasErrors()).isTrue();
	}

	@Test
	void rejeitaUsuarioBloqueado() {
		when(usuarios.buscarPorId(42L)).thenReturn(Optional.of(Usuario.reconstituir(42L, "Ana",
				new Email("ana@finisus.test"), "hash", true, LocalDateTime.of(2026, 1, 1, 0, 0), 3, 5, true,
				Set.of())));

		assertThat(validator.validate(jwt("42", "access", 3L)).hasErrors()).isTrue();
	}

	@Test
	void rejeitaTokenQuandoPermissoesForamAlteradas() {
		when(usuarios.buscarPorId(42L)).thenReturn(Optional.of(Usuario.reconstituir(42L, "Ana",
				new Email("ana@finisus.test"), "hash", true, LocalDateTime.of(2026, 1, 1, 0, 0), 3, 0, false,
				Set.of(PermissaoUsuario.USUARIO_CADASTRAR))));

		assertThat(validator.validate(jwt("42", "access", 3L)).hasErrors()).isTrue();
	}

	@Test
	void aceitaTokenComAsPermissoesAtuais() {
		when(usuarios.buscarPorId(42L)).thenReturn(Optional.of(Usuario.reconstituir(42L, "Ana",
				new Email("ana@finisus.test"), "hash", true, LocalDateTime.of(2026, 1, 1, 0, 0), 3, 0, false,
				Set.of(PermissaoUsuario.USUARIO_CADASTRAR))));

		Jwt jwt = Jwt.withTokenValue("token-de-teste").header("alg", "RS256").subject("42")
				.claim("type", "access").claim("sessao_versao", 3L)
				.claim("permissoes", Set.of("USUARIO_CADASTRAR")).build();

		assertThat(validator.validate(jwt).hasErrors()).isFalse();
	}

	@Test
	void rejeitaTipoSubjectEClaimDeSessaoInvalidos() {
		assertThat(validator.validate(jwt("42", "refresh", 3L)).hasErrors()).isTrue();
		assertThat(validator.validate(jwt("invalido", "access", 3L)).hasErrors()).isTrue();
		assertThat(validator.validate(jwt("42", "access", "3")).hasErrors()).isTrue();
		assertThat(validator.validate(jwtSemSessao("42")).hasErrors()).isTrue();
	}

	private Usuario usuario(boolean ativo, long sessaoVersao) {
		return Usuario.reconstituir(42L, "Ana", new Email("ana@finisus.test"), "hash", ativo,
				LocalDateTime.of(2026, 1, 1, 0, 0), sessaoVersao);
	}

	private Jwt jwt(String subject, String tipo, Object sessaoVersao) {
		return Jwt.withTokenValue("token-de-teste").header("alg", "RS256").subject(subject).claim("type", tipo)
				.claim("sessao_versao", sessaoVersao).build();
	}

	private Jwt jwtSemSessao(String subject) {
		return Jwt.withTokenValue("token-de-teste").header("alg", "RS256").subject(subject).claim("type", "access")
				.build();
	}
}
