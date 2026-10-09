package com.finisus.adapters.in.web;

import org.springframework.http.HttpStatus;
import com.finisus.adapters.in.web.security.UsuarioAtual;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finisus.application.ports.in.GerenciarPerfilUseCase;
import com.finisus.domain.model.Usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
	@Operation(summary = "Desativa o usuário autenticado",
			description = "Inativa a conta, revoga as sessões existentes e preserva os dados financeiros e o histórico compartilhado. Não realiza exclusão definitiva nem anonimização.")
	@ApiResponse(responseCode = "204", description = "Conta desativada e sessões revogadas.")
	void desativar(@UsuarioAtual Long usuarioId) {
		useCase.desativar(usuarioId);
	}

	@GetMapping("/dados")
	@Operation(summary = "Exporta os dados do titular",
			description = "Retorna um JSON portátil, versionado e restrito ao usuário autenticado. Não inclui senha nem tokens.")
	ExportacaoResponse exportarDados(@UsuarioAtual Long usuarioId) {
		return ExportacaoResponse.from(useCase.exportarDados(usuarioId));
	}

	@PostMapping("/solicitacoes-anonimizacao")
	@ResponseStatus(HttpStatus.CREATED)
	SolicitacaoResponse solicitarAnonimizacao(@UsuarioAtual Long usuarioId,
			@Valid @RequestBody SolicitacaoRequest request) {
		return SolicitacaoResponse.from(useCase.solicitarAnonimizacao(usuarioId, request.motivo()));
	}

	@GetMapping("/solicitacoes-privacidade")
	List<SolicitacaoResponse> listarSolicitacoes(@UsuarioAtual Long usuarioId) {
		return useCase.listarSolicitacoesPrivacidade(usuarioId).stream().map(SolicitacaoResponse::from).toList();
	}

	record Request(@NotBlank @Size(max = 150) String nome, @NotBlank @Email String email) {
	}

	record Response(Long id, String nome, String email, boolean ativo) {
		static Response from(Usuario usuario) {
			return new Response(usuario.getId(), usuario.getNome(), usuario.getEmail().valor(), usuario.isAtivo());
		}
	}

	record SolicitacaoRequest(@NotBlank @Size(max = 500) String motivo) { }

	record ExportacaoResponse(int versaoFormato, Instant geradaEm,
			Map<String, List<Map<String, Object>>> secoes) {
		static ExportacaoResponse from(GerenciarPerfilUseCase.ExportacaoDados exportacao) {
			return new ExportacaoResponse(exportacao.versaoFormato(), exportacao.geradaEm(), exportacao.secoes());
		}
	}

	record SolicitacaoResponse(Long id, String tipo, String status, String motivo, LocalDateTime solicitadaEm,
			LocalDateTime concluidaEm, String observacao) {
		static SolicitacaoResponse from(com.finisus.domain.model.SolicitacaoPrivacidade solicitacao) {
			return new SolicitacaoResponse(solicitacao.id(), solicitacao.tipo().name(), solicitacao.status().name(),
					solicitacao.motivo(), solicitacao.solicitadaEm(), solicitacao.concluidaEm(), solicitacao.observacao());
		}
	}

}
