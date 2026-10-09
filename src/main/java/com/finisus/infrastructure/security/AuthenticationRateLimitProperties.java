package com.finisus.infrastructure.security;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security.rate-limit.authentication")
public record AuthenticationRateLimitProperties(boolean enabled,
		@Min(1) @Max(10_000) int maxRequests,
		@NotNull Duration window,
		@Min(100) @Max(1_000_000) int maxIdentifiers) {

	public AuthenticationRateLimitProperties {
		if (window == null || window.isZero() || window.isNegative()) {
			throw new IllegalArgumentException("A janela do rate limit deve ser positiva");
		}
	}
}
