package com.finisus.application.ports.out;

import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TransacaoHistorico;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;

import java.util.List;
import java.util.Optional;

public interface TransacaoRepositoryPort {

	Transacao salvar(Transacao transacao);

	Optional<Transacao> buscarPorId(Long id);

	Optional<Transacao> buscarPorIdEUsuario(Long id, Long usuarioId);

	Optional<Transacao> buscarPorIdEUsuarioParaAtualizacao(Long id, Long usuarioId);

	List<Transacao> listarPorUsuario(Long usuarioId);

	Pagina<Transacao> listarPorUsuario(Long usuarioId, Paginacao paginacao);

	List<Transacao> listarPorFatura(Long faturaId);

	List<Transacao> listarPorCompraParcelada(Long compraParceladaId);

	List<Transacao> listarPorConta(Long contaId);

	TransacaoHistorico salvarHistorico(TransacaoHistorico historico);

	List<TransacaoHistorico> listarHistoricoPorTransacao(Long transacaoId);

	Pagina<TransacaoHistorico> listarHistoricoPorTransacao(Long transacaoId, Paginacao paginacao);
}
