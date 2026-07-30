package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.PrevisaoMensal;

import java.util.List;

public interface PrevisaoFluxoCaixaUseCase {
    List<PrevisaoMensal> recalcular(Long usuarioId, int meses);
    List<PrevisaoMensal> consultar(Long usuarioId, String anoMes);
    Pagina<PrevisaoMensal> consultar(Long usuarioId, String anoMes, Paginacao paginacao);
}
