package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.Financiamento;

import java.util.List;
import java.util.Optional;

public interface FinanciamentoRepositoryPort {
	Financiamento salvar(Financiamento financiamento);

	Optional<Financiamento> buscarPorIdEUsuario(Long id, Long usuarioId);

	List<Financiamento> listarPorUsuario(Long usuarioId);

	Pagina<Financiamento> listarPorUsuario(Long usuarioId, Paginacao paginacao);
}
