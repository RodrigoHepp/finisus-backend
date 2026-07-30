package com.financeiro.application.ports.out;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.PrevisaoMensal;
import com.financeiro.domain.vo.AnoMes;

import java.util.List;

public interface PrevisaoMensalRepositoryPort {
    void substituir(Long usuarioId, AnoMes anoMes, List<PrevisaoMensal> previsoes);
    List<PrevisaoMensal> listar(Long usuarioId, AnoMes anoMes);
    Pagina<PrevisaoMensal> listar(Long usuarioId, AnoMes anoMes, Paginacao paginacao);
}
