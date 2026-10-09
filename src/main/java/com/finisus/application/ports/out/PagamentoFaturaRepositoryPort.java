package com.finisus.application.ports.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.finisus.domain.model.PagamentoFatura;

public interface PagamentoFaturaRepositoryPort {
	PagamentoFatura salvar(PagamentoFatura pagamento);
	Optional<PagamentoFatura> buscarPorUsuarioEChave(Long usuarioId, String chaveIdempotencia);
	List<PagamentoFatura> listarPorFaturaEUsuario(Long faturaId, Long usuarioId);
	Map<Long, List<PagamentoFatura>> listarPorFaturasEUsuario(List<Long> faturasIds, Long usuarioId);
}
