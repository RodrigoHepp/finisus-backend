package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.CartaoCredito;

import java.math.BigDecimal;
import java.util.List;

public interface CartaoCreditoUseCase {
	CartaoCredito criarCartao(Long usuarioId, CriarCartaoCommand command);

	List<CartaoCredito> listarCartoes(Long usuarioId);

	Pagina<CartaoCredito> listarCartoes(Long usuarioId, Paginacao paginacao);

	CartaoCredito buscarCartao(Long usuarioId, Long cartaoId);

	CartaoCredito atualizarCartao(Long usuarioId, Long cartaoId, CriarCartaoCommand command);

	CartaoCredito inativarCartao(Long usuarioId, Long cartaoId);

	record CriarCartaoCommand(String nome, BigDecimal limite, int diaFechamento, int diaVencimento) {
	}
}
