package com.financeiro.application.pagination;

import java.util.List;
import java.util.function.Function;

public record Pagina<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {
    public <R> Pagina<R> map(Function<T, R> mapper) {
        return new Pagina<>(conteudo.stream().map(mapper).toList(), pagina, tamanho, totalElementos, totalPaginas);
    }
}
