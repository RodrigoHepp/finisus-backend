package com.financeiro.adapters.in.web;

import org.springframework.http.HttpStatus;
import com.financeiro.adapters.in.web.security.UsuarioAtual;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.application.ports.in.GerenciarPerfilUseCase;
import com.financeiro.domain.model.Usuario;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/usuarios/me")
@Validated
@Tag(name = "Perfil", description = "Consulta, atualização e desativação do usuário autenticado.")
@SecurityRequirement(name = "bearerAuth")
public class PerfilUsuarioController {
	private final GerenciarPerfilUseCase useCase;

	public PerfilUsuarioController(GerenciarPerfilUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping
	Response consultar(@UsuarioAtual Long usuarioId) {
		return Response.from(useCase.consultar(usuarioId));
	}

	@PatchMapping
	Response atualizar(@UsuarioAtual Long usuarioId, @Valid @RequestBody Request request) {
		return Response.from(useCase.atualizar(usuarioId,
				new GerenciarPerfilUseCase.AtualizarCommand(request.nome(), request.email())));
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void desativar(@UsuarioAtual Long usuarioId) {
		useCase.desativar(usuarioId);
	}

	record Request(@NotBlank @Size(max = 150) String nome, @NotBlank @Email String email) {
	}

	record Response(Long id, String nome, String email, boolean ativo) {
		static Response from(Usuario usuario) {
			return new Response(usuario.getId(), usuario.getNome(), usuario.getEmail().valor(), usuario.isAtivo());
		}
	}
}
