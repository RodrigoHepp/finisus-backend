package com.finisus.application.ports.in;

import com.finisus.domain.model.Transacao;

public interface RegistrarTransacaoUseCase {
	Transacao registrar(Long usuarioId, TransacaoUseCase.RegistrarCommand command);
}
