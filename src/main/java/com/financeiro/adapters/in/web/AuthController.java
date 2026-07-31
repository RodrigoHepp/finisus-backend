package com.financeiro.adapters.in.web;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.application.ports.in.AutenticarUsuarioUseCase;
import com.financeiro.application.ports.in.CadastrarUsuarioUseCase;
import com.financeiro.application.ports.in.RenovarTokenUseCase;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Tag(name = "Autenticação", description = "Cadastro, login e renovação de tokens JWT.")
public class AuthController {
	private final CadastrarUsuarioUseCase cadastrar;
	private final AutenticarUsuarioUseCase autenticar;
	private final RenovarTokenUseCase renovar;

	public AuthController(CadastrarUsuarioUseCase cadastrar, AutenticarUsuarioUseCase autenticar,
			RenovarTokenUseCase renovar) {
		this.cadastrar = cadastrar;
		this.autenticar = autenticar;
		this.renovar = renovar;
	}

	@PostMapping("/cadastro")
	@ResponseStatus(HttpStatus.CREATED)
	UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest request) {
		var result = cadastrar
				.executar(new CadastrarUsuarioUseCase.Command(request.nome(), request.email(), request.senha()));
		return new UsuarioResponse(result.id(), result.nome(), result.email());
	}

	@PostMapping("/login")
	TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return tokens(autenticar.executar(new AutenticarUsuarioUseCase.Command(request.email(), request.senha())));
	}

	@PostMapping("/refresh")
	TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
		var result = renovar.executar(new RenovarTokenUseCase.Command(request.refreshToken()));
		return new TokenResponse(result.accessToken(), result.refreshToken(), result.accessTokenExpiraEm());
	}

	private TokenResponse tokens(AutenticarUsuarioUseCase.Result result) {
		return new TokenResponse(result.accessToken(), result.refreshToken(), result.accessTokenExpiraEm());
	}

	record CadastroRequest(@NotBlank @Size(max = 150) String nome, @NotBlank @Email String email,
			@NotBlank @Size(min = 8, max = 128) String senha) {
	}

	record LoginRequest(@NotBlank @Email String email, @NotBlank String senha) {
	}

	record RefreshRequest(@NotBlank String refreshToken) {
	}

	record UsuarioResponse(Long id, String nome, String email) {
	}

	record TokenResponse(String accessToken, String refreshToken, Instant expiraEm) {
	}
}
