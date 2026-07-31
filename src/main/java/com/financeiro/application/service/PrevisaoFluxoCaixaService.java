package com.financeiro.application.service;

import com.financeiro.application.ports.in.PrevisaoFluxoCaixaUseCase;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.out.PrevisaoMensalRepositoryPort;
import com.financeiro.application.ports.out.RecorrenciaRepositoryPort;
import com.financeiro.application.ports.out.TransacaoRepositoryPort;
import com.financeiro.application.ports.out.ParcelaFinanciamentoRepositoryPort;
import com.financeiro.application.ports.out.ObterDataAtualPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.*;
import com.financeiro.domain.vo.AnoMes;
import com.financeiro.domain.vo.ValorMonetario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class PrevisaoFluxoCaixaService implements PrevisaoFluxoCaixaUseCase {
	private final PrevisaoMensalRepositoryPort previsoes;
	private final RecorrenciaRepositoryPort recorrencias;
	private final TransacaoRepositoryPort transacoes;
	private final ParcelaFinanciamentoRepositoryPort parcelas;
	private final ObterDataAtualPort dataAtual;

	public PrevisaoFluxoCaixaService(PrevisaoMensalRepositoryPort previsoes, RecorrenciaRepositoryPort recorrencias,
			TransacaoRepositoryPort transacoes, ParcelaFinanciamentoRepositoryPort parcelas,
			ObterDataAtualPort dataAtual) {
		this.previsoes = previsoes;
		this.recorrencias = recorrencias;
		this.transacoes = transacoes;
		this.parcelas = parcelas;
		this.dataAtual = dataAtual;
	}

	public List<PrevisaoMensal> recalcular(Long usuarioId, int meses) {
		if (meses < 1 || meses > 12)
			throw new DomainException("error.previsao.periodo.invalido");
		AnoMes inicio = AnoMes.from(dataAtual.obter());
		AnoMes fim = inicio;
		for (int indice = 1; indice < meses; indice++)
			fim = fim.proximo();
		List<PrevisaoMensal> resultado = new ArrayList<>();
		List<Recorrencia> ativas = recorrencias.listarAtivasPorUsuario(usuarioId);
		List<Transacao> conhecidas = transacoes.listarPorUsuario(usuarioId);
		Map<AnoMes, List<ParcelaFinanciamento>> parcelasPorCompetencia = parcelas
				.listarPorUsuarioEPeriodo(usuarioId, inicio, fim).stream()
				.collect(java.util.stream.Collectors.groupingBy(parcela -> AnoMes.from(parcela.getDataVencimento())));
		for (int indice = 0; indice < meses; indice++) {
			AnoMes periodo = inicio;
			for (int passo = 0; passo < indice; passo++)
				periodo = periodo.proximo();
			Map<Long, Totais> totais = new HashMap<>();
			for (Recorrencia recorrencia : ativas)
				adicionar(totais, recorrencia.getCategoriaId(), recorrencia.getTipo(),
						recorrencia.getValorEsperado().valor());
			for (Transacao transacao : conhecidas)
				if (AnoMes.from(transacao.getData()).equals(periodo) && transacao.getRecorrenciaId() == null
						&& !transacao.isEstornada())
					adicionar(totais, transacao.getCategoriaId(), transacao.getTipo(), transacao.getValor().valor());
			for (ParcelaFinanciamento parcela : parcelasPorCompetencia.getOrDefault(periodo, List.of())) {
				adicionar(totais, null, TipoTransacao.SAIDA, parcela.getValor().valor());
			}
			AnoMes mesAtual = periodo;
			List<PrevisaoMensal> mensal = totais.entrySet().stream()
					.map(e -> PrevisaoMensal.calcular(usuarioId, mesAtual, e.getKey(),
							ValorMonetario.of(e.getValue().entrada), ValorMonetario.of(e.getValue().saida),
							dataAtual.obterDataHora()))
					.toList();
			previsoes.substituir(usuarioId, mesAtual, mensal);
			resultado.addAll(mensal);
		}
		return resultado;
	}

	public List<PrevisaoMensal> consultar(Long usuarioId, String anoMes) {
		return previsoes.listar(usuarioId, AnoMes.parse(anoMes));
	}

	public Pagina<PrevisaoMensal> consultar(Long usuarioId, String anoMes, Paginacao paginacao) {
		return previsoes.listar(usuarioId, AnoMes.parse(anoMes), paginacao);
	}

	private void adicionar(Map<Long, Totais> totais, Long categoriaId, TipoTransacao tipo, BigDecimal valor) {
		Totais atual = totais.computeIfAbsent(categoriaId, ignored -> new Totais());
		if (tipo == TipoTransacao.ENTRADA)
			atual.entrada = atual.entrada.add(valor);
		else
			atual.saida = atual.saida.add(valor);
	}

	private static final class Totais {
		BigDecimal entrada = BigDecimal.ZERO;
		BigDecimal saida = BigDecimal.ZERO;
	}
}
