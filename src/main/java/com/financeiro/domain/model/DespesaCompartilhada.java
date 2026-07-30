package com.financeiro.domain.model;


public class DespesaCompartilhada {

    private final Long id;
    private final Long transacaoId;
    private final Long criadorId;
    private final TipoRateio tipoRateio;

    private DespesaCompartilhada(Long id, Long transacaoId, Long criadorId, TipoRateio tipoRateio) {
        this.id = id;
        this.transacaoId = transacaoId;
        this.criadorId = criadorId;
        this.tipoRateio = tipoRateio;
    }

    public static DespesaCompartilhada nova(Long transacaoId, Long criadorId, TipoRateio tipoRateio) {
        return new DespesaCompartilhada(null, transacaoId, criadorId, tipoRateio);
    }

    public static DespesaCompartilhada reconstituir(Long id, Long transacaoId, Long criadorId,
                                                    TipoRateio tipoRateio) {
        return new DespesaCompartilhada(id, transacaoId, criadorId, tipoRateio);
    }

    public Long getId() { return id; }
    public Long getTransacaoId() { return transacaoId; }
    public Long getCriadorId() { return criadorId; }
    public TipoRateio getTipoRateio() { return tipoRateio; }
}
