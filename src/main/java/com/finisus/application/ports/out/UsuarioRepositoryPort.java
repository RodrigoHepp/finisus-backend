package com.finisus.application.ports.out;

import com.finisus.domain.model.Usuario;

import java.util.Optional;
import java.util.List;

public interface UsuarioRepositoryPort {

	Usuario salvar(Usuario usuario);

	Optional<Usuario> buscarPorId(Long id);

	Optional<Usuario> buscarPorEmail(String email);

	boolean existePorEmail(String email);

	List<Usuario> listarAtivos();
}
