package com.finisus.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RevisaoLancamentoImportado(Long id, Long revisadoPor, DecisaoRevisaoImportacao decisao,
		String motivoIncerteza, String justificativa, Estado anterior, Estado novo, LocalDateTime revisadoEm) {

	public record Estado(LocalDate data, String descricao, BigDecimal valor, TipoTransacao tipo, boolean importar,
			Long categoriaId, Long itemId, Long transacaoId, Long obrigacaoFinanceiraId) { }
}
