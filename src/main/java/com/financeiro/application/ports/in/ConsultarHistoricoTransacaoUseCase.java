package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.TransacaoHistorico;

import java.util.List;

public interface ConsultarHistoricoTransacaoUseCase {
	List<TransacaoHistorico> listar(Long usuarioId, Long transacaoId);

	Pagina<TransacaoHistorico> listar(Long usuarioId, Long transacaoId, Paginacao paginacao);
}
