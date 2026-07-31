package com.financeiro.application.service;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.ItemUseCase;
import com.financeiro.application.ports.out.CategoriaRepositoryPort;
import com.financeiro.application.ports.out.ItemRepositoryPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.Item;

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
			categorias.buscarPorIdEUsuario(categoriaId, usuarioId).orElseThrow(this::naoEncontrado);
	}

	private DomainException naoEncontrado() {
		return new DomainException("error.recurso.nao.encontrado");
	}
}
