package com.finisus.application.ports.in;

import com.finisus.domain.model.ImportacaoFinanceira;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (!(object instanceof IniciarCommand other)) {
				return false;
			}
			return Objects.equals(bancoId, other.bancoId)
					&& tipoPretendido == other.tipoPretendido
					&& Objects.equals(contaId, other.contaId)
					&& Objects.equals(faturaId, other.faturaId)
					&& Objects.equals(nomeArquivo, other.nomeArquivo)
					&& Objects.equals(contentType, other.contentType)
					&& Arrays.equals(conteudo, other.conteudo);
		}

		@Override
		public int hashCode() {
			int result = Objects.hash(bancoId, tipoPretendido, contaId, faturaId, nomeArquivo, contentType);
			return 31 * result + Arrays.hashCode(conteudo);
		}

		@Override
		public String toString() {
			return "IniciarCommand[bancoId=" + bancoId
					+ ", tipoPretendido=" + tipoPretendido
					+ ", contaId=" + contaId
					+ ", faturaId=" + faturaId
					+ ", nomeArquivo=" + nomeArquivo
					+ ", contentType=" + contentType
					+ ", conteudoLength=" + (conteudo == null ? 0 : conteudo.length) + "]";
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
