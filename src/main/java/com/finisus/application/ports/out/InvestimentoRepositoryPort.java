package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.Investimento;

import java.util.List;
import java.util.Optional;

public interface InvestimentoRepositoryPort {
	Investimento salvar(Investimento investimento);

	Optional<Investimento> buscarPorIdEUsuario(Long id, Long usuarioId);

	List<Investimento> listarPorUsuario(Long usuarioId);

	Pagina<Investimento> listarPorUsuario(Long usuarioId, Paginacao paginacao);

	boolean existePorContaCustodiaExceto(Long contaCustodiaId, Long investimentoId);
}
