package com.financeiro.domain.model;

import com.financeiro.domain.vo.ValorMonetario;

import java.time.LocalDate;

public class MovimentoInvestimento {

    private final Long id;
    private final Long investimentoId;
    private final TipoMovimentoInvestimento tipo;
    private final ValorMonetario valor;
    private final LocalDate data;
    private final Long transacaoId;

    private MovimentoInvestimento(Long id, Long investimentoId, TipoMovimentoInvestimento tipo,
                                  ValorMonetario valor, LocalDate data, Long transacaoId) {
        this.id = id;
        this.investimentoId = investimentoId;
        this.tipo = tipo;
        this.valor = valor;
        this.data = data;
        this.transacaoId = transacaoId;
    }

    public static MovimentoInvestimento novo(Long investimentoId, TipoMovimentoInvestimento tipo,
                                             ValorMonetario valor, LocalDate data) {
        return new MovimentoInvestimento(null, investimentoId, tipo, valor, data, null);
    }

    public static MovimentoInvestimento reconstituir(Long id, Long investimentoId,
                                                     TipoMovimentoInvestimento tipo,
                                                     ValorMonetario valor, LocalDate data,
                                                     Long transacaoId) {
        return new MovimentoInvestimento(id, investimentoId, tipo, valor, data, transacaoId);
    }

    public Long getId() { return id; }
    public Long getInvestimentoId() { return investimentoId; }
    public TipoMovimentoInvestimento getTipo() { return tipo; }
    public ValorMonetario getValor() { return valor; }
    public LocalDate getData() { return data; }
    public Long getTransacaoId() { return transacaoId; }
}
