package com.financeiro.application.ports.out;

import com.financeiro.domain.model.Recorrencia;
import com.financeiro.domain.vo.AnoMes;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;

import java.util.List;
import java.util.Optional;

public interface RecorrenciaRepositoryPort {

    Recorrencia salvar(Recorrencia recorrencia);

    Optional<Recorrencia> buscarPorId(Long id);

    Optional<Recorrencia> buscarPorIdEUsuario(Long id, Long usuarioId);

    List<Recorrencia> listarPorUsuario(Long usuarioId);
    Pagina<Recorrencia> listarPorUsuario(Long usuarioId, Paginacao paginacao);

    List<Recorrencia> listarAtivasPorUsuario(Long usuarioId);

    void registrarGeracao(Long recorrenciaId, AnoMes anoMes, Long transacaoId);

    boolean existsGeracaoPorRecorrenciaEAnoMes(Long recorrenciaId, AnoMes anoMes);
}
