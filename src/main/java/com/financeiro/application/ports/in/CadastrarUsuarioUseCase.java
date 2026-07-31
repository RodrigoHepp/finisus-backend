package com.financeiro.application.ports.in;

public interface CadastrarUsuarioUseCase {

	Result executar(Command command);

	record Command(String nome, String email, String senha) {
	}

	record Result(Long id, String nome, String email) {
	}
}
