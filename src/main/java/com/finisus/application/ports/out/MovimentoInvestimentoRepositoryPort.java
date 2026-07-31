package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.MovimentoInvestimento;

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
