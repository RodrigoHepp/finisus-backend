package com.finisus.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUsuarioAutenticadoAuthenticationConverterTest {
	private final JwtUsuarioAutenticadoAuthenticationConverter converter = new JwtUsuarioAutenticadoAuthenticationConverter();

	@Test
	void converteSubjectNumericoParaPrincipalTipado() {
		Jwt jwt = Jwt.withTokenValue("token-de-teste").header("alg", "RS256").subject("42")
				.claim("permissoes", java.util.List.of("USUARIO_CADASTRAR", "USUARIO_DESBLOQUEAR")).build();

		var authentication = converter.convert(jwt);

		assertThat(authentication.getPrincipal()).isEqualTo(new UsuarioAutenticado(42L));
		assertThat(authentication.getAuthorities()).extracting("authority")
				.containsExactly("USUARIO_CADASTRAR", "USUARIO_DESBLOQUEAR");
	}

	@Test
	void tokenSemPermissoesNaoRecebeAuthorities() {
		Jwt jwt = Jwt.withTokenValue("token-de-teste").header("alg", "RS256").subject("42").build();

		assertThat(converter.convert(jwt).getAuthorities()).isEmpty();
	}
}
