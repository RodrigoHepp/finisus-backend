package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Banco;

import java.util.List;

public interface BancoUseCase {
	Banco criar(Long usuarioId, CriarCommand command);

	Banco buscar(Long usuarioId, Long bancoId);

	List<Banco> listar(Long usuarioId);

	Pagina<Banco> listar(Long usuarioId, Paginacao paginacao);

	Banco atualizar(Long usuarioId, Long bancoId, CriarCommand command);

	Banco inativar(Long usuarioId, Long bancoId);

	record CriarCommand(String nome, String codigo) {
	}
}
