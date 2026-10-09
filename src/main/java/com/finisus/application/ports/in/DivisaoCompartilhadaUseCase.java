package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.DivisaoCompartilhada;
import com.finisus.domain.model.HistoricoParticipanteDivisao;

import java.math.BigDecimal;
import java.util.List;

public interface DivisaoCompartilhadaUseCase {
    DivisaoCompartilhada criar(Long usuarioId, CriarCommand command);
    Pagina<DivisaoCompartilhada> listar(Long usuarioId, Paginacao paginacao);
    DivisaoCompartilhada buscar(Long usuarioId, Long divisaoId);
    DivisaoCompartilhada atualizarParticipantes(Long usuarioId, Long divisaoId, List<ParticipanteCommand> participantes);
    DivisaoCompartilhada inativar(Long usuarioId, Long divisaoId);
    List<HistoricoParticipanteDivisao> listarHistoricoParticipantes(Long usuarioId, Long divisaoId);

    record CriarCommand(String nome, List<ParticipanteCommand> participantes) { }
    record ParticipanteCommand(Long usuarioId, BigDecimal percentual) { }
}
