package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.ParcelaFinanciamento;
import com.finisus.domain.model.StatusParcelaFinanciamento;
import com.finisus.domain.vo.AnoMes;

import java.time.LocalDate;
import java.math.BigDecimal;
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

	List<ParcelaFinanciamento> listarPendentesPorUsuario(Long usuarioId);

	boolean existePagaPorFinanciamento(Long financiamentoId);
	boolean existePorTransacao(Long transacaoId);

	void registrarSnapshotCronograma(Long financiamentoId, int cronogramaVersao);

	List<ParcelaHistorica> listarCronogramaHistorico(Long financiamentoId, int cronogramaVersao);

	record ParcelaHistorica(Long parcelaIdOrigem, int numero, BigDecimal valor, BigDecimal principal,
			BigDecimal juros, BigDecimal encargos, BigDecimal saldoDevedorInicial, BigDecimal saldoDevedorFinal,
			LocalDate vencimento, StatusParcelaFinanciamento status, Long transacaoId) { }
}
