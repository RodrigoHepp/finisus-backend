package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Conta;
import com.financeiro.domain.model.TipoConta;

import java.util.List;

public interface ContaUseCase {
	Conta criar(Long usuarioId, CriarCommand command);

	Conta buscar(Long usuarioId, Long contaId);

	List<Conta> listar(Long usuarioId);

	Pagina<Conta> listar(Long usuarioId, Paginacao paginacao);

	Conta atualizar(Long usuarioId, Long contaId, CriarCommand command);

	Conta inativar(Long usuarioId, Long contaId);

	record CriarCommand(String nome, TipoConta tipo, Long bancoId) {
	}
}
