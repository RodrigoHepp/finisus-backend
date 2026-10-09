package com.finisus.application.ports.out;

import com.finisus.domain.model.ImportacaoFinanceira;
import java.util.Optional;

public interface ImportacaoFinanceiraRepositoryPort {
	ImportacaoFinanceira salvar(ImportacaoFinanceira importacao);
	Optional<ImportacaoFinanceira> buscarPorUsuarioBancoEHash(Long usuarioId, Long bancoId, String hashArquivo);
	Optional<ImportacaoFinanceira> buscarPorIdEUsuario(Long importacaoId, Long usuarioId);
	Optional<ImportacaoFinanceira> buscarPorIdEUsuarioParaAtualizacao(Long importacaoId, Long usuarioId);
}
