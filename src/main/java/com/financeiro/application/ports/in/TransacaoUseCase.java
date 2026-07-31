package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.TipoTransacao;
import com.financeiro.domain.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransacaoUseCase {
	Transacao registrar(Long usuarioId, RegistrarCommand command);

	Transacao buscar(Long usuarioId, Long transacaoId);

	List<Transacao> listar(Long usuarioId);

	Pagina<Transacao> listar(Long usuarioId, Paginacao paginacao);

	Transacao corrigir(Long usuarioId, Long transacaoId, RegistrarCommand command);

	Transacao estornar(Long usuarioId, Long transacaoId);

	record RegistrarCommand(TipoTransacao tipo, BigDecimal valor, LocalDate data, String descricao, Long contaId,
			Long categoriaId, Long meioPagamentoId, List<ItemCommand> itens) {
	}

	record ItemCommand(Long itemId, BigDecimal valor) {
	}
}
