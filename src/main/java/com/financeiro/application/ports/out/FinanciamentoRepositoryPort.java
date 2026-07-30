package com.financeiro.application.ports.out;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Financiamento;
import com.financeiro.domain.model.ParcelaFinanciamento;
import com.financeiro.domain.vo.AnoMes;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface FinanciamentoRepositoryPort {
    Financiamento salvarComParcelas(Financiamento financiamento, List<ParcelaFinanciamento> parcelas);
    Optional<Financiamento> buscarPorIdEUsuario(Long id, Long usuarioId);
    List<Financiamento> listarPorUsuario(Long usuarioId);
    Pagina<Financiamento> listarPorUsuario(Long usuarioId, Paginacao paginacao);
    List<ParcelaFinanciamento> listarParcelas(Long financiamentoId);
    Pagina<ParcelaFinanciamento> listarParcelas(Long financiamentoId, Paginacao paginacao);
    ParcelaFinanciamento salvarParcela(ParcelaFinanciamento parcela);
    List<ParcelaFinanciamento> listarParcelasPorUsuarioEPeriodo(Long usuarioId, AnoMes inicio, AnoMes fim);
    List<ParcelaFinanciamento> listarParcelasVencidas(LocalDate dataReferencia);
}
