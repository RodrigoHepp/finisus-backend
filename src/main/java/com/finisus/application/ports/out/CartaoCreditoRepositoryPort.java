package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.CartaoCredito;
import java.util.List;
import java.util.Optional;

public interface CartaoCreditoRepositoryPort {
	CartaoCredito salvar(CartaoCredito cartao);

	Optional<CartaoCredito> buscarPorIdEUsuario(Long id, Long usuarioId);

	List<CartaoCredito> listarPorUsuario(Long usuarioId);

	Pagina<CartaoCredito> listarPorUsuario(Long usuarioId, Paginacao paginacao);
}
