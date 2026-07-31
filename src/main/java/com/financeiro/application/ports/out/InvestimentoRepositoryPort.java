package com.financeiro.application.ports.out;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Investimento;

import java.util.List;
import java.util.Optional;

public interface InvestimentoRepositoryPort {
	Investimento salvar(Investimento investimento);

	Optional<Investimento> buscarPorIdEUsuario(Long id, Long usuarioId);

	List<Investimento> listarPorUsuario(Long usuarioId);

	Pagina<Investimento> listarPorUsuario(Long usuarioId, Paginacao paginacao);
}
