package com.financeiro.application.ports.in;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Fatura;
import com.financeiro.domain.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FaturaUseCase {
	Fatura criar(Long usuarioId, CriarCommand command);

	Fatura buscar(Long usuarioId, Long faturaId);

	Detalhe buscarDetalhe(Long usuarioId, Long faturaId);

	List<Fatura> listar(Long usuarioId, Long cartaoId);

	Pagina<Fatura> listar(Long usuarioId, Long cartaoId, Paginacao paginacao);

	Transacao lancarGasto(Long usuarioId, LancarGastoCommand command);

	Fatura fechar(Long usuarioId, Long faturaId);

	Fatura pagar(Long usuarioId, Long faturaId, LocalDate dataPagamento);

	Fatura atualizar(Long usuarioId, Long faturaId, AtualizarCommand command);

	Fatura cancelar(Long usuarioId, Long faturaId);

	record CriarCommand(Long cartaoId, String anoMes, LocalDate dataFechamento, LocalDate dataVencimento,
			Long contaPagamentoId) {
	}

	record LancarGastoCommand(Long faturaId, BigDecimal valor, LocalDate data, String descricao, Long contaId,
			Long categoriaId, List<TransacaoUseCase.ItemCommand> itens) {
	}

	record Detalhe(Fatura fatura, List<Transacao> transacoes, BigDecimal valorTotal) {
	}

	record AtualizarCommand(LocalDate dataFechamento, LocalDate dataVencimento, Long contaPagamentoId) {
	}
}
