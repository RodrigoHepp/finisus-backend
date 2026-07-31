package com.finisus.infrastructure.config;

import com.finisus.application.ports.out.UsuarioRepositoryPort;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class UsuarioSessaoJwtValidator implements OAuth2TokenValidator<Jwt> {

	private static final OAuth2Error INVALID_TOKEN = new OAuth2Error("invalid_token", "Sessão inválida ou expirada",
			null);

	private final UsuarioRepositoryPort usuarios;

	public UsuarioSessaoJwtValidator(UsuarioRepositoryPort usuarios) {
		this.usuarios = usuarios;
	}

	@Override
	public OAuth2TokenValidatorResult validate(Jwt jwt) {
		if (!"access".equals(jwt.getClaimAsString("type"))) {
			return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
		}
		Long usuarioId;
		Long sessaoVersao = jwt.getClaim("sessao_versao");
		try {
			usuarioId = Long.valueOf(jwt.getSubject());
		} catch (NumberFormatException exception) {
			return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
		}
		if (sessaoVersao == null) {
			return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
		}
		return usuarios.buscarPorId(usuarioId)
				.filter(usuario -> usuario.isAtivo() && usuario.getSessaoVersao() == sessaoVersao)
				.map(usuario -> OAuth2TokenValidatorResult.success())
				.orElseGet(() -> OAuth2TokenValidatorResult.failure(INVALID_TOKEN));
	}
}
