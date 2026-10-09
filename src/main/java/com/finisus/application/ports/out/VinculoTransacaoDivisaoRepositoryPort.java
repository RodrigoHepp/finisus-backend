package com.finisus.application.ports.out;

import com.finisus.domain.vo.ValorMonetario;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;
import com.finisus.domain.model.StatusSnapshotDivisao;

public interface VinculoTransacaoDivisaoRepositoryPort {
    void associar(Long divisaoId, Long transacaoId, List<Responsabilidade> responsabilidades);
    void associar(Long divisaoId, Long transacaoId, List<Responsabilidade> responsabilidades,
            ValorMonetario baseCompartilhada);
    boolean existePorTransacaoId(Long transacaoId);
    boolean cancelar(Long divisaoId, Long transacaoId, Long canceladoPor);
    List<LancamentoDivisao> listarLancamentosAtivos(Long divisaoId, LocalDate inicio, LocalDate fim);
    List<VinculoPendente> listarPendentes(Long divisaoId);
    Optional<StatusSnapshotDivisao> buscarStatusParaAtualizacao(Long divisaoId, Long transacaoId);
    void substituirResponsabilidades(Long divisaoId, Long transacaoId, List<Responsabilidade> responsabilidades);

    record Responsabilidade(Long usuarioId, BigDecimal percentual, ValorMonetario valorDevido) { }
    record LancamentoDivisao(Long usuarioId, ValorMonetario valor, List<Responsabilidade> responsabilidades,
            boolean pendenteRevisao) { }
    record VinculoPendente(Long transacaoId, Long pagadorId, LocalDate data, String descricao,
            ValorMonetario valor) { }
}
