package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.CategoriaUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Categoria;

import java.util.List;

public class CategoriaService implements CategoriaUseCase {
	private final CategoriaRepositoryPort categorias;

	public CategoriaService(CategoriaRepositoryPort categorias) {
		this.categorias = categorias;
	}

	@Override
	public Categoria criar(Long usuarioId, CriarCommand command) {
		validarPai(usuarioId, command.categoriaPaiId(), null);
		return categorias.salvar(Categoria.nova(usuarioId, command.nome(), command.categoriaPaiId()));
	}

	@Override
	public List<Categoria> listar(Long usuarioId) {
		return categorias.listarPorUsuario(usuarioId);
	}

	@Override
	public Pagina<Categoria> listar(Long usuarioId, Paginacao paginacao) {
		return categorias.listarPorUsuario(usuarioId, paginacao);
	}

	@Override
	public Categoria buscar(Long usuarioId, Long categoriaId) {
		return categorias.buscarPorIdEUsuario(categoriaId, usuarioId).orElseThrow(this::notFound);
	}

	@Override
	public Categoria atualizar(Long usuarioId, Long categoriaId, CriarCommand command) {
		Categoria atual = buscar(usuarioId, categoriaId);
		validarPai(usuarioId, command.categoriaPaiId(), categoriaId);
		return categorias.salvar(Categoria.reconstituir(atual.getId(), usuarioId, command.nome(),
				command.categoriaPaiId(), atual.isAtivo()));
	}

	@Override
	public Categoria inativar(Long usuarioId, Long categoriaId) {
		Categoria atual = buscar(usuarioId, categoriaId);
		return categorias.salvar(
				Categoria.reconstituir(atual.getId(), usuarioId, atual.getNome(), atual.getCategoriaPaiId(), false));
	}

	private void validarPai(Long usuarioId, Long paiId, Long categoriaId) {
		if (paiId == null)
			return;
		if (paiId.equals(categoriaId))
			throw new DomainException("error.categoria.pai.invalida");
		categorias.buscarPorIdEUsuario(paiId, usuarioId).orElseThrow(this::notFound);
	}

	private DomainException notFound() {
		return new DomainException("error.recurso.nao.encontrado");
	}
}
