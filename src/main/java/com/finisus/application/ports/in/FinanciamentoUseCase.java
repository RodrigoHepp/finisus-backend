package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.Financiamento;
import com.finisus.domain.model.ModalidadeAmortizacaoFinanciamento;
import com.finisus.application.ports.out.ParcelaFinanciamentoRepositoryPort.ParcelaHistorica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinanciamentoUseCase {
	Financiamento criar(Long usuarioId, CriarCommand command);

	Financiamento buscar(Long usuarioId, Long financiamentoId);

	List<Financiamento> listar(Long usuarioId);

	Pagina<Financiamento> listar(Long usuarioId, Paginacao paginacao);

	RefinanciamentoResult excluirPorRefinanciamento(Long usuarioId, Long financiamentoId, Long parcelaId);

	RefinanciamentoCompletoResult refinanciar(Long usuarioId, Long financiamentoId, RefinanciarCommand command);

	int excluirPorErroDeLancamento(Long usuarioId, Long financiamentoId, Long parcelaId);

	Financiamento cancelar(Long usuarioId, Long financiamentoId);

	AmortizacaoResult amortizar(Long usuarioId, Long financiamentoId, AmortizarCommand command);

	List<ParcelaHistorica> consultarCronogramaHistorico(Long usuarioId, Long financiamentoId, int versao);

	record CriarCommand(String descricao, BigDecimal principal, BigDecimal taxaJurosMensal, int numeroParcelas,
			LocalDate dataInicio, Long contaId) {
	}

	record RefinanciamentoResult(Financiamento financiamento, int parcelasExcluidas) {
	}

	record RefinanciarCommand(Long parcelaId, CriarCommand novoFinanciamento) { }

	record RefinanciamentoCompletoResult(Financiamento financiamentoOrigem, Financiamento novoFinanciamento,
			int parcelasExcluidas) { }

	record AmortizarCommand(BigDecimal valor, LocalDate dataPagamento, Integer numeroParcelasRestantes,
			ModalidadeAmortizacaoFinanciamento modalidade) {
		public AmortizarCommand(BigDecimal valor, LocalDate dataPagamento, int numeroParcelasRestantes) {
			this(valor, dataPagamento, numeroParcelasRestantes, null);
		}
	}

	record AmortizacaoResult(Financiamento financiamento, Long transacaoId, BigDecimal saldoDevedorAnterior,
			BigDecimal saldoDevedorAtual, int parcelasRestantes) { }
}
