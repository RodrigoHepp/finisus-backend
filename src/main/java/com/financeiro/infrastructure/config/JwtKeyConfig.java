package com.financeiro.infrastructure.config;

import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@Profile("!test")
public class JwtKeyConfig {
	private final JwtProperties properties;
	private final ResourceLoader resources;

	public JwtKeyConfig(JwtProperties properties, ResourceLoader resources) {
		this.properties = properties;
		this.resources = resources;
	}

	@Bean
	RSAKey rsaKey() throws Exception {
		if (properties.privateKeyLocation() == null || properties.publicKeyLocation() == null) {
			throw new IllegalStateException("Configure app.jwt.private-key-location e app.jwt.public-key-location.");
		}
		KeyFactory factory = KeyFactory.getInstance("RSA");
		RSAPrivateKey privateKey = (RSAPrivateKey) factory
				.generatePrivate(new PKCS8EncodedKeySpec(pem(properties.privateKeyLocation())));
		RSAPublicKey publicKey = (RSAPublicKey) factory
				.generatePublic(new X509EncodedKeySpec(pem(properties.publicKeyLocation())));
		return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID("finisus-jwt-key").build();
	}

	@Bean
	RSAPublicKey rsaPublicKey(RSAKey rsaKey) throws Exception {
		return rsaKey.toRSAPublicKey();
	}

	@Bean
	RSAPrivateKey rsaPrivateKey(RSAKey rsaKey) throws Exception {
		return rsaKey.toRSAPrivateKey();
	}

	private byte[] pem(String location) throws Exception {
		Resource resource = resources.getResource(location);
		if (!resource.exists())
			throw new IllegalStateException("Chave JWT não encontrada em " + location);
		String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.US_ASCII);
		return Base64.getMimeDecoder()
				.decode(content.replaceAll("-----BEGIN [A-Z ]+-----|-----END [A-Z ]+-----|\\s", ""));
	}
}
