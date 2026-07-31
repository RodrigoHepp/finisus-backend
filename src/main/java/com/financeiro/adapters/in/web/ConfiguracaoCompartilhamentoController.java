package com.financeiro.adapters.in.web;

import com.financeiro.application.ports.in.ConfiguracaoCompartilhamentoUseCase;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.financeiro.adapters.in.web.security.UsuarioAtual;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/compartilhamentos/opt-in")
@Tag(name = "Compartilhamentos", description = "Despesas compartilhadas, rateios e opt-in.")
@SecurityRequirement(name = "bearerAuth")
public class ConfiguracaoCompartilhamentoController {
	private final ConfiguracaoCompartilhamentoUseCase useCase;

	public ConfiguracaoCompartilhamentoController(ConfiguracaoCompartilhamentoUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping
	ConfigResponse consultar(@UsuarioAtual Long usuarioId) {
		return new ConfigResponse(useCase.consultarOptIn(usuarioId).isAceitaCompartilhamento());
	}

	@PutMapping
	ConfigResponse atualizar(@UsuarioAtual Long usuarioId, @Valid @RequestBody OptInRequest request) {
		return new ConfigResponse(useCase.atualizarOptIn(usuarioId, request.aceita()).isAceitaCompartilhamento());
	}

	record OptInRequest(boolean aceita) {
	}

	record ConfigResponse(boolean aceita) {
	}
}
