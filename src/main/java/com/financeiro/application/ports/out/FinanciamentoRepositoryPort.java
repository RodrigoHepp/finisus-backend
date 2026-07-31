package com.financeiro.application.ports.out;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Financiamento;

import java.util.List;
import java.util.Optional;

public interface FinanciamentoRepositoryPort {
	Financiamento salvar(Financiamento financiamento);

	Optional<Financiamento> buscarPorIdEUsuario(Long id, Long usuarioId);

	List<Financiamento> listarPorUsuario(Long usuarioId);

	Pagina<Financiamento> listarPorUsuario(Long usuarioId, Paginacao paginacao);
}
