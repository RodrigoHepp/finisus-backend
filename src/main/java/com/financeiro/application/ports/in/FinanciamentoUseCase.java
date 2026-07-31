package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Financiamento;

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
