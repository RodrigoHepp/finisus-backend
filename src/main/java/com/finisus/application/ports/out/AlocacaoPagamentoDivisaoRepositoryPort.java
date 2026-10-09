package com.finisus.application.ports.out;

import com.finisus.domain.model.AlocacaoPagamentoDivisao;
import com.finisus.domain.vo.ValorMonetario;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface AlocacaoPagamentoDivisaoRepositoryPort {
    AlocacaoPagamentoDivisao criarInicial(Long divisaoId, Long transacaoId, Long pagadorId,
            ValorMonetario valor);
    List<AlocacaoPagamentoDivisao> listar(Long divisaoId, Long transacaoDespesaId);
    List<AlocacaoPagamentoDivisao> listarAtivas(Long divisaoId, Long transacaoDespesaId);
    List<AlocacaoPagamentoDivisao> listarAtivas(Long divisaoId, LocalDate inicio, LocalDate fim);
    Optional<ValorMonetario> buscarBaseCompartilhadaAtiva(Long divisaoId, Long transacaoDespesaId);
    Optional<ValorMonetario> buscarBaseCompartilhadaAtivaParaAtualizacao(Long divisaoId, Long transacaoDespesaId);
    BigDecimal totalAtivoPorTransacaoExcluindoVinculo(Long transacaoId, Long divisaoId, Long transacaoDespesaId);
    List<AlocacaoPagamentoDivisao> substituir(Long divisaoId, Long transacaoDespesaId, Long canceladoPor,
            List<NovaAlocacao> novasAlocacoes);
    boolean cancelar(Long divisaoId, Long transacaoDespesaId, Long alocacaoId, Long canceladoPor);

    record NovaAlocacao(Long transacaoId, Long pagadorId, ValorMonetario valor) { }
}
