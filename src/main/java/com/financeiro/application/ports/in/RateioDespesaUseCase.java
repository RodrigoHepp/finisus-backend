package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.RateioDespesa;

import java.util.List;

public interface RateioDespesaUseCase {
	List<RateioDespesa> listar(Long usuarioId, Long despesaId);

	Pagina<RateioDespesa> listar(Long usuarioId, Long despesaId, Paginacao paginacao);

	RateioDespesa responder(Long usuarioId, Long rateioId, boolean aceita);

	RateioDespesa marcarPago(Long usuarioId, Long rateioId);

	Pagina<RateioRecebido> listarRecebidos(Long usuarioId, Paginacao paginacao);

	record RateioRecebido(RateioDespesa rateio, com.financeiro.domain.model.DespesaCompartilhada despesa) {
	}
}
