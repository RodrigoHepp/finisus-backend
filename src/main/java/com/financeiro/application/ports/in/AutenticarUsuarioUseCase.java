package com.financeiro.application.ports.in;

import java.time.Instant;

public interface AutenticarUsuarioUseCase {

	Result executar(Command command);

	record Command(String email, String senha) {
	}

	record Result(Long usuarioId, String accessToken, String refreshToken, Instant accessTokenExpiraEm) {
	}
}
