package com.finisus.domain.model;

import com.finisus.domain.vo.ValorMonetario;
import java.time.LocalDate;

public record OcorrenciaRecorrencia(Long id, Long recorrenciaId, Long usuarioId, String anoMes, LocalDate vencimento,
		TipoTransacao tipo, ValorMonetario valor, String descricao, Long contaId, Long categoriaId,
		Long meioPagamentoId, StatusOcorrenciaRecorrencia status, Long transacaoId) {

	public static OcorrenciaRecorrencia pendente(Recorrencia recorrencia, String anoMes, LocalDate vencimento) {
		return new OcorrenciaRecorrencia(null, recorrencia.getId(), recorrencia.getUsuarioId(), anoMes, vencimento,
				recorrencia.getTipo(), recorrencia.getValorEsperado(), recorrencia.getNome(), recorrencia.getContaId(),
				recorrencia.getCategoriaId(), recorrencia.getMeioPagamentoId(), StatusOcorrenciaRecorrencia.PENDENTE, null);
	}

	public OcorrenciaRecorrencia realizada(Long transacaoId) {
		return new OcorrenciaRecorrencia(id, recorrenciaId, usuarioId, anoMes, vencimento, tipo, valor, descricao,
				contaId, categoriaId, meioPagamentoId, StatusOcorrenciaRecorrencia.REALIZADA, transacaoId);
	}
}
