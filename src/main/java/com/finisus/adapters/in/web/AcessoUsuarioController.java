package com.finisus.adapters.in.web;

import com.finisus.adapters.in.web.security.UsuarioAtual;
import com.finisus.application.ports.in.GerenciarAcessoUsuarioUseCase;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@Validated
@Tag(name = "Acesso de usuários", description = "Administração de bloqueios de autenticação.")
@SecurityRequirement(name = "bearerAuth")
public class AcessoUsuarioController {
	private final GerenciarAcessoUsuarioUseCase useCase;

	public AcessoUsuarioController(GerenciarAcessoUsuarioUseCase useCase) {
		this.useCase = useCase;
	}

	@PostMapping("/{usuarioId}/desbloquear")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void desbloquear(@UsuarioAtual Long usuarioSolicitanteId, @PathVariable @Positive Long usuarioId) {
		useCase.desbloquear(usuarioSolicitanteId, usuarioId);
	}
}
