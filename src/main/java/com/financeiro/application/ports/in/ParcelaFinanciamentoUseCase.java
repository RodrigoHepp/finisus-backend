package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.ParcelaFinanciamento;

import java.util.List;

public interface ParcelaFinanciamentoUseCase extends PagarParcelaFinanciamentoUseCase, ProcessarAtrasosParcelasUseCase {
	List<ParcelaFinanciamento> listar(Long usuarioId, Long financiamentoId);

	Pagina<ParcelaFinanciamento> listar(Long usuarioId, Long financiamentoId, Paginacao paginacao);
}
