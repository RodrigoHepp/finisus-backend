package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.Financiamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinanciamentoUseCase {
	Financiamento criar(Long usuarioId, CriarCommand command);

	Financiamento buscar(Long usuarioId, Long financiamentoId);

	List<Financiamento> listar(Long usuarioId);

	Pagina<Financiamento> listar(Long usuarioId, Paginacao paginacao);

	RefinanciamentoResult excluirPorRefinanciamento(Long usuarioId, Long financiamentoId, Long parcelaId);

	int excluirPorErroDeLancamento(Long usuarioId, Long financiamentoId, Long parcelaId);

	Financiamento cancelar(Long usuarioId, Long financiamentoId);

	record CriarCommand(String descricao, BigDecimal principal, BigDecimal taxaJurosMensal, int numeroParcelas,
			LocalDate dataInicio, Long contaId) {
	}

	record RefinanciamentoResult(Financiamento financiamento, int parcelasExcluidas) {
	}
}
