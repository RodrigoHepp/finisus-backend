package com.finisus.application.ports.out;

import com.finisus.domain.model.LancamentoImportado;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LeitorDocumentoFinanceiroPort {
	DocumentoLido ler(ArquivoPdf arquivo);

	record ArquivoPdf(byte[] conteudo, String nomeOriginal, String bancoCodigo, String bancoNome) { }
	record DocumentoLido(String leitor, TipoDocumentoFinanceiro tipoDocumento, String identificadorOrigem, LocalDate periodoInicio,
			LocalDate periodoFim, LocalDate dataVencimento, BigDecimal saldoInicial, BigDecimal saldoFinal,
			BigDecimal valorTotal, List<LancamentoImportado> lancamentos) { }
}
