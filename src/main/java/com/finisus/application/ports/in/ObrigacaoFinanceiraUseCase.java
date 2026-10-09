package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.ObrigacaoFinanceira;
import com.finisus.domain.model.StatusObrigacaoFinanceira;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface ObrigacaoFinanceiraUseCase {
	ObrigacaoFinanceira criar(Long usuarioId, CriarCommand command);
	ObrigacaoFinanceira buscar(Long usuarioId, Long obrigacaoId);
	Pagina<ObrigacaoFinanceira> listar(Long usuarioId, FiltroListagem filtro, Paginacao paginacao);
	ObrigacaoFinanceira pagar(Long usuarioId, Long obrigacaoId, PagamentoCommand command);
	default ObrigacaoFinanceira pagar(Long usuarioId, Long obrigacaoId, LocalDate dataPagamento) {
		return pagar(usuarioId, obrigacaoId, new PagamentoCommand(dataPagamento, null, null, null, null));
	}
	java.util.List<com.finisus.domain.model.PagamentoObrigacao> listarPagamentos(Long usuarioId, Long obrigacaoId);
	ObrigacaoFinanceira estornarPagamento(Long usuarioId, Long obrigacaoId, Long pagamentoId);
	ObrigacaoFinanceira estornarPagamento(Long usuarioId, Long obrigacaoId);
	ObrigacaoFinanceira cancelar(Long usuarioId, Long obrigacaoId);
	int processarVencimentos(LocalDate dataReferencia);

	record CriarCommand(String descricao, String credor, BigDecimal valor, LocalDate dataVencimento,
			Long contaPagamentoId, Long categoriaId) { }
	record PagamentoCommand(LocalDate dataPagamento, BigDecimal valor, BigDecimal juros, BigDecimal encargos,
			BigDecimal desconto) { }
	record FiltroListagem(StatusObrigacaoFinanceira status, LocalDate inicio, LocalDate fim) { }
}
