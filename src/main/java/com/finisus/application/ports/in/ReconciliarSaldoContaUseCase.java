package com.finisus.application.ports.in;

import java.math.BigDecimal;

public interface ReconciliarSaldoContaUseCase {
	ReconciliacaoSaldo reconciliar(Long usuarioId, Long contaId);

	record ReconciliacaoSaldo(Long contaId, BigDecimal saldoMaterializado, BigDecimal saldoCalculado,
			BigDecimal divergencia, long quantidadeMovimentos, long quantidadeAjustes, boolean conciliado) { }
}
