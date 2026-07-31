package com.finisus.application.ports.out;

import com.finisus.domain.model.Categoria;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepositoryPort {

	Categoria salvar(Categoria categoria);

	Optional<Categoria> buscarPorId(Long id);

	Optional<Categoria> buscarPorIdEUsuario(Long id, Long usuarioId);

	List<Categoria> listarPorUsuario(Long usuarioId);

	Pagina<Categoria> listarPorUsuario(Long usuarioId, Paginacao paginacao);
}
