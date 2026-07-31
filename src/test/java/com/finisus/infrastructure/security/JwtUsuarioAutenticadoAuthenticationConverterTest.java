package com.finisus.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUsuarioAutenticadoAuthenticationConverterTest {
	private final JwtUsuarioAutenticadoAuthenticationConverter converter = new JwtUsuarioAutenticadoAuthenticationConverter();

	@Test
	void converteSubjectNumericoParaPrincipalTipado() {
		Jwt jwt = Jwt.withTokenValue("token-de-teste").header("alg", "RS256").subject("42")
				.claim("scope", "financeiro:ler").build();

		var authentication = converter.convert(jwt);

		assertThat(authentication.getPrincipal()).isEqualTo(new UsuarioAutenticado(42L));
		assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("SCOPE_financeiro:ler");
	}
}
