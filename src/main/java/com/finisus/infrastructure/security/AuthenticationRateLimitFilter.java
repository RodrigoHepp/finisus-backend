package com.finisus.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

public final class AuthenticationRateLimitFilter extends OncePerRequestFilter {

	private static final Set<String> ROTAS_PROTEGIDAS = Set.of("/api/v1/auth/login", "/api/v1/auth/refresh");
	private static final String CODIGO = "error.auth.rate-limit";

	private final AuthenticationRateLimitProperties properties;
	private final ObjectMapper objectMapper;
	private final Clock clock;
	private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
	private final AtomicLong operations = new AtomicLong();

	public AuthenticationRateLimitFilter(AuthenticationRateLimitProperties properties, ObjectMapper objectMapper) {
		this(properties, objectMapper, Clock.systemUTC());
	}

	public AuthenticationRateLimitFilter(AuthenticationRateLimitProperties properties) {
		this(properties, new ObjectMapper(), Clock.systemUTC());
	}

	AuthenticationRateLimitFilter(AuthenticationRateLimitProperties properties, ObjectMapper objectMapper, Clock clock) {
		this.properties = properties;
		this.objectMapper = objectMapper;
		this.clock = clock;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !properties.enabled() || !"POST".equals(request.getMethod())
				|| !ROTAS_PROTEGIDAS.contains(request.getRequestURI());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Instant now = clock.instant();
		if ((operations.incrementAndGet() & 63) == 0) {
			removeExpired(now);
		}

		String identifier = normalizeRemoteAddress(request.getRemoteAddr());
		Window window = windowFor(identifier, now);
		Attempt attempt = window.register(now, properties.window(), properties.maxRequests());
		if (attempt.allowed()) {
			filterChain.doFilter(request, response);
			return;
		}

		writeRateLimitProblem(request, response, attempt.retryAfter());
	}

	private Window windowFor(String identifier, Instant now) {
		Window current = windows.get(identifier);
		if (current != null) {
			return current;
		}
		synchronized (windows) {
			current = windows.get(identifier);
			if (current != null) {
				return current;
			}
			removeExpired(now);
			while (windows.size() >= properties.maxIdentifiers()) {
				windows.entrySet().stream().min(Comparator.comparing(entry -> entry.getValue().expiresAt()))
						.ifPresent(entry -> windows.remove(entry.getKey(), entry.getValue()));
			}
			Window created = new Window(now.plus(properties.window()));
			windows.put(identifier, created);
			return created;
		}
	}

	private void removeExpired(Instant now) {
		windows.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
	}

	private String normalizeRemoteAddress(String remoteAddress) {
		return remoteAddress == null || remoteAddress.isBlank() ? "unknown" : remoteAddress;
	}

	private void writeRateLimitProblem(HttpServletRequest request, HttpServletResponse response, Duration retryAfter)
			throws IOException {
		long retryAfterSeconds = Math.max(1, retryAfter.plusMillis(999).toSeconds());
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
				"Muitas tentativas de autenticação. Tente novamente mais tarde.");
		problem.setType(URI.create("urn:finisus:problem:" + CODIGO));
		problem.setTitle(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
		problem.setInstance(URI.create(request.getRequestURI()));
		problem.setProperty("code", CODIGO);

		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
		objectMapper.writeValue(response.getOutputStream(), problem);
	}

	private static final class Window {
		private Instant expiresAt;
		private int requests;

		private Window(Instant expiresAt) {
			this.expiresAt = expiresAt;
		}

		private synchronized Attempt register(Instant now, Duration duration, int maxRequests) {
			if (!expiresAt.isAfter(now)) {
				expiresAt = now.plus(duration);
				requests = 0;
			}
			requests++;
			return requests <= maxRequests ? new Attempt(true, Duration.ZERO)
					: new Attempt(false, Duration.between(now, expiresAt));
		}

		private synchronized Instant expiresAt() {
			return expiresAt;
		}
	}

	private record Attempt(boolean allowed, Duration retryAfter) {
	}
}
