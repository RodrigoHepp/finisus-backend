package com.finisus.application.service;

import com.finisus.application.ports.in.RecorrenciaUseCase;
import com.finisus.application.ports.out.*;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.*;
import com.finisus.domain.vo.AnoMes;
import com.finisus.domain.vo.ValorMonetario;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class RecorrenciaService implements RecorrenciaUseCase {
	private final RecorrenciaRepositoryPort recorrencias;
	private final ContaRepositoryPort contas;
	private final CategoriaRepositoryPort categorias;
	private final MeioPagamentoRepositoryPort meios;
	private final TransacaoRepositoryPort transacoes;
	private final ObterDataAtualPort dataAtual;

	public RecorrenciaService(RecorrenciaRepositoryPort recorrencias, ContaRepositoryPort contas,
			CategoriaRepositoryPort categorias, MeioPagamentoRepositoryPort meios, TransacaoRepositoryPort transacoes,
			ObterDataAtualPort dataAtual) {
		this.recorrencias = recorrencias;
		this.contas = contas;
		this.categorias = categorias;
		this.meios = meios;
		this.transacoes = transacoes;
		this.dataAtual = dataAtual;
	}

	public Recorrencia criar(Long usuarioId, CriarCommand command) {
		ContaAtivaValidator.exigirAtiva(require(contas.buscarPorIdEUsuario(command.contaId(), usuarioId)));
		if (command.categoriaId() != null)
			CategoriaAtivaValidator.exigirAtiva(
					require(categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId)));
		if (command.meioPagamentoId() != null)
			require(meios.buscarPorIdEUsuario(command.meioPagamentoId(), usuarioId));
		if (command.diaDoMes() < 1 || command.diaDoMes() > 31)
			throw new DomainException("error.recorrencia.dia.invalido");
		return recorrencias.salvar(
				Recorrencia.nova(usuarioId, command.nome(), command.tipo(), ValorMonetario.of(command.valorEsperado()),
						command.diaDoMes(), command.categoriaId(), command.contaId(), command.meioPagamentoId()));
	}

	public List<Recorrencia> listar(Long usuarioId) {
		return recorrencias.listarPorUsuario(usuarioId);
	}

	public Pagina<Recorrencia> listar(Long usuarioId, Paginacao paginacao) {
		return recorrencias.listarPorUsuario(usuarioId, paginacao);
	}

	public Recorrencia buscar(Long usuarioId, Long recorrenciaId) {
		return require(recorrencias.buscarPorIdEUsuario(recorrenciaId, usuarioId));
	}

	public Recorrencia atualizar(Long usuarioId, Long recorrenciaId, CriarCommand command) {
		validar(usuarioId, command);
		Recorrencia atual = buscar(usuarioId, recorrenciaId);
		return recorrencias.salvar(Recorrencia.reconstituir(atual.getId(), usuarioId, command.nome(), command.tipo(),
				ValorMonetario.of(command.valorEsperado()), command.diaDoMes(), command.categoriaId(),
				command.contaId(), command.meioPagamentoId(), atual.isAtivo()));
	}

	public Recorrencia inativar(Long usuarioId, Long recorrenciaId) {
		Recorrencia atual = buscar(usuarioId, recorrenciaId);
		return recorrencias.salvar(Recorrencia.reconstituir(atual.getId(), usuarioId, atual.getNome(), atual.getTipo(),
				atual.getValorEsperado(), atual.getDiaDoMes(), atual.getCategoriaId(), atual.getContaId(),
				atual.getMeioPagamentoId(), false));
	}

	@Transactional
	public List<OcorrenciaRecorrencia> gerarMes(Long usuarioId, String anoMes) {
		AnoMes periodo = AnoMes.parse(anoMes);
		AnoMes mesAtual = AnoMes.from(dataAtual.obter());
		if (periodo.primeiroDia().isAfter(mesAtual.primeiroDia())) {
			throw new DomainException("error.recorrencia.periodo.futuro");
		}
		List<OcorrenciaRecorrencia> geradas = new ArrayList<>();
		for (Recorrencia recorrencia : recorrencias.listarAtivasPorUsuario(usuarioId)) {
			if (recorrencias.existsGeracaoPorRecorrenciaEAnoMes(recorrencia.getId(), periodo))
				continue;
			LocalDate data = periodo.primeiroDia()
					.withDayOfMonth(Math.min(recorrencia.getDiaDoMes(), periodo.primeiroDia().lengthOfMonth()));
			Conta conta = require(contas.buscarPorIdEUsuario(recorrencia.getContaId(), usuarioId));
			if (!conta.isAtivo())
				continue;
			geradas.add(recorrencias.salvarOcorrencia(OcorrenciaRecorrencia.pendente(recorrencia,
					periodo.formatado(), data)));
		}
		return geradas;
	}

	public List<OcorrenciaRecorrencia> listarOcorrencias(Long usuarioId, String anoMes) {
		return recorrencias.listarOcorrencias(usuarioId, AnoMes.parse(anoMes));
	}

	@Transactional
	public OcorrenciaRecorrencia realizar(Long usuarioId, Long ocorrenciaId) {
		OcorrenciaRecorrencia ocorrencia = require(
				recorrencias.buscarOcorrenciaParaAtualizacao(ocorrenciaId, usuarioId));
		if (ocorrencia.status() != StatusOcorrenciaRecorrencia.PENDENTE)
			throw new DomainException("error.recorrencia.ocorrencia.ja.realizada");
		Conta conta = require(contas.buscarPorIdEUsuario(ocorrencia.contaId(), usuarioId));
		ContaAtivaValidator.exigirAtiva(conta);
		Transacao transacao = transacoes.salvar(Transacao.geradaPorRecorrencia(usuarioId, ocorrencia.tipo(),
				ocorrencia.valor(), dataAtual.obter(), ocorrencia.descricao(), ocorrencia.contaId(),
				ocorrencia.categoriaId(), ocorrencia.meioPagamentoId(), ocorrencia.recorrenciaId()));
		ValorMonetario saldo = ocorrencia.tipo() == TipoTransacao.ENTRADA
				? conta.getSaldo().somar(ocorrencia.valor()) : conta.getSaldo().subtrair(ocorrencia.valor());
		contas.salvar(Conta.reconstituir(conta.getId(), conta.getUsuarioId(), conta.getNome(), conta.getTipo(),
				conta.getBancoId(), saldo, conta.getVersion()));
		transacoes.salvarHistorico(TransacaoHistorico.registrar(transacao.getId(), "BAIXA_RECORRENCIA", null,
				transacao.getValor().valor().toPlainString(), usuarioId, dataAtual.obterDataHora()));
		return recorrencias.salvarOcorrencia(ocorrencia.realizada(transacao.getId()));
	}

	private <T> T require(java.util.Optional<T> value) {
		return value.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}

	private void validar(Long usuarioId, CriarCommand command) {
		ContaAtivaValidator.exigirAtiva(require(contas.buscarPorIdEUsuario(command.contaId(), usuarioId)));
		if (command.categoriaId() != null)
			CategoriaAtivaValidator.exigirAtiva(
					require(categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId)));
		if (command.meioPagamentoId() != null)
			require(meios.buscarPorIdEUsuario(command.meioPagamentoId(), usuarioId));
		if (command.diaDoMes() < 1 || command.diaDoMes() > 31)
			throw new DomainException("error.recorrencia.dia.invalido");
	}
}
