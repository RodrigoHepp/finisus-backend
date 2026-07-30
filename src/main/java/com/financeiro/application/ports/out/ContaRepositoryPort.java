package com.financeiro.application.ports.out;

import com.financeiro.domain.model.Conta;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;

import java.util.List;
import java.util.Optional;

public interface ContaRepositoryPort {

    Conta salvar(Conta conta);

    Optional<Conta> buscarPorId(Long id);

    Optional<Conta> buscarPorIdEUsuario(Long id, Long usuarioId);

    List<Conta> listarPorUsuario(Long usuarioId);
    Pagina<Conta> listarPorUsuario(Long usuarioId, Paginacao paginacao);

    boolean existePorIdEUsuario(Long id, Long usuarioId);
}
