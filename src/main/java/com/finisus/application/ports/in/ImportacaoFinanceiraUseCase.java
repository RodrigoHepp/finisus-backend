package com.finisus.application.ports.in;

import com.finisus.domain.model.ImportacaoFinanceira;
import com.finisus.domain.model.TipoTransacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ImportacaoFinanceiraUseCase {
	Revisao iniciar(Long usuarioId, IniciarCommand command);
	Revisao buscar(Long usuarioId, Long importacaoId);
	Revisao revisar(Long usuarioId, Long importacaoId, RevisarCommand command);
	ImportacaoFinanceira confirmar(Long usuarioId, Long importacaoId);

	record IniciarCommand(Long bancoId, String nomeArquivo, String contentType, byte[] conteudo) { }
	record RevisarCommand(Long contaId, Long faturaId, List<LancamentoCommand> lancamentos) { }
	record LancamentoCommand(Long id, LocalDate data, String descricao, BigDecimal valor, TipoTransacao tipo,
			boolean importar, Long categoriaId, Long itemId) { }
	record Revisao(ImportacaoFinanceira importacao, List<PossivelDuplicidade> possiveisDuplicidades) { }
	record PossivelDuplicidade(Long lancamentoImportadoId, Long transacaoId, LocalDate data, BigDecimal valor,
			String descricao) { }
}
