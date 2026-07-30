package com.financeiro.application.ports.out;

import com.financeiro.domain.model.MeioPagamento;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;

import java.util.List;
import java.util.Optional;

public interface MeioPagamentoRepositoryPort {

    MeioPagamento salvar(MeioPagamento meioPagamento);

    Optional<MeioPagamento> buscarPorId(Long id);

    Optional<MeioPagamento> buscarPorIdEUsuario(Long id, Long usuarioId);

    List<MeioPagamento> listarPorUsuario(Long usuarioId);
    Pagina<MeioPagamento> listarPorUsuario(Long usuarioId, Paginacao paginacao);
}
