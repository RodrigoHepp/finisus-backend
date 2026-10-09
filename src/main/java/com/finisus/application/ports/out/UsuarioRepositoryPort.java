package com.finisus.application.ports.out;

import com.finisus.domain.model.Usuario;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepositoryPort {

	Usuario salvar(Usuario usuario);

	Optional<Usuario> buscarPorId(Long id);
	Optional<Usuario> buscarPorIdParaAtualizacao(Long id);

	List<Usuario> buscarPorIds(Collection<Long> ids);

	Optional<Usuario> buscarPorEmail(String email);

	Optional<Usuario> buscarPorEmailParaAtualizacao(String email);

	boolean existePorEmail(String email);

	List<Usuario> listarAtivos();
}
