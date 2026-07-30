package com.financeiro.domain.model;

import com.financeiro.domain.vo.AnoMes;

import java.time.LocalDate;

public class Fatura {

    private final Long id;
    private final Long cartaoId;
    private final AnoMes mesReferencia;
    private final LocalDate dataFechamento;
    private final LocalDate dataVencimento;
    private StatusFatura status;
    private final Long contaPagamentoId;
    private final long version;

    private Fatura(Long id, Long cartaoId, AnoMes mesReferencia, LocalDate dataFechamento,
                   LocalDate dataVencimento, StatusFatura status, Long contaPagamentoId, long version) {
        this.id = id;
        this.cartaoId = cartaoId;
        this.mesReferencia = mesReferencia;
        this.dataFechamento = dataFechamento;
        this.dataVencimento = dataVencimento;
        this.status = status;
        this.contaPagamentoId = contaPagamentoId;
        this.version = version;
    }

    public static Fatura nova(Long cartaoId, AnoMes mesReferencia, LocalDate dataFechamento,
                              LocalDate dataVencimento, Long contaPagamentoId) {
        return new Fatura(null, cartaoId, mesReferencia, dataFechamento, dataVencimento,
                StatusFatura.ABERTA, contaPagamentoId, 0);
    }

    public static Fatura reconstituir(Long id, Long cartaoId, AnoMes mesReferencia,
                                      LocalDate dataFechamento, LocalDate dataVencimento,
                                      StatusFatura status, Long contaPagamentoId, long version) {
        return new Fatura(id, cartaoId, mesReferencia, dataFechamento, dataVencimento,
                status, contaPagamentoId, version);
    }

    public void fechar() {
        if (status != StatusFatura.ABERTA) {
            throw new com.financeiro.domain.DomainException("error.fatura.transicao.invalida");
        }
        this.status = StatusFatura.FECHADA;
    }

    public void pagar() {
        if (status != StatusFatura.FECHADA) {
            throw new com.financeiro.domain.DomainException("error.fatura.transicao.invalida");
        }
        this.status = StatusFatura.PAGA;
    }

    public Long getId() { return id; }
    public Long getCartaoId() { return cartaoId; }
    public AnoMes getMesReferencia() { return mesReferencia; }
    public LocalDate getDataFechamento() { return dataFechamento; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public StatusFatura getStatus() { return status; }
    public Long getContaPagamentoId() { return contaPagamentoId; }
    public long getVersion() { return version; }
}
