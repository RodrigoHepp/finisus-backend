package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.ItemUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.ItemRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Item;

public class ItemService implements ItemUseCase {
	private final ItemRepositoryPort itens;
	private final CategoriaRepositoryPort categorias;

	public ItemService(ItemRepositoryPort itens, CategoriaRepositoryPort categorias) {
		this.itens = itens;
		this.categorias = categorias;
	}

	@Override
	public Item criar(Long usuarioId, CriarCommand command) {
		validarCategoria(usuarioId, command.categoriaPadraoId());
		return itens.salvar(Item.novo(usuarioId, command.nome(), command.categoriaPadraoId()));
	}

	@Override
	public Item buscar(Long usuarioId, Long itemId) {
		return itens.buscarPorIdEUsuario(itemId, usuarioId).orElseThrow(this::naoEncontrado);
	}

	@Override
	public Pagina<Item> listar(Long usuarioId, Paginacao paginacao) {
		return itens.listarAtivosPorUsuario(usuarioId, paginacao);
	}

	@Override
	public Item atualizar(Long usuarioId, Long itemId, CriarCommand command) {
		Item atual = buscar(usuarioId, itemId);
		validarCategoria(usuarioId, command.categoriaPadraoId());
		return itens.salvar(Item.reconstituir(atual.getId(), usuarioId, command.nome(), command.categoriaPadraoId(),
				atual.isAtivo()));
	}

	@Override
	public Item inativar(Long usuarioId, Long itemId) {
		Item atual = buscar(usuarioId, itemId);
		return itens.salvar(
				Item.reconstituir(atual.getId(), usuarioId, atual.getNome(), atual.getCategoriaPadraoId(), false));
	}

	private void validarCategoria(Long usuarioId, Long categoriaId) {
		if (categoriaId != null)
			CategoriaAtivaValidator.exigirAtiva(
					categorias.buscarPorIdEUsuario(categoriaId, usuarioId).orElseThrow(this::naoEncontrado));
	}

	private DomainException naoEncontrado() {
		return new DomainException("error.recurso.nao.encontrado");
	}
}
