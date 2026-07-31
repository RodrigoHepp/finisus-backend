package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.ConsultarHistoricoTransacaoUseCase;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.TransacaoHistorico;

import java.util.List;

public class ConsultarHistoricoTransacaoService implements ConsultarHistoricoTransacaoUseCase {
	private final TransacaoRepositoryPort transacoes;

	public ConsultarHistoricoTransacaoService(TransacaoRepositoryPort transacoes) {
		this.transacoes = transacoes;
	}

	@Override
	public List<TransacaoHistorico> listar(Long usuarioId, Long transacaoId) {
		validarTransacao(usuarioId, transacaoId);
		return transacoes.listarHistoricoPorTransacao(transacaoId);
	}

	@Override
	public Pagina<TransacaoHistorico> listar(Long usuarioId, Long transacaoId, Paginacao paginacao) {
		validarTransacao(usuarioId, transacaoId);
		return transacoes.listarHistoricoPorTransacao(transacaoId, paginacao);
	}

	private void validarTransacao(Long usuarioId, Long transacaoId) {
		transacoes.buscarPorIdEUsuario(transacaoId, usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}
}
