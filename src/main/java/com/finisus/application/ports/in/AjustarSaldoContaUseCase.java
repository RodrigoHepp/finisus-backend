package com.finisus.application.ports.in;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.finisus.domain.model.AjusteSaldoConta;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;

public interface AjustarSaldoContaUseCase {
	AjusteSaldoConta ajustar(Long usuarioId, Long contaId, String chaveIdempotencia, AjustarCommand command);
	Pagina<AjusteSaldoConta> listar(Long usuarioId, Long contaId, Paginacao paginacao);

	record AjustarCommand(BigDecimal saldoInformado, String motivo) { }

	record Resultado(Long id, Long contaId, BigDecimal saldoAnterior, BigDecimal saldoCalculadoAnterior,
			BigDecimal saldoInformado, BigDecimal valorAjuste, String motivo, LocalDate dataAjuste) {
		public static Resultado from(AjusteSaldoConta ajuste) {
			return new Resultado(ajuste.id(), ajuste.contaId(), ajuste.saldoAnterior(),
					ajuste.saldoCalculadoAnterior(), ajuste.saldoInformado(), ajuste.valorAjuste(), ajuste.motivo(),
					ajuste.dataAjuste());
		}
	}
}
