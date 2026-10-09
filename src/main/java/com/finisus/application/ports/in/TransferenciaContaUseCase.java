package com.finisus.application.ports.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.finisus.domain.model.TransferenciaConta;

public interface TransferenciaContaUseCase {
	TransferenciaConta transferir(Long usuarioId, String chaveIdempotencia, CriarCommand command);
	TransferenciaConta buscar(Long usuarioId, Long transferenciaId);
	TransferenciaConta estornar(Long usuarioId, Long transferenciaId);

	record CriarCommand(Long contaOrigemId, Long contaDestinoId, BigDecimal valor, LocalDate data, String descricao) { }
}
