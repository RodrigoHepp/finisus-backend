package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.Investimento;
import com.finisus.domain.model.TipoInvestimento;

import java.util.List;

public interface InvestimentoUseCase {
	Investimento criar(Long usuarioId, CriarCommand command);

	Investimento buscar(Long usuarioId, Long investimentoId);

	Investimento atualizar(Long usuarioId, Long investimentoId, CriarCommand command);

	Investimento inativar(Long usuarioId, Long investimentoId);

	List<Investimento> listar(Long usuarioId);

	Pagina<Investimento> listar(Long usuarioId, Paginacao paginacao);

	record CriarCommand(String nome, TipoInvestimento tipo, Long contaOrigemId, Long contaCustodiaId) {
	}
}
