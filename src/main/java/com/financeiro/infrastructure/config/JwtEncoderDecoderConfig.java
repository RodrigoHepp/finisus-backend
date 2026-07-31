package com.financeiro.infrastructure.config;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;

@Configuration
public class JwtEncoderDecoderConfig {

	@Bean
	public JwtEncoder jwtEncoder(RSAKey rsaKey) {
		return new NimbusJwtEncoder(new ImmutableJWKSet<>(new com.nimbusds.jose.jwk.JWKSet(rsaKey)));
	}

	@Bean
	public JwtDecoder jwtDecoder(RSAKey rsaKey, JwtProperties jwtProperties,
			UsuarioSessaoJwtValidator usuarioSessaoJwtValidator) throws Exception {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()), usuarioSessaoJwtValidator));
		return decoder;
	}
}
