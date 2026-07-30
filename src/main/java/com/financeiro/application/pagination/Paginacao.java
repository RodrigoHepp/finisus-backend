package com.financeiro.application.pagination;

import com.financeiro.domain.DomainException;

/**
 * Contrato comum das listas: pagina é baseada em zero e tamanho deve estar entre 1 e 100.
 */
public record Paginacao(int pagina, int tamanho) {
    public static final int TAMANHO_PADRAO = 20;
    public static final int TAMANHO_MAXIMO = 100;

    public Paginacao {
        if (pagina < 0 || tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new DomainException("error.paginacao.invalida");
        }
    }
}
