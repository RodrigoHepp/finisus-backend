package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.TipoConta;

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
