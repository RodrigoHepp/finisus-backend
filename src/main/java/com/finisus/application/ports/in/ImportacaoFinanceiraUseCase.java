package com.finisus.application.ports.in;

import com.finisus.domain.model.ImportacaoFinanceira;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ImportacaoFinanceiraUseCase {
	Revisao iniciar(Long usuarioId, IniciarCommand command);
	Revisao buscar(Long usuarioId, Long importacaoId);
	Revisao revisar(Long usuarioId, Long importacaoId, RevisarCommand command);
	ImportacaoFinanceira confirmar(Long usuarioId, Long importacaoId);

	record IniciarCommand(Long bancoId, TipoDocumentoFinanceiro tipoPretendido, Long contaId, Long faturaId,
			String nomeArquivo, String contentType, byte[] conteudo) {
		public IniciarCommand(Long bancoId, String nomeArquivo, String contentType, byte[] conteudo) {
			this(bancoId, null, null, null, nomeArquivo, contentType, conteudo);
		}
	}
	record RevisarCommand(Long contaId, Long faturaId, List<LancamentoCommand> lancamentos) { }
	record LancamentoCommand(Long id, LocalDate data, String descricao, BigDecimal valor, TipoTransacao tipo,
			boolean importar, Long categoriaId, Long itemId, Long transacaoId, Long obrigacaoFinanceiraId,
			String justificativa) { }
	record Revisao(ImportacaoFinanceira importacao, List<PossivelDuplicidade> possiveisDuplicidades,
			List<Divergencia> divergencias) { }
	record Divergencia(String codigo, String mensagem, String esperado, String detectado) { }
	record PossivelDuplicidade(Long lancamentoImportadoId, Long transacaoId, Long transferenciaId, Long obrigacaoFinanceiraId,
			LocalDate data, BigDecimal valor, String descricao, NivelDuplicidade nivel, List<String> evidencias) { }
	enum NivelDuplicidade { EXATA, PROVAVEL }
}
