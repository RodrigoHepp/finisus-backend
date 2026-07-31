package com.financeiro.application.service;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.ContaUseCase;
import com.financeiro.application.ports.out.BancoRepositoryPort;
import com.financeiro.application.ports.out.ContaRepositoryPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.Conta;

import java.util.List;

public class ContaService implements ContaUseCase {
	private final ContaRepositoryPort contas;
	private final BancoRepositoryPort bancos;

	public ContaService(ContaRepositoryPort contas, BancoRepositoryPort bancos) {
		this.contas = contas;
		this.bancos = bancos;
	}

	@Override
	public Conta criar(Long usuarioId, CriarCommand command) {
		validarBanco(usuarioId, command.bancoId());
		return contas.salvar(Conta.nova(usuarioId, command.nome(), command.tipo(), command.bancoId()));
	}

	@Override
	public List<Conta> listar(Long usuarioId) {
		return contas.listarPorUsuario(usuarioId);
	}

	@Override
	public Pagina<Conta> listar(Long usuarioId, Paginacao paginacao) {
		return contas.listarPorUsuario(usuarioId, paginacao);
	}

	@Override
	public Conta buscar(Long usuarioId, Long contaId) {
		return contas.buscarPorIdEUsuario(contaId, usuarioId).orElseThrow(this::notFound);
	}

	@Override
	public Conta atualizar(Long usuarioId, Long contaId, CriarCommand command) {
		Conta atual = buscar(usuarioId, contaId);
		validarBanco(usuarioId, command.bancoId());
		return contas.salvar(Conta.reconstituir(atual.getId(), usuarioId, command.nome(), command.tipo(),
				command.bancoId(), atual.getSaldo(), atual.isAtivo(), atual.getVersion()));
	}

	@Override
	public Conta inativar(Long usuarioId, Long contaId) {
		Conta atual = buscar(usuarioId, contaId);
		return contas.salvar(Conta.reconstituir(atual.getId(), usuarioId, atual.getNome(), atual.getTipo(),
				atual.getBancoId(), atual.getSaldo(), false, atual.getVersion()));
	}

	private void validarBanco(Long usuarioId, Long bancoId) {
		if (bancoId != null && bancos.buscarPorId(bancoId)
				.filter(banco -> banco.isSistema() || usuarioId.equals(banco.getUsuarioId())).isEmpty())
			throw notFound();
	}

	private DomainException notFound() {
		return new DomainException("error.recurso.nao.encontrado");
	}
}
