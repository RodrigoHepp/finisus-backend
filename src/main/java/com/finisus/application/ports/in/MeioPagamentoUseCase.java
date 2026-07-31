package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.MeioPagamento;

import java.util.List;

public interface MeioPagamentoUseCase {
	MeioPagamento criar(Long usuarioId, CriarCommand command);

	MeioPagamento buscar(Long usuarioId, Long meioPagamentoId);

	List<MeioPagamento> listar(Long usuarioId);

	Pagina<MeioPagamento> listar(Long usuarioId, Paginacao paginacao);

	MeioPagamento atualizar(Long usuarioId, Long meioPagamentoId, CriarCommand command);

	MeioPagamento inativar(Long usuarioId, Long meioPagamentoId);

	record CriarCommand(String nome) {
	}
}
