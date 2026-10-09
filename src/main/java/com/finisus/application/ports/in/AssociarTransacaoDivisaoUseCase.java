package com.finisus.application.ports.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.finisus.domain.model.AlocacaoPagamentoDivisao;
import com.finisus.domain.model.ResumoPagamentoDivisao;
import com.finisus.domain.model.ReembolsoDivisao;

public interface AssociarTransacaoDivisaoUseCase {
    void associar(Long usuarioId, Long divisaoId, Long transacaoId);
    void associar(Long usuarioId, Long divisaoId, Long transacaoId, BigDecimal baseCompartilhada,
            List<ResponsabilidadeCommand> responsabilidades);
    void desassociar(Long usuarioId, Long divisaoId, Long transacaoId);
    List<VinculoPendente> listarPendentes(Long usuarioId, Long divisaoId);
    void revisar(Long usuarioId, Long divisaoId, Long transacaoId, List<ResponsabilidadeCommand> responsabilidades);
    List<AlocacaoPagamentoDivisao> listarAlocacoes(Long usuarioId, Long divisaoId, Long transacaoId);
    List<AlocacaoPagamentoDivisao> substituirAlocacoes(Long usuarioId, Long divisaoId, Long transacaoId,
            List<AlocacaoCommand> alocacoes);
    ResumoPagamentoDivisao consultarPagamento(Long usuarioId, Long divisaoId, Long transacaoId);
    void cancelarAlocacao(Long usuarioId, Long divisaoId, Long transacaoId, Long alocacaoId);
    ReembolsoDivisao registrarReembolso(Long usuarioId, Long divisaoId, Long transacaoId, Long recebedorId,
            BigDecimal valor);
    void cancelarReembolso(Long usuarioId, Long divisaoId, Long reembolsoId);
    List<ReembolsoDivisao> listarReembolsos(Long usuarioId, Long divisaoId);

    record ResponsabilidadeCommand(Long usuarioId, BigDecimal percentual, BigDecimal valorDevido) { }
    record AlocacaoCommand(Long transacaoId, BigDecimal valor) { }
    record VinculoPendente(Long transacaoId, Long pagadorId, LocalDate data, String descricao, BigDecimal valor) { }
}
