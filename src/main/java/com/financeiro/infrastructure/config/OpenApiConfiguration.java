package com.financeiro.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

	public static final String BEARER_AUTH = "bearerAuth";

	@Bean
	OpenAPI financeiroOpenApi() {
		return new OpenAPI()
				.info(new Info().title("Finisus API").version("v1")
						.description("API REST para gestão de finanças pessoais."))
				.components(new Components().addSecuritySchemes(BEARER_AUTH,
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
								.description("Informe o access token obtido em /api/v1/auth/login.")));
	}
}
