package com.finisus.application.ports.out;

import com.finisus.domain.model.MeioPagamento;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;

import java.util.List;
import java.util.Optional;

public interface MeioPagamentoRepositoryPort {

	MeioPagamento salvar(MeioPagamento meioPagamento);

	Optional<MeioPagamento> buscarPorId(Long id);

	Optional<MeioPagamento> buscarPorIdEUsuario(Long id, Long usuarioId);

	List<MeioPagamento> listarPorUsuario(Long usuarioId);

	Pagina<MeioPagamento> listarPorUsuario(Long usuarioId, Paginacao paginacao);
}
