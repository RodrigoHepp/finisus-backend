package com.finisus.infrastructure.config;

import com.finisus.infrastructure.security.AuthenticationRateLimitFilter;
import com.finisus.infrastructure.security.AuthenticationRateLimitProperties;
import com.finisus.infrastructure.security.JwtUsuarioAutenticadoAuthenticationConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({ JwtProperties.class, AuthenticationRateLimitProperties.class })
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProblemDetailHandlers problemHandlers,
			JwtUsuarioAutenticadoAuthenticationConverter authenticationConverter,
			AuthenticationRateLimitProperties rateLimitProperties) throws Exception {
		return http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh", "/actuator/health")
						.permitAll()
						.requestMatchers("/api/v1/docs/**", "/api/v1/swagger-ui.html", "/api/v1/swagger-ui/**",
								"/swagger-ui/**")
						.hasAuthority("DOCUMENTACAO_API_LER")
						.requestMatchers("/actuator/**").hasAuthority("OBSERVABILIDADE_LER")
						.requestMatchers("/api/v1/auth/cadastro").hasAuthority("USUARIO_CADASTRAR")
						.requestMatchers("/api/v1/usuarios/*/desbloquear").hasAuthority("USUARIO_DESBLOQUEAR")
						.anyRequest().authenticated())
				.oauth2ResourceServer(
						oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)))
				.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(problemHandlers)
						.accessDeniedHandler(problemHandlers))
				.addFilterBefore(new AuthenticationRateLimitFilter(rateLimitProperties),
						BearerTokenAuthenticationFilter.class)
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
		List<String> origins = Arrays.stream(allowedOrigins.split(",")).map(String::trim)
				.filter(origin -> !origin.isBlank()).distinct().toList();
		if (origins.isEmpty() || origins.stream().anyMatch(origin -> origin.contains("*"))) {
			throw new IllegalStateException("app.cors.allowed-origins exige ao menos uma origem exata e não aceita curingas");
		}
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(origins);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key"));
		configuration.setExposedHeaders(List.of("Location", "Retry-After"));
		configuration.setAllowCredentials(false);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
