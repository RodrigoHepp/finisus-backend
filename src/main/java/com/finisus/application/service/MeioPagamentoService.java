package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.MeioPagamentoUseCase;
import com.finisus.application.ports.out.MeioPagamentoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.MeioPagamento;

import java.util.List;

public class MeioPagamentoService implements MeioPagamentoUseCase {
	private final MeioPagamentoRepositoryPort meios;

	public MeioPagamentoService(MeioPagamentoRepositoryPort meios) {
		this.meios = meios;
	}

	@Override
	public MeioPagamento criar(Long usuarioId, CriarCommand command) {
		return meios.salvar(MeioPagamento.novo(usuarioId, command.nome()));
	}

	@Override
	public List<MeioPagamento> listar(Long usuarioId) {
		return meios.listarPorUsuario(usuarioId);
	}

	@Override
	public Pagina<MeioPagamento> listar(Long usuarioId, Paginacao paginacao) {
		return meios.listarPorUsuario(usuarioId, paginacao);
	}

	@Override
	public MeioPagamento buscar(Long usuarioId, Long meioPagamentoId) {
		return meios.buscarPorIdEUsuario(meioPagamentoId, usuarioId).orElseThrow(this::notFound);
	}

	@Override
	public MeioPagamento atualizar(Long usuarioId, Long meioPagamentoId, CriarCommand command) {
		MeioPagamento atual = buscar(usuarioId, meioPagamentoId);
		return meios.salvar(MeioPagamento.reconstituir(atual.getId(), usuarioId, command.nome(), atual.isAtivo()));
	}

	@Override
	public MeioPagamento inativar(Long usuarioId, Long meioPagamentoId) {
		MeioPagamento atual = buscar(usuarioId, meioPagamentoId);
		return meios.salvar(MeioPagamento.reconstituir(atual.getId(), usuarioId, atual.getNome(), false));
	}

	private DomainException notFound() {
		return new DomainException("error.recurso.nao.encontrado");
	}
}
