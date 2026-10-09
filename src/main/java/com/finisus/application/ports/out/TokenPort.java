package com.finisus.application.ports.out;

import java.time.Instant;
import java.util.Set;
import java.util.Optional;

import com.finisus.domain.model.PermissaoUsuario;

public interface TokenPort {

	String gerarAccessToken(Long usuarioId, String email, long sessaoVersao, Set<PermissaoUsuario> permissoes);

	String gerarRefreshToken(Long usuarioId);

	Optional<Long> validarAccessToken(String token);

	Optional<Long> validarRefreshToken(String token);

	Instant expiracaoAccessToken();

	Instant expiracaoRefreshToken();
}
