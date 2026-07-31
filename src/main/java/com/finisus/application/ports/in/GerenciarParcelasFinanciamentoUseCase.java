package com.finisus.application.ports.in;

import com.finisus.domain.model.Financiamento;
import com.finisus.domain.model.ParcelaFinanciamento;

import java.util.List;

/**
 * Porta de aplicação usada pelo financiamento para delegar a manutenção de seu
 * plano de parcelas.
 */
public interface GerenciarParcelasFinanciamentoUseCase {
	void gerar(Financiamento financiamento);

	int excluirPendentesAPartirDe(Financiamento financiamento, Long parcelaId);

	List<ParcelaFinanciamento> excluirERecalcular(Financiamento financiamento, Long parcelaId);
}
