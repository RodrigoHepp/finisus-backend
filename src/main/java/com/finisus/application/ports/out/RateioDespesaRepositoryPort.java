package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.RateioDespesa;

import java.util.List;
import java.util.Optional;

public interface RateioDespesaRepositoryPort {
	List<RateioDespesa> salvarTodos(Long despesaId, List<RateioDespesa> rateios);

	Optional<RateioDespesa> buscarPorId(Long rateioId);

	RateioDespesa salvar(RateioDespesa rateio);

	List<RateioDespesa> listarPorDespesaId(Long despesaId);

	Pagina<RateioDespesa> listarPorDespesaId(Long despesaId, Paginacao paginacao);

	Pagina<RateioDespesa> listarRecebidosPorUsuarioId(Long usuarioId, Paginacao paginacao);
}
