package com.finisus.adapters.out.security;

import com.finisus.application.ports.out.TokenPort;
import com.finisus.infrastructure.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.finisus.domain.model.PermissaoUsuario;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenPort {

	private final JwtEncoder jwtEncoder;
	private final JwtDecoder jwtDecoder;
	private final JwtProperties jwtProperties;

	@Override
	public String gerarAccessToken(Long usuarioId, String email, long sessaoVersao,
			Set<PermissaoUsuario> permissoes) {
		Instant now = Instant.now();
		Instant exp = now.plus(jwtProperties.accessTokenExpirationMinutes(), ChronoUnit.MINUTES);
		JwtClaimsSet claims = JwtClaimsSet.builder().issuer(jwtProperties.issuer()).issuedAt(now).expiresAt(exp)
				.subject(String.valueOf(usuarioId)).claim("email", email).claim("type", "access")
				.claim("sessao_versao", sessaoVersao)
				.claim("permissoes", permissoes.stream().map(Enum::name).sorted().toList())
				.id(UUID.randomUUID().toString()).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}

	@Override
	public String gerarRefreshToken(Long usuarioId) {
		return UUID.randomUUID().toString() + "." + usuarioId;
	}

	@Override
	public Optional<Long> validarAccessToken(String token) {
		try {
			var jwt = jwtDecoder.decode(token);
			if (!"access".equals(jwt.getClaimAsString("type"))) {
				return Optional.empty();
			}
			return Optional.of(Long.parseLong(jwt.getSubject()));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Long> validarRefreshToken(String token) {
		return Optional.empty();
	}

	@Override
	public Instant expiracaoAccessToken() {
		return Instant.now().plus(jwtProperties.accessTokenExpirationMinutes(), ChronoUnit.MINUTES);
	}

	@Override
	public Instant expiracaoRefreshToken() {
		return Instant.now().plus(jwtProperties.refreshTokenExpirationDays(), ChronoUnit.DAYS);
	}
}
