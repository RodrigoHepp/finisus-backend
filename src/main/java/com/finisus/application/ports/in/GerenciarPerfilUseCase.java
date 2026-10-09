package com.finisus.application.ports.in;

import com.finisus.domain.model.Usuario;
import com.finisus.domain.model.SolicitacaoPrivacidade;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface GerenciarPerfilUseCase {
	Usuario consultar(Long usuarioId);

	Usuario atualizar(Long usuarioId, AtualizarCommand command);

	void desativar(Long usuarioId);

	ExportacaoDados exportarDados(Long usuarioId);

	SolicitacaoPrivacidade solicitarAnonimizacao(Long usuarioId, String motivo);

	List<SolicitacaoPrivacidade> listarSolicitacoesPrivacidade(Long usuarioId);

	record AtualizarCommand(String nome, String email) {
	}

	record ExportacaoDados(int versaoFormato, Instant geradaEm,
			Map<String, List<Map<String, Object>>> secoes) {
	}
}
