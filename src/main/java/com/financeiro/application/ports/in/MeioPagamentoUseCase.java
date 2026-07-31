package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.MeioPagamento;

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
