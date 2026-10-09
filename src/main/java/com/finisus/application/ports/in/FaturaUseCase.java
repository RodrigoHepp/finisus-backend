package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.Fatura;
import com.finisus.domain.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FaturaUseCase {
	Fatura criar(Long usuarioId, CriarCommand command);

	Fatura buscar(Long usuarioId, Long faturaId);

	Detalhe buscarDetalhe(Long usuarioId, Long faturaId);
	Map<Long, BigDecimal> buscarValoresEmAberto(Long usuarioId, List<Long> faturasIds);

	List<Fatura> listar(Long usuarioId, Long cartaoId);

	Pagina<Fatura> listar(Long usuarioId, Long cartaoId, Paginacao paginacao);

	Transacao lancarGasto(Long usuarioId, LancarGastoCommand command);
	Transacao lancarCredito(Long usuarioId, LancarGastoCommand command);

	Fatura fechar(Long usuarioId, Long faturaId);
	ResultadoProcessamentoCiclo processarCiclos(Long usuarioId, LocalDate dataReferencia);

	Fatura pagar(Long usuarioId, Long faturaId, String chaveIdempotencia, PagamentoCommand command);
	Fatura estornarPagamento(Long usuarioId, Long faturaId);

	Fatura atualizar(Long usuarioId, Long faturaId, AtualizarCommand command);

	Fatura cancelar(Long usuarioId, Long faturaId);

	record CriarCommand(Long cartaoId, String anoMes, LocalDate dataFechamento, LocalDate dataVencimento,
			Long contaPagamentoId) {
	}

	record LancarGastoCommand(Long faturaId, BigDecimal valor, LocalDate data, String descricao, Long contaId,
			Long categoriaId, List<TransacaoUseCase.ItemCommand> itens) {
	}

	record Detalhe(Fatura fatura, List<Transacao> transacoes, BigDecimal valorTotal, BigDecimal valorPago,
			BigDecimal creditoAplicado, BigDecimal valorEmAberto, BigDecimal credito) {
	}

	record PagamentoCommand(BigDecimal valor, LocalDate dataPagamento, Long contaId) { }

	record AtualizarCommand(LocalDate dataFechamento, LocalDate dataVencimento, Long contaPagamentoId) {
	}

	record ResultadoProcessamentoCiclo(List<Fatura> fechadas, List<Fatura> criadas) {
	}
}
