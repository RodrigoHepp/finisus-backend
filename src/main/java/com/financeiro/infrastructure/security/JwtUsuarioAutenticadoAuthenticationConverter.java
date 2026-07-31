package com.financeiro.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

/** Converte um JWT já validado no principal tipado usado pelos controllers. */
@Component
public class JwtUsuarioAutenticadoAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
	private final JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		UsuarioAutenticado principal = new UsuarioAutenticado(Long.valueOf(jwt.getSubject()));
		return new UsernamePasswordAuthenticationToken(principal, jwt, authoritiesConverter.convert(jwt));
	}
}
