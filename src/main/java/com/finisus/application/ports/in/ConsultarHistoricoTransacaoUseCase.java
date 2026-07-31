package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.TransacaoHistorico;

import java.util.List;

public interface ConsultarHistoricoTransacaoUseCase {
	List<TransacaoHistorico> listar(Long usuarioId, Long transacaoId);

	Pagina<TransacaoHistorico> listar(Long usuarioId, Long transacaoId, Paginacao paginacao);
}
