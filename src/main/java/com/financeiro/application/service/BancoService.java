package com.financeiro.application.service;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.BancoUseCase;
import com.financeiro.application.ports.out.BancoRepositoryPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.Banco;

import java.util.List;

public class BancoService implements BancoUseCase {
	private final BancoRepositoryPort bancos;

	public BancoService(BancoRepositoryPort bancos) {
		this.bancos = bancos;
	}

	@Override
	public Banco criar(Long usuarioId, CriarCommand command) {
		return bancos.salvar(Banco.novoUsuario(command.nome(), command.codigo(), usuarioId));
	}

	@Override
	public List<Banco> listar(Long usuarioId) {
		return bancos.listarDisponiveisParaUsuario(usuarioId);
	}

	@Override
	public Pagina<Banco> listar(Long usuarioId, Paginacao paginacao) {
		return bancos.listarDisponiveisParaUsuario(usuarioId, paginacao);
	}

	@Override
	public Banco buscar(Long usuarioId, Long bancoId) {
		Banco banco = bancos.buscarPorId(bancoId).orElseThrow(this::notFound);
		if (banco.isSistema() || !usuarioId.equals(banco.getUsuarioId()))
			throw notFound();
		return banco;
	}

	@Override
	public Banco atualizar(Long usuarioId, Long bancoId, CriarCommand command) {
		Banco atual = buscar(usuarioId, bancoId);
		return bancos.salvar(
				Banco.reconstituir(atual.getId(), command.nome(), command.codigo(), atual.isAtivo(), usuarioId));
	}

	@Override
	public Banco inativar(Long usuarioId, Long bancoId) {
		Banco atual = buscar(usuarioId, bancoId);
		return bancos.salvar(Banco.reconstituir(atual.getId(), atual.getNome(), atual.getCodigo(), false, usuarioId));
	}

	private DomainException notFound() {
		return new DomainException("error.recurso.nao.encontrado");
	}
}
