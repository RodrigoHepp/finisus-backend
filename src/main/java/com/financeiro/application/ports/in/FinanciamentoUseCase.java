package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Financiamento;
import com.financeiro.domain.model.ParcelaFinanciamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinanciamentoUseCase extends PagarParcelaFinanciamentoUseCase, ProcessarAtrasosParcelasUseCase {
    Financiamento criar(Long usuarioId, CriarCommand command);
    Financiamento buscar(Long usuarioId, Long financiamentoId);
    List<Financiamento> listar(Long usuarioId);
    Pagina<Financiamento> listar(Long usuarioId, Paginacao paginacao);
    List<ParcelaFinanciamento> listarParcelas(Long usuarioId, Long financiamentoId);
    Pagina<ParcelaFinanciamento> listarParcelas(Long usuarioId, Long financiamentoId, Paginacao paginacao);

    record CriarCommand(String descricao, BigDecimal principal, BigDecimal taxaJurosMensal, int numeroParcelas,
                        LocalDate dataInicio, Long contaId) {}
}
