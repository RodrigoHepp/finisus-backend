package com.finisus.adapters.in.web.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Injeta o identificador do usuário autenticado em endpoints protegidos. */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "id", errorOnInvalidType = true)
public @interface UsuarioAtual {
}
