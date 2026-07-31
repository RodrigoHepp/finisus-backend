package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.DespesaCompartilhada;
import com.finisus.domain.model.TipoAlvoCompartilhamento;

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
