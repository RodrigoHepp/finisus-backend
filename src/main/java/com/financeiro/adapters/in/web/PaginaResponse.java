package com.financeiro.adapters.in.web;

import com.financeiro.application.pagination.Pagina;

import java.util.List;

public record PaginaResponse<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {
    public static <T> PaginaResponse<T> from(Pagina<T> pagina) {
        return new PaginaResponse<>(pagina.conteudo(), pagina.pagina(), pagina.tamanho(), pagina.totalElementos(), pagina.totalPaginas());
    }
}
