package com.finisus.application.ports.in;

import com.finisus.domain.model.Financiamento;
import com.finisus.domain.model.ParcelaFinanciamento;
import com.finisus.domain.model.ModalidadeAmortizacaoFinanciamento;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Porta de aplicação usada pelo financiamento para delegar a manutenção de seu
 * plano de parcelas.
 */
public interface GerenciarParcelasFinanciamentoUseCase {
	void gerar(Financiamento financiamento);

	int excluirPendentesAPartirDe(Financiamento financiamento, Long parcelaId);

	List<ParcelaFinanciamento> excluirERecalcular(Financiamento financiamento, Long parcelaId);

	AmortizacaoCronograma amortizar(Long usuarioId, Financiamento financiamento, BigDecimal valor,
			LocalDate dataPagamento, Integer numeroParcelasRestantes,
			ModalidadeAmortizacaoFinanciamento modalidade);

	record AmortizacaoCronograma(Long transacaoId, BigDecimal saldoDevedorAnterior, BigDecimal saldoDevedorAtual,
			int parcelasPagasPreservadas, int parcelasRestantes) { }
}
