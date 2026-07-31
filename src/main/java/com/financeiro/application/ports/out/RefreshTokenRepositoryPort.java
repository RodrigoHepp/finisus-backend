package com.financeiro.application.ports.out;

import java.util.Optional;

public interface RefreshTokenRepositoryPort {

	void salvar(String token, Long usuarioId, java.time.Instant expiraEm);

	Optional<Long> buscarUsuarioIdPorToken(String token);

	void invalidar(String token);

	void invalidarTodosDoUsuario(Long usuarioId);
}
