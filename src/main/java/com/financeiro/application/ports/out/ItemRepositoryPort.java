package com.financeiro.application.ports.out;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Item;

import java.util.Optional;

public interface ItemRepositoryPort {
	Item salvar(Item item);

	Optional<Item> buscarPorIdEUsuario(Long itemId, Long usuarioId);

	Pagina<Item> listarAtivosPorUsuario(Long usuarioId, Paginacao paginacao);
}
