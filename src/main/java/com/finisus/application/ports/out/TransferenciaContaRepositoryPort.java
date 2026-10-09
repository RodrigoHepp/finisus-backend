package com.finisus.application.ports.out;

import java.util.Optional;
import com.finisus.domain.model.TransferenciaConta;

public interface TransferenciaContaRepositoryPort {
	TransferenciaConta salvar(TransferenciaConta transferencia);
	Optional<TransferenciaConta> buscarPorIdEUsuario(Long id, Long usuarioId);
	Optional<TransferenciaConta> buscarPorIdEUsuarioParaAtualizacao(Long id, Long usuarioId);
	Optional<TransferenciaConta> buscarPorUsuarioEChave(Long usuarioId, String chave);
}
