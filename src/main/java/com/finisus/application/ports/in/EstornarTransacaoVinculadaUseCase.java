package com.finisus.application.ports.in;

import com.finisus.domain.model.Transacao;

public interface EstornarTransacaoVinculadaUseCase {
	Transacao estornarVinculada(Long usuarioId, Long transacaoId);
}
