package com.finisus.application.service;

import java.util.List;
import java.util.Optional;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.CartaoCreditoUseCase;
import com.finisus.application.ports.out.CartaoCreditoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.CartaoCredito;
import com.finisus.domain.vo.ValorMonetario;

public class CartaoCreditoService implements CartaoCreditoUseCase {
	private final CartaoCreditoRepositoryPort cartoes;

	public CartaoCreditoService(CartaoCreditoRepositoryPort cartoes) {
		this.cartoes = cartoes;
	}

	@Override
	public CartaoCredito criarCartao(Long usuarioId, CriarCartaoCommand command) {
		validarDia(command.diaFechamento());
		validarDia(command.diaVencimento());
		return cartoes.salvar(CartaoCredito.novo(usuarioId, command.nome(), ValorMonetario.of(command.limite()),
				command.diaFechamento(), command.diaVencimento()));
	}

	@Override
	public List<CartaoCredito> listarCartoes(Long usuarioId) {
		return cartoes.listarPorUsuario(usuarioId);
	}

	@Override
	public Pagina<CartaoCredito> listarCartoes(Long usuarioId, Paginacao paginacao) {
		return cartoes.listarPorUsuario(usuarioId, paginacao);
	}

	@Override
	public CartaoCredito buscarCartao(Long usuarioId, Long cartaoId) {
		return require(cartoes.buscarPorIdEUsuario(cartaoId, usuarioId));
	}

	@Override
	public CartaoCredito atualizarCartao(Long usuarioId, Long cartaoId, CriarCartaoCommand command) {
		validarDia(command.diaFechamento());
		validarDia(command.diaVencimento());
		CartaoCredito atual = buscarCartao(usuarioId, cartaoId);
		return cartoes.salvar(CartaoCredito.reconstituir(atual.getId(), usuarioId, command.nome(),
				ValorMonetario.of(command.limite()), command.diaFechamento(), command.diaVencimento(),
				atual.isAtivo()));
	}

	@Override
	public CartaoCredito inativarCartao(Long usuarioId, Long cartaoId) {
		CartaoCredito atual = buscarCartao(usuarioId, cartaoId);
		return cartoes.salvar(CartaoCredito.reconstituir(atual.getId(), usuarioId, atual.getNome(), atual.getLimite(),
				atual.getDiaFechamento(), atual.getDiaVencimento(), false));
	}

	private void validarDia(int dia) {
		if (dia < 1 || dia > 31) {
			throw new DomainException("error.cartao.dia.invalido");
		}
	}

	private <T> T require(Optional<T> valor) {
		return valor.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}
}
