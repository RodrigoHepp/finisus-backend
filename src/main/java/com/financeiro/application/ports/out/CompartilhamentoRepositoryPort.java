package com.financeiro.application.ports.out;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.ConfiguracaoCompartilhamento;
import com.financeiro.domain.model.DespesaCompartilhada;
import com.financeiro.domain.model.RateioDespesa;

import java.util.List;
import java.util.Optional;

public interface CompartilhamentoRepositoryPort {
    ConfiguracaoCompartilhamento salvarConfiguracao(ConfiguracaoCompartilhamento configuracao);
    Optional<ConfiguracaoCompartilhamento> buscarConfiguracao(Long usuarioId);
    DespesaCompartilhada salvarDespesa(DespesaCompartilhada despesa);
    Optional<DespesaCompartilhada> buscarDespesaPorIdECriador(Long despesaId, Long criadorId);
    List<DespesaCompartilhada> listarDespesasPorCriador(Long criadorId);
    Pagina<DespesaCompartilhada> listarDespesasPorCriador(Long criadorId, Paginacao paginacao);
    List<RateioDespesa> salvarRateios(Long despesaId, List<RateioDespesa> rateios);
    Optional<RateioDespesa> buscarRateio(Long rateioId);
    RateioDespesa salvarRateio(RateioDespesa rateio);
    List<RateioDespesa> listarRateios(Long despesaId);
    Pagina<RateioDespesa> listarRateios(Long despesaId, Paginacao paginacao);
}
