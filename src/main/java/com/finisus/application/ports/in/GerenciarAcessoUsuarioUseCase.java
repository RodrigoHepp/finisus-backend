package com.finisus.application.ports.in;

public interface GerenciarAcessoUsuarioUseCase {

	void desbloquear(Long usuarioSolicitanteId, Long usuarioId);
}
