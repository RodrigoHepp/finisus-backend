package com.finisus.application.ports.out;

import com.finisus.domain.model.PagamentoObrigacao;
import java.util.List;
import java.util.Optional;

public interface PagamentoObrigacaoRepositoryPort {
	PagamentoObrigacao salvar(PagamentoObrigacao pagamento);
	Optional<PagamentoObrigacao> buscarPorIdObrigacaoEUsuario(Long pagamentoId, Long obrigacaoId, Long usuarioId);
	List<PagamentoObrigacao> listarPorObrigacaoEUsuario(Long obrigacaoId, Long usuarioId);
}
