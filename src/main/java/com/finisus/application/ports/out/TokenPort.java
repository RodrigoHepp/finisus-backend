package com.finisus.application.ports.out;

import java.time.Instant;
import java.util.Optional;

public interface TokenPort {

	String gerarAccessToken(Long usuarioId, String email, long sessaoVersao);

	String gerarRefreshToken(Long usuarioId);

	Optional<Long> validarAccessToken(String token);

	Optional<Long> validarRefreshToken(String token);

	Instant expiracaoAccessToken();

	Instant expiracaoRefreshToken();
}
