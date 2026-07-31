package com.financeiro.application.ports.out;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.ParcelaFinanciamento;
import com.financeiro.domain.vo.AnoMes;

import java.time.LocalDate;
import java.util.List;

public interface ParcelaFinanciamentoRepositoryPort {
	void salvarNovas(Long financiamentoId, List<ParcelaFinanciamento> parcelas);

	List<ParcelaFinanciamento> listarPorFinanciamento(Long financiamentoId);

	Pagina<ParcelaFinanciamento> listarPorFinanciamento(Long financiamentoId, Paginacao paginacao);

	ParcelaFinanciamento salvar(ParcelaFinanciamento parcela);

	void excluir(Long parcelaId);

	int excluirPendentesAPartirDe(Long financiamentoId, int numeroParcela);

	List<ParcelaFinanciamento> listarVencidas(LocalDate dataReferencia);

	List<ParcelaFinanciamento> listarPorUsuarioEPeriodo(Long usuarioId, AnoMes inicio, AnoMes fim);

	boolean existePagaPorFinanciamento(Long financiamentoId);
}
