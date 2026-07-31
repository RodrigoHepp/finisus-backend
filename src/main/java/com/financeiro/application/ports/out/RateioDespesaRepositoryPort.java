package com.financeiro.application.ports.out;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.RateioDespesa;

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
