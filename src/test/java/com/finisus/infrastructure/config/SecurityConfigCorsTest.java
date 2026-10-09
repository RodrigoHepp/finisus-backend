package com.finisus.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SecurityConfigCorsTest {
	private final SecurityConfig securityConfig = new SecurityConfig();

	@Test
	void permiteSomenteOrigensExatasConfiguradas() {
		var source = securityConfig.corsConfigurationSource(
				" http://localhost:4200, https://app.finisus.example,https://app.finisus.example ");

		var configuration = source.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/api/v1/contas"));

		assertThat(configuration).isNotNull();
		assertThat(configuration.getAllowedOrigins())
				.containsExactly("http://localhost:4200", "https://app.finisus.example");
		assertThat(configuration.getAllowedOriginPatterns()).isNullOrEmpty();
		assertThat(configuration.checkOrigin("https://app.finisus.example"))
				.isEqualTo("https://app.finisus.example");
		assertThat(configuration.checkOrigin("https://origem-nao-configurada.example")).isNull();
	}

	@Test
	void recusaConfiguracaoVaziaOuComCuringa() {
		assertThatThrownBy(() -> securityConfig.corsConfigurationSource(" "))
				.isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> securityConfig.corsConfigurationSource("https://*.finisus.example"))
				.isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> securityConfig.corsConfigurationSource("*"))
				.isInstanceOf(IllegalStateException.class);
	}
}
