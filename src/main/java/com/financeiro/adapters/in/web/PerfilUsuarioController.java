package com.financeiro.adapters.in.web;

import com.financeiro.application.ports.in.GerenciarPerfilUseCase;
import com.financeiro.domain.model.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios/me")
@Validated
public class PerfilUsuarioController {
    private final GerenciarPerfilUseCase useCase;
    public PerfilUsuarioController(GerenciarPerfilUseCase useCase) { this.useCase = useCase; }
    @GetMapping Response consultar(@AuthenticationPrincipal Jwt jwt) { return Response.from(useCase.consultar(id(jwt))); }
    @PatchMapping Response atualizar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Request request) { return Response.from(useCase.atualizar(id(jwt), new GerenciarPerfilUseCase.AtualizarCommand(request.nome(), request.email()))); }
    @DeleteMapping @ResponseStatus(HttpStatus.NO_CONTENT) void desativar(@AuthenticationPrincipal Jwt jwt) { useCase.desativar(id(jwt)); }
    private Long id(Jwt jwt) { return Long.valueOf(jwt.getSubject()); }
    record Request(@NotBlank @Size(max = 150) String nome, @NotBlank @Email String email) { }
    record Response(Long id, String nome, String email, boolean ativo) { static Response from(Usuario usuario) { return new Response(usuario.getId(), usuario.getNome(), usuario.getEmail().valor(), usuario.isAtivo()); } }
}
