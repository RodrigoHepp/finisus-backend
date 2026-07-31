package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.Categoria;

import java.util.List;

public interface CategoriaUseCase {
	Categoria criar(Long usuarioId, CriarCommand command);

	Categoria buscar(Long usuarioId, Long categoriaId);

	List<Categoria> listar(Long usuarioId);

	Pagina<Categoria> listar(Long usuarioId, Paginacao paginacao);

	Categoria atualizar(Long usuarioId, Long categoriaId, CriarCommand command);

	Categoria inativar(Long usuarioId, Long categoriaId);

	record CriarCommand(String nome, Long categoriaPaiId) {
	}
}
