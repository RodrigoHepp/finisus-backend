package com.financeiro;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@TestConfiguration
public class TestJwtKeyConfig {
	@Bean
	RSAKey rsaKey() throws Exception {
		return new RSAKeyGenerator(2048).keyID("test-jwt-key").generate();
	}

	@Bean
	RSAPublicKey rsaPublicKey(RSAKey key) throws Exception {
		return key.toRSAPublicKey();
	}

	@Bean
	RSAPrivateKey rsaPrivateKey(RSAKey key) throws Exception {
		return key.toRSAPrivateKey();
	}
}
