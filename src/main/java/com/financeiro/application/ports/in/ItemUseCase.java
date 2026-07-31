package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Item;

public interface ItemUseCase {
	Item criar(Long usuarioId, CriarCommand command);

	Item buscar(Long usuarioId, Long itemId);

	Pagina<Item> listar(Long usuarioId, Paginacao paginacao);

	Item atualizar(Long usuarioId, Long itemId, CriarCommand command);

	Item inativar(Long usuarioId, Long itemId);

	record CriarCommand(String nome, Long categoriaPadraoId) {
	}
}
