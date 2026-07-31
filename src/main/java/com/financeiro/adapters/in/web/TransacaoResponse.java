package com.financeiro.adapters.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.financeiro.domain.model.TipoTransacao;
import com.financeiro.domain.model.Transacao;
import com.financeiro.domain.model.TransacaoItem;

public record TransacaoResponse(Long id, TipoTransacao tipo, BigDecimal valor, LocalDate data, String descricao,
		Long contaId, Long categoriaId, Long meioPagamentoId, List<ItemResponse> itens) {
	public static TransacaoResponse from(Transacao transacao) {
		return new TransacaoResponse(transacao.getId(), transacao.getTipo(), transacao.getValor().valor(),
				transacao.getData(), transacao.getDescricao(), transacao.getContaId(), transacao.getCategoriaId(),
				transacao.getMeioPagamentoId(), transacao.getItens().stream().map(ItemResponse::from).toList());
	}

	public record ItemResponse(Long id, Long itemId, String descricao, BigDecimal valor, Long categoriaId) {
		static ItemResponse from(TransacaoItem item) {
			return new ItemResponse(item.getId(), item.getItemId(), item.getDescricao(), item.getValor().valor(),
					item.getCategoriaId());
		}
	}
}
