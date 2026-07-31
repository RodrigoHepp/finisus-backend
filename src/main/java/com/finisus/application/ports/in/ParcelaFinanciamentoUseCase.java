package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.ParcelaFinanciamento;

import java.util.List;

public interface ParcelaFinanciamentoUseCase extends PagarParcelaFinanciamentoUseCase, ProcessarAtrasosParcelasUseCase {
	List<ParcelaFinanciamento> listar(Long usuarioId, Long financiamentoId);

	Pagina<ParcelaFinanciamento> listar(Long usuarioId, Long financiamentoId, Paginacao paginacao);
}
