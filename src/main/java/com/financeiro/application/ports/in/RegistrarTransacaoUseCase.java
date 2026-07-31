package com.financeiro.application.ports.in;

import com.financeiro.domain.model.Transacao;

public interface RegistrarTransacaoUseCase {
	Transacao registrar(Long usuarioId, TransacaoUseCase.RegistrarCommand command);
}
