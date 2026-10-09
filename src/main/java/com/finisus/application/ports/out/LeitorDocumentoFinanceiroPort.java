package com.finisus.application.ports.out;

import com.finisus.domain.model.LancamentoImportado;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface LeitorDocumentoFinanceiroPort {
	DocumentoLido ler(ArquivoPdf arquivo);

	record ArquivoPdf(byte[] conteudo, String nomeOriginal, String bancoCodigo, String bancoNome) {
		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (!(object instanceof ArquivoPdf other)) {
				return false;
			}
			return Arrays.equals(conteudo, other.conteudo)
					&& Objects.equals(nomeOriginal, other.nomeOriginal)
					&& Objects.equals(bancoCodigo, other.bancoCodigo)
					&& Objects.equals(bancoNome, other.bancoNome);
		}

		@Override
		public int hashCode() {
			int result = Arrays.hashCode(conteudo);
			return 31 * result + Objects.hash(nomeOriginal, bancoCodigo, bancoNome);
		}

		@Override
		public String toString() {
			return "ArquivoPdf[conteudoLength=" + (conteudo == null ? 0 : conteudo.length)
					+ ", nomeOriginal=" + nomeOriginal
					+ ", bancoCodigo=" + bancoCodigo
					+ ", bancoNome=" + bancoNome + "]";
		}
	}
	record DocumentoLido(String leitor, TipoDocumentoFinanceiro tipoDocumento, String identificadorOrigem, LocalDate periodoInicio,
			LocalDate periodoFim, LocalDate dataVencimento, BigDecimal saldoInicial, BigDecimal saldoFinal,
			BigDecimal valorTotal, List<LancamentoImportado> lancamentos) { }
}
