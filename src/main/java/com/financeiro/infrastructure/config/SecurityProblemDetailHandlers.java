package com.financeiro.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Component
public class SecurityProblemDetailHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
			throws IOException {
		write(response, request, HttpStatus.UNAUTHORIZED, "Autenticação ausente ou inválida.");
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
			throws IOException {
		write(response, request, HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso.");
	}

	private void write(HttpServletResponse response, HttpServletRequest request, HttpStatus status, String detail)
			throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.getWriter()
				.write("{\"type\":\"about:blank\",\"title\":\"" + status.getReasonPhrase() + "\",\"status\":"
						+ status.value() + ",\"detail\":\"" + detail + "\",\"instance\":\""
						+ URI.create(request.getRequestURI()) + "\"}");
	}
}
