package com.finisus.application.ports.out;

import com.finisus.domain.model.SolicitacaoPrivacidade;
import java.util.List;
import java.util.Optional;

public interface SolicitacaoPrivacidadeRepositoryPort {
	SolicitacaoPrivacidade salvar(SolicitacaoPrivacidade solicitacao);
	Optional<SolicitacaoPrivacidade> buscarAberta(Long usuarioId, SolicitacaoPrivacidade.Tipo tipo);
	List<SolicitacaoPrivacidade> listarPorUsuario(Long usuarioId);
}
