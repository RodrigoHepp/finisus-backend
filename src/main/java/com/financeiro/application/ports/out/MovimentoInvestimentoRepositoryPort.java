package com.financeiro.application.ports.out;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.MovimentoInvestimento;

import java.util.List;
import java.util.Optional;

public interface MovimentoInvestimentoRepositoryPort {
	MovimentoInvestimento salvar(MovimentoInvestimento movimento);

	List<MovimentoInvestimento> listarPorInvestimento(Long investimentoId);

	Pagina<MovimentoInvestimento> listarPorInvestimento(Long investimentoId, Paginacao paginacao);

	Optional<MovimentoInvestimento> buscarPorIdEInvestimento(Long movimentoId, Long investimentoId);

	Optional<MovimentoInvestimento> buscarPorId(Long movimentoId);

	boolean existeCompensacao(Long movimentoOrigemId);
}
