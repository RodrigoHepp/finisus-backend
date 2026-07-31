package com.finisus.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String issuer, long accessTokenExpirationMinutes, long refreshTokenExpirationDays,
		String privateKeyLocation, String publicKeyLocation) {
}
