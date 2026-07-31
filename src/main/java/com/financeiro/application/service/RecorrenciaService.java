package com.financeiro.application.service;

import com.financeiro.application.ports.in.RecorrenciaUseCase;
import com.financeiro.application.ports.out.*;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.*;
import com.financeiro.domain.vo.AnoMes;
import com.financeiro.domain.vo.ValorMonetario;
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
			require(categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId));
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
	public List<Transacao> gerarMes(Long usuarioId, String anoMes) {
		AnoMes periodo = AnoMes.parse(anoMes);
		AnoMes mesAtual = AnoMes.from(dataAtual.obter());
		if (periodo.primeiroDia().isAfter(mesAtual.primeiroDia())) {
			throw new DomainException("error.recorrencia.periodo.futuro");
		}
		List<Transacao> geradas = new ArrayList<>();
		for (Recorrencia recorrencia : recorrencias.listarAtivasPorUsuario(usuarioId)) {
			if (recorrencias.existsGeracaoPorRecorrenciaEAnoMes(recorrencia.getId(), periodo))
				continue;
			LocalDate data = periodo.primeiroDia()
					.withDayOfMonth(Math.min(recorrencia.getDiaDoMes(), periodo.primeiroDia().lengthOfMonth()));
			Conta conta = require(contas.buscarPorIdEUsuario(recorrencia.getContaId(), usuarioId));
			if (!conta.isAtivo())
				continue;
			Transacao transacao = transacoes.salvar(Transacao.geradaPorRecorrencia(usuarioId, recorrencia.getTipo(),
					recorrencia.getValorEsperado(), data, recorrencia.getNome(), recorrencia.getContaId(),
					recorrencia.getCategoriaId(), recorrencia.getMeioPagamentoId(), recorrencia.getId()));
			ValorMonetario saldo = recorrencia.getTipo() == TipoTransacao.ENTRADA
					? conta.getSaldo().somar(transacao.getValor())
					: conta.getSaldo().subtrair(transacao.getValor());
			contas.salvar(Conta.reconstituir(conta.getId(), conta.getUsuarioId(), conta.getNome(), conta.getTipo(),
					conta.getBancoId(), saldo, conta.getVersion()));
			recorrencias.registrarGeracao(recorrencia.getId(), periodo, transacao.getId());
			transacoes.salvarHistorico(TransacaoHistorico.registrar(transacao.getId(), "GERACAO_RECORRENCIA", null,
					transacao.getValor().valor().toPlainString(), usuarioId, dataAtual.obterDataHora()));
			geradas.add(transacao);
		}
		return geradas;
	}

	private <T> T require(java.util.Optional<T> value) {
		return value.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}

	private void validar(Long usuarioId, CriarCommand command) {
		ContaAtivaValidator.exigirAtiva(require(contas.buscarPorIdEUsuario(command.contaId(), usuarioId)));
		if (command.categoriaId() != null)
			require(categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId));
		if (command.meioPagamentoId() != null)
			require(meios.buscarPorIdEUsuario(command.meioPagamentoId(), usuarioId));
		if (command.diaDoMes() < 1 || command.diaDoMes() > 31)
			throw new DomainException("error.recorrencia.dia.invalido");
	}
}
