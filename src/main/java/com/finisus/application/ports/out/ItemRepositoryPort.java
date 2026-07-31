package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.Item;

import java.util.Optional;

public interface ItemRepositoryPort {
	Item salvar(Item item);

	Optional<Item> buscarPorIdEUsuario(Long itemId, Long usuarioId);

	Pagina<Item> listarAtivosPorUsuario(Long usuarioId, Paginacao paginacao);
}
