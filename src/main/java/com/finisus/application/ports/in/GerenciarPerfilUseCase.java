package com.finisus.application.ports.in;

import com.finisus.domain.model.Usuario;

public interface GerenciarPerfilUseCase {
	Usuario consultar(Long usuarioId);

	Usuario atualizar(Long usuarioId, AtualizarCommand command);

	void desativar(Long usuarioId);

	record AtualizarCommand(String nome, String email) {
	}
}
