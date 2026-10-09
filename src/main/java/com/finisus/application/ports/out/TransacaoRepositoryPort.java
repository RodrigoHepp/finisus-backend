package com.finisus.application.ports.out;

import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TransacaoHistorico;
import com.finisus.application.ports.in.TransacaoUseCase.FiltroListagem;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import com.finisus.domain.model.TipoTransacao;

public interface TransacaoRepositoryPort {

	Transacao salvar(Transacao transacao);

	Optional<Transacao> buscarPorId(Long id);

	Optional<Transacao> buscarPorIdParaAtualizacao(Long id);

	Optional<Transacao> buscarPorIdEUsuario(Long id, Long usuarioId);

	Optional<Transacao> buscarPorIdEUsuarioParaAtualizacao(Long id, Long usuarioId);

	List<Transacao> listarPorUsuario(Long usuarioId);

	Pagina<Transacao> listarPorUsuario(Long usuarioId, Paginacao paginacao);

	Pagina<Transacao> listarPorUsuario(Long usuarioId, Paginacao paginacao, FiltroListagem filtro);

	List<Transacao> listarPorFatura(Long faturaId);
	Map<Long, List<Transacao>> listarPorFaturas(List<Long> faturasIds);

	List<Transacao> listarPagamentosPorFatura(Long faturaId);

	List<Transacao> listarPorCompraParcelada(Long compraParceladaId);

	List<Transacao> listarPorTransferencia(Long transferenciaId);

	List<Transacao> listarPorConta(Long contaId);

	List<Transacao> listarPorUsuarioEPeriodo(Long usuarioId, LocalDate inicio, LocalDate fim);

	List<Transacao> buscarCandidatosImportacaoPorConta(Long usuarioId, Long contaId, TipoTransacao tipo,
			java.math.BigDecimal valor, LocalDate inicio, LocalDate fim);

	List<Transacao> buscarCandidatosImportacaoPorFatura(Long usuarioId, Long faturaId, TipoTransacao tipo,
			java.math.BigDecimal valor, LocalDate inicio, LocalDate fim);

	TransacaoHistorico salvarHistorico(TransacaoHistorico historico);

	List<TransacaoHistorico> listarHistoricoPorTransacao(Long transacaoId);

	Pagina<TransacaoHistorico> listarHistoricoPorTransacao(Long transacaoId, Paginacao paginacao);
}
