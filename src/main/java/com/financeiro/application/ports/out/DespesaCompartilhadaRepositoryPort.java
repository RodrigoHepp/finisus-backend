package com.financeiro.application.ports.out;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.DespesaCompartilhada;
import com.financeiro.domain.model.TipoAlvoCompartilhamento;

import java.util.List;
import java.util.Optional;

public interface DespesaCompartilhadaRepositoryPort {
	DespesaCompartilhada salvar(DespesaCompartilhada despesa);

	Optional<DespesaCompartilhada> buscarPorId(Long despesaId);

	Optional<DespesaCompartilhada> buscarPorIdECriadorId(Long despesaId, Long criadorId);

	boolean existePorTransacaoETipoAlvo(Long transacaoId, TipoAlvoCompartilhamento tipoAlvo);

	boolean existePorTransacaoItemId(Long transacaoItemId);

	List<DespesaCompartilhada> listarPorCriadorId(Long criadorId);

	Pagina<DespesaCompartilhada> listarPorCriadorId(Long criadorId, Paginacao paginacao);
}
