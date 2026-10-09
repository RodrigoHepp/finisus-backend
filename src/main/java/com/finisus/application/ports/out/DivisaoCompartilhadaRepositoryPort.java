package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.DivisaoCompartilhada;
import com.finisus.domain.model.HistoricoParticipanteDivisao;

import java.util.List;
import java.util.Optional;

public interface DivisaoCompartilhadaRepositoryPort {
    DivisaoCompartilhada salvar(DivisaoCompartilhada divisao);
    Optional<DivisaoCompartilhada> buscarPorId(Long divisaoId);
    Optional<DivisaoCompartilhada> buscarPorIdParaAtualizacao(Long divisaoId);
    Pagina<DivisaoCompartilhada> listarPorParticipante(Long usuarioId, Paginacao paginacao);
    List<DivisaoCompartilhada> listarAtivasPorParticipante(Long usuarioId);
    List<HistoricoParticipanteDivisao> listarHistoricoParticipantes(Long divisaoId);
}
