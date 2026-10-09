package com.finisus.application.ports.out;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.ObrigacaoFinanceiraUseCase.FiltroListagem;
import com.finisus.domain.model.ObrigacaoFinanceira;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ObrigacaoFinanceiraRepositoryPort {
	ObrigacaoFinanceira salvar(ObrigacaoFinanceira obrigacao);
	Optional<ObrigacaoFinanceira> buscarPorIdEUsuario(Long obrigacaoId, Long usuarioId);
	Optional<ObrigacaoFinanceira> buscarPorIdEUsuarioParaAtualizacao(Long obrigacaoId, Long usuarioId);
	Pagina<ObrigacaoFinanceira> listarPorUsuario(Long usuarioId, FiltroListagem filtro, Paginacao paginacao);
	List<ObrigacaoFinanceira> listarEmAbertoVencidasAte(LocalDate dataReferencia);
	List<ObrigacaoFinanceira> listarPendentesPorUsuarioEPeriodo(Long usuarioId, LocalDate inicio, LocalDate fim);
	List<ObrigacaoFinanceira> buscarCandidatosImportacao(Long usuarioId, Long contaId, LocalDate inicio,
			LocalDate fim, BigDecimal valor);
	boolean existePorTransacaoEUsuario(Long transacaoId, Long usuarioId);
}
