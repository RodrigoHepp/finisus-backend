package com.financeiro.domain.model;

import com.financeiro.domain.vo.ValorMonetario;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CompraParcelada {

    private final Long id;
    private final Long usuarioId;
    private final String descricao;
    private final ValorMonetario valorTotal;
    private final int numeroParcelas;
    private final LocalDate dataCompra;
    private final Long categoriaId;
    private final Long contaId;
    private final LocalDateTime canceladaEm;

    private CompraParcelada(Long id, Long usuarioId, String descricao, ValorMonetario valorTotal,
                            int numeroParcelas, LocalDate dataCompra, Long categoriaId, Long contaId, LocalDateTime canceladaEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.descricao = descricao;
        this.valorTotal = valorTotal;
        this.numeroParcelas = numeroParcelas;
        this.dataCompra = dataCompra;
        this.categoriaId = categoriaId;
        this.contaId = contaId;
        this.canceladaEm = canceladaEm;
    }

    public static CompraParcelada nova(Long usuarioId, String descricao, ValorMonetario valorTotal,
                                       int numeroParcelas, LocalDate dataCompra,
                                       Long categoriaId, Long contaId) {
        return new CompraParcelada(null, usuarioId, descricao, valorTotal, numeroParcelas,
                dataCompra, categoriaId, contaId, null);
    }

    public static CompraParcelada reconstituir(Long id, Long usuarioId, String descricao,
                                               ValorMonetario valorTotal, int numeroParcelas,
                                               LocalDate dataCompra, Long categoriaId, Long contaId) {
        return reconstituir(id, usuarioId, descricao, valorTotal, numeroParcelas, dataCompra, categoriaId, contaId, null);
    }

    public static CompraParcelada reconstituir(Long id, Long usuarioId, String descricao,
                                               ValorMonetario valorTotal, int numeroParcelas,
                                               LocalDate dataCompra, Long categoriaId, Long contaId, LocalDateTime canceladaEm) {
        return new CompraParcelada(id, usuarioId, descricao, valorTotal, numeroParcelas, dataCompra, categoriaId, contaId, canceladaEm);
    }

    public CompraParcelada cancelada(LocalDateTime canceladaEm) { if (this.canceladaEm != null) throw new com.financeiro.domain.DomainException("error.compra.cancelada"); return reconstituir(id, usuarioId, descricao, valorTotal, numeroParcelas, dataCompra, categoriaId, contaId, canceladaEm); }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getDescricao() { return descricao; }
    public ValorMonetario getValorTotal() { return valorTotal; }
    public int getNumeroParcelas() { return numeroParcelas; }
    public LocalDate getDataCompra() { return dataCompra; }
    public Long getCategoriaId() { return categoriaId; }
    public Long getContaId() { return contaId; }
    public LocalDateTime getCanceladaEm() { return canceladaEm; }
}
