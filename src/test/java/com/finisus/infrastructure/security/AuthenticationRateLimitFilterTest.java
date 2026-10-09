package com.finisus.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AuthenticationRateLimitFilterTest {

	private final MutableClock clock = new MutableClock(Instant.parse("2026-10-08T12:00:00Z"));
	private final AuthenticationRateLimitProperties properties = new AuthenticationRateLimitProperties(true, 1,
			Duration.ofMinutes(1), 100);
	private final AuthenticationRateLimitFilter filter = new AuthenticationRateLimitFilter(properties,
			new ObjectMapper(), clock);

	@Test
	void limitaLoginPorEnderecoRemotoERetornaProblemDetailComRetryAfter() throws Exception {
		AtomicInteger chamadas = new AtomicInteger();
		var primeiraResposta = executar("/api/v1/auth/login", "198.51.100.10", null, chamadas);
		var segundaResposta = executar("/api/v1/auth/login", "198.51.100.10", null, chamadas);

		assertThat(primeiraResposta.getStatus()).isEqualTo(200);
		assertThat(segundaResposta.getStatus()).isEqualTo(429);
		assertThat(segundaResposta.getHeader("Retry-After")).isEqualTo("60");
		assertThat(segundaResposta.getContentType()).startsWith("application/problem+json");
		assertThat(segundaResposta.getContentAsString()).contains("error.auth.rate-limit");
		assertThat(chamadas).hasValue(1);
	}

	@Test
	void ignoraXForwardedForEnviadoPeloCliente() throws Exception {
		AtomicInteger chamadas = new AtomicInteger();
		executar("/api/v1/auth/refresh", "203.0.113.20", "198.51.100.1", chamadas);
		var resposta = executar("/api/v1/auth/refresh", "203.0.113.20", "198.51.100.2", chamadas);

		assertThat(resposta.getStatus()).isEqualTo(429);
		assertThat(chamadas).hasValue(1);
	}

	@Test
	void separaEnderecosERenovaJanelaExpirada() throws Exception {
		AtomicInteger chamadas = new AtomicInteger();
		executar("/api/v1/auth/login", "192.0.2.1", null, chamadas);
		var outroEndereco = executar("/api/v1/auth/login", "192.0.2.2", null, chamadas);
		clock.advance(Duration.ofMinutes(1));
		var novaJanela = executar("/api/v1/auth/login", "192.0.2.1", null, chamadas);

		assertThat(outroEndereco.getStatus()).isEqualTo(200);
		assertThat(novaJanela.getStatus()).isEqualTo(200);
		assertThat(chamadas).hasValue(3);
	}

	@Test
	void naoLimitaOutrasRotasNemMetodos() throws Exception {
		AtomicInteger chamadas = new AtomicInteger();
		executar("/api/v1/contas", "192.0.2.10", null, chamadas);
		executar("/api/v1/contas", "192.0.2.10", null, chamadas);

		assertThat(chamadas).hasValue(2);
	}

	private MockHttpServletResponse executar(String path, String remoteAddress, String forwardedFor,
			AtomicInteger chamadas) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
		request.setRemoteAddr(remoteAddress);
		if (forwardedFor != null) {
			request.addHeader("X-Forwarded-For", forwardedFor);
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> chamadas.incrementAndGet());
		return response;
	}

	private static final class MutableClock extends Clock {
		private Instant instant;

		private MutableClock(Instant instant) {
			this.instant = instant;
		}

		private void advance(Duration duration) {
			instant = instant.plus(duration);
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return instant;
		}
	}
}
