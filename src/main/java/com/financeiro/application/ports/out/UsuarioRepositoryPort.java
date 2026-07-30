package com.financeiro.application.ports.out;

import com.financeiro.domain.model.Usuario;

import java.util.Optional;
import java.util.List;

public interface UsuarioRepositoryPort {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(String email);

    boolean existePorEmail(String email);

    List<Usuario> listarAtivos();
}
