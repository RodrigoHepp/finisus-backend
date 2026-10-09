package com.finisus.application.ports.out;

import java.util.Optional;

import com.finisus.domain.model.AjusteSaldoConta;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;

public interface AjusteSaldoContaRepositoryPort {
	AjusteSaldoConta salvar(AjusteSaldoConta ajuste);
	Optional<AjusteSaldoConta> buscarPorUsuarioEChave(Long usuarioId, String chaveIdempotencia);
	Pagina<AjusteSaldoConta> listarPorContaEUsuario(Long contaId, Long usuarioId, Paginacao paginacao);
}
