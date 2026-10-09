package com.finisus.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

/** Converte um JWT já validado no principal tipado usado pelos controllers. */
@Component
public class JwtUsuarioAutenticadoAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		UsuarioAutenticado principal = new UsuarioAutenticado(Long.valueOf(jwt.getSubject()));
		List<String> permissoes = jwt.getClaimAsStringList("permissoes");
		var authorities = permissoes == null ? List.<SimpleGrantedAuthority>of()
				: permissoes.stream().map(SimpleGrantedAuthority::new).toList();
		return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
	}
}
