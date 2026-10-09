package com.finisus.application.ports.in;

public interface CadastrarUsuarioUseCase {

	Result executar(Long usuarioSolicitanteId, Command command);

	record Command(String nome, String email, String senha) {
	}

	record Result(Long id, String nome, String email) {
	}
}
