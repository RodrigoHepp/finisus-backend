package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.ConfiguracaoCompartilhamento;
import com.financeiro.domain.model.DespesaCompartilhada;
import com.financeiro.domain.model.RateioDespesa;
import com.financeiro.domain.model.TipoRateio;

import java.math.BigDecimal;
import java.util.List;

public interface CompartilhamentoUseCase {
    ConfiguracaoCompartilhamento atualizarOptIn(Long usuarioId, boolean aceita);
    ConfiguracaoCompartilhamento consultarOptIn(Long usuarioId);
    Resultado criarDespesa(Long usuarioId, CriarCommand command);
    List<DespesaCompartilhada> listarDespesas(Long usuarioId);
    Pagina<DespesaCompartilhada> listarDespesas(Long usuarioId, Paginacao paginacao);
    List<RateioDespesa> listarRateios(Long usuarioId, Long despesaId);
    Pagina<RateioDespesa> listarRateios(Long usuarioId, Long despesaId, Paginacao paginacao);
    RateioDespesa responderRateio(Long usuarioId, Long rateioId, boolean aceita);
    RateioDespesa marcarRateioPago(Long usuarioId, Long rateioId);

    record CriarCommand(Long transacaoId, TipoRateio tipoRateio, List<ParticipanteCommand> participantes) {}
    record ParticipanteCommand(Long usuarioId, String nomeExterno, String emailExterno, BigDecimal valorFixo, BigDecimal percentual) {}
    record Resultado(DespesaCompartilhada despesa, List<RateioDespesa> rateios) {}
}
