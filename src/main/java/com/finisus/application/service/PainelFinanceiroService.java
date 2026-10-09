package com.finisus.application.service;

import com.finisus.application.ports.in.DashboardFinanceiroUseCase;
import com.finisus.application.ports.in.ConsultarResumoDivisaoUseCase;
import com.finisus.application.ports.in.FaturaUseCase;
import com.finisus.application.ports.in.PainelFinanceiroUseCase;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.DashboardFinanceiroRepositoryPort;
import com.finisus.application.ports.out.DivisaoCompartilhadaRepositoryPort;
import com.finisus.application.ports.out.FaturaRepositoryPort;
import com.finisus.application.ports.out.InvestimentoRepositoryPort;
import com.finisus.application.ports.out.MovimentoInvestimentoRepositoryPort;
import com.finisus.application.ports.out.ObrigacaoFinanceiraRepositoryPort;
import com.finisus.application.ports.out.ParcelaFinanciamentoRepositoryPort;
import com.finisus.application.ports.out.PosicaoInvestimentoRepositoryPort;
import com.finisus.application.ports.out.RecorrenciaRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.StatusParcelaFinanciamento;
import com.finisus.domain.model.TipoMovimentoInvestimento;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.vo.AnoMes;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

public class PainelFinanceiroService implements PainelFinanceiroUseCase {
	private static final long MAXIMO_DIAS_CONSULTA = 366;
	private static final LocalDate INICIO_HISTORICO = LocalDate.of(1900, 1, 1);
	private final ContaRepositoryPort contas;
	private final InvestimentoRepositoryPort investimentos;
	private final MovimentoInvestimentoRepositoryPort movimentos;
	private final PosicaoInvestimentoRepositoryPort posicoes;
	private final FaturaRepositoryPort faturas;
	private final FaturaUseCase detalhesFatura;
	private final ObrigacaoFinanceiraRepositoryPort obrigacoes;
	private final ParcelaFinanciamentoRepositoryPort parcelas;
	private final RecorrenciaRepositoryPort recorrencias;
	private final TransacaoRepositoryPort transacoes;
	private final DashboardFinanceiroUseCase dashboard;
	private final DashboardFinanceiroRepositoryPort dadosDashboard;
	private final DivisaoCompartilhadaRepositoryPort divisoes;
	private final ConsultarResumoDivisaoUseCase resumosDivisao;
	private final UsuarioRepositoryPort usuarios;
	private final Clock clock;

	public PainelFinanceiroService(ContaRepositoryPort contas, InvestimentoRepositoryPort investimentos,
			MovimentoInvestimentoRepositoryPort movimentos, PosicaoInvestimentoRepositoryPort posicoes,
			FaturaRepositoryPort faturas, FaturaUseCase detalhesFatura, ObrigacaoFinanceiraRepositoryPort obrigacoes,
			ParcelaFinanciamentoRepositoryPort parcelas, RecorrenciaRepositoryPort recorrencias,
			TransacaoRepositoryPort transacoes, DashboardFinanceiroUseCase dashboard,
			DashboardFinanceiroRepositoryPort dadosDashboard, DivisaoCompartilhadaRepositoryPort divisoes,
			ConsultarResumoDivisaoUseCase resumosDivisao, UsuarioRepositoryPort usuarios, Clock clock) {
		this.contas = contas;
		this.investimentos = investimentos;
		this.movimentos = movimentos;
		this.posicoes = posicoes;
		this.faturas = faturas;
		this.detalhesFatura = detalhesFatura;
		this.obrigacoes = obrigacoes;
		this.parcelas = parcelas;
		this.recorrencias = recorrencias;
		this.transacoes = transacoes;
		this.dashboard = dashboard;
		this.dadosDashboard = dadosDashboard;
		this.divisoes = divisoes;
		this.resumosDivisao = resumosDivisao;
		this.usuarios = usuarios;
		this.clock = clock;
	}

	@Override
	@Transactional(readOnly = true)
	public VisaoGeral consultarVisaoGeral(Long usuarioId, LocalDate referencia, int janelaDias) {
		if (referencia == null || janelaDias < 1 || janelaDias > 90) {
			throw new DomainException("error.dashboard.visao.geral.invalida");
		}
		var mensal = dashboard.consultarMensal(usuarioId, AnoMes.from(referencia).formatado());
		BigDecimal ajusteInvestimentos = ajusteInvestimentosDoMes(usuarioId, referencia);
		BigDecimal saldoContas = saldoContas(usuarioId);
		AgendaFinanceira agenda = consultarAgendaComVencidos(usuarioId, referencia, referencia.plusDays(janelaDias));
		List<CategoriaGasto> maioresGastos = mensal.categorias().stream()
				.filter(categoria -> categoria.despesas().signum() > 0)
				.sorted(Comparator.comparing(DashboardFinanceiroUseCase.ResumoCategoria::despesas).reversed())
				.limit(3).map(categoria -> new CategoriaGasto(categoria.categoriaId(), categoria.categoriaNome(),
						categoria.despesas())).toList();
		List<AlertaFinanceiro> alertas = agenda.compromissos().stream()
				.filter(compromisso -> !compromisso.vencimento().isAfter(referencia.plusDays(7)))
				.map(compromisso -> new AlertaFinanceiro(compromisso.tipo(), compromisso.descricao(),
						compromisso.vencimento(), compromisso.valor(),
						compromisso.vencimento().isBefore(referencia) ? "VENCIDO" : "ATENCAO"))
				.limit(10).toList();
		return new VisaoGeral(referencia, saldoContas,
				mensal.resumo().resultadoCompetencia().add(ajusteInvestimentos), mensal.resumo().resultadoCaixa(),
				agenda.totalComprometido(), saldoContas.subtract(agenda.totalComprometido()), maioresGastos, alertas);
	}

	@Override
	@Transactional(readOnly = true)
	public AgendaFinanceira consultarAgenda(Long usuarioId, LocalDate inicio, LocalDate fim) {
		validarPeriodo(inicio, fim, "error.dashboard.agenda.periodo.invalido");
		List<CompromissoFinanceiro> compromissos = new ArrayList<>();
		obrigacoes.listarPendentesPorUsuarioEPeriodo(usuarioId, inicio, fim).stream()
				.filter(obrigacao -> entre(obrigacao.getDataVencimento(), inicio, fim)).forEach(obrigacao -> compromissos
				.add(new CompromissoFinanceiro("OBRIGACAO", obrigacao.getId(), obrigacao.getDescricao(),
						obrigacao.getDataVencimento(), obrigacao.getValor().valor(), obrigacao.getStatus().name())));
		var faturasDaAgenda = faturas.listarEmAbertoPorUsuario(usuarioId).stream()
				.filter(fatura -> entre(fatura.getDataVencimento(), inicio, fim)).toList();
		var valoresEmAberto = valoresEmAberto(usuarioId, faturasDaAgenda);
		faturasDaAgenda.forEach(fatura -> {
					var valorEmAberto = valoresEmAberto.getOrDefault(fatura.getId(), BigDecimal.ZERO);
					if (valorEmAberto.signum() > 0) compromissos.add(new CompromissoFinanceiro("FATURA",
							fatura.getId(), "Fatura " + fatura.getMesReferencia().formatado(), fatura.getDataVencimento(),
							valorEmAberto, fatura.getStatus().name()));
				});
		parcelas.listarPendentesPorUsuario(usuarioId).stream()
				.filter(parcela -> entre(parcela.getDataVencimento(), inicio, fim)).forEach(parcela -> compromissos
						.add(new CompromissoFinanceiro("PARCELA_FINANCIAMENTO", parcela.getId(),
								"Parcela de financiamento " + parcela.getNumero(), parcela.getDataVencimento(),
								parcela.getValor().valor(), parcela.getStatus().name())));
		transacoes.listarPorUsuarioEPeriodo(usuarioId, inicio, fim.plusDays(1)).stream()
				.filter(transacao -> transacao.getCompraParceladaId() != null && !transacao.isEstornada())
				.forEach(transacao -> compromissos.add(new CompromissoFinanceiro("COMPRA_PARCELADA", transacao.getId(),
						transacao.getDescricao(), transacao.getData(), transacao.getValor().valor(), "AGENDADA")));
		adicionarRecorrencias(usuarioId, inicio, fim, compromissos);
		List<CompromissoFinanceiro> ordenados = compromissos.stream()
				.sorted(Comparator.comparing(CompromissoFinanceiro::vencimento).thenComparing(CompromissoFinanceiro::tipo)
						.thenComparing(CompromissoFinanceiro::referenciaId)).toList();
		BigDecimal total = ordenados.stream().map(CompromissoFinanceiro::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new AgendaFinanceira(inicio, fim, total, ordenados);
	}

	private AgendaFinanceira consultarAgendaComVencidos(Long usuarioId, LocalDate referencia, LocalDate fim) {
		AgendaFinanceira agendaDaJanela = consultarAgenda(usuarioId, referencia, fim);
		List<CompromissoFinanceiro> compromissos = new ArrayList<>(agendaDaJanela.compromissos());
		LocalDate limiteVencidos = referencia.minusDays(1);
		obrigacoes.listarPendentesPorUsuarioEPeriodo(usuarioId, INICIO_HISTORICO, limiteVencidos).stream()
				.filter(obrigacao -> obrigacao.getDataVencimento().isBefore(referencia))
				.forEach(obrigacao -> compromissos.add(new CompromissoFinanceiro("OBRIGACAO", obrigacao.getId(),
						obrigacao.getDescricao(), obrigacao.getDataVencimento(), obrigacao.getValor().valor(),
						obrigacao.getStatus().name())));
		faturas.listarEmAbertoPorUsuario(usuarioId).stream()
				.filter(fatura -> fatura.getDataVencimento().isBefore(referencia)).forEach(fatura -> {
					var detalhe = detalhesFatura.buscarDetalhe(usuarioId, fatura.getId());
					if (detalhe.valorEmAberto().signum() > 0) compromissos.add(new CompromissoFinanceiro("FATURA",
							fatura.getId(), "Fatura " + fatura.getMesReferencia().formatado(), fatura.getDataVencimento(),
							detalhe.valorEmAberto(), fatura.getStatus().name()));
				});
		parcelas.listarPendentesPorUsuario(usuarioId).stream()
				.filter(parcela -> parcela.getDataVencimento().isBefore(referencia))
				.forEach(parcela -> compromissos.add(new CompromissoFinanceiro("PARCELA_FINANCIAMENTO", parcela.getId(),
						"Parcela de financiamento " + parcela.getNumero(), parcela.getDataVencimento(),
						parcela.getValor().valor(), parcela.getStatus().name())));
		transacoes.listarPorUsuarioEPeriodo(usuarioId, INICIO_HISTORICO, referencia).stream()
				.filter(transacao -> transacao.getCompraParceladaId() != null && !transacao.isEstornada()
						&& transacao.getData().isBefore(referencia))
				.forEach(transacao -> compromissos.add(new CompromissoFinanceiro("COMPRA_PARCELADA", transacao.getId(),
						transacao.getDescricao(), transacao.getData(), transacao.getValor().valor(), "VENCIDA")));
		List<CompromissoFinanceiro> ordenados = compromissos.stream()
				.sorted(Comparator.comparing(CompromissoFinanceiro::vencimento).thenComparing(CompromissoFinanceiro::tipo)
						.thenComparing(CompromissoFinanceiro::referenciaId)).toList();
		BigDecimal total = ordenados.stream().map(CompromissoFinanceiro::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new AgendaFinanceira(referencia, fim, total, ordenados);
	}

	@Override
	@Transactional(readOnly = true)
	public Patrimonio consultarPatrimonio(Long usuarioId, LocalDate referencia) {
		if (referencia == null) throw new DomainException("error.dashboard.patrimonio.referencia.invalida");
		Instant saldosEDividasConsultadosEm = clock.instant();
		var investimentosCadastrados = investimentos.listarPorUsuario(usuarioId);
		List<Long> investimentosIds = investimentosCadastrados.stream().map(investimento -> investimento.getId()).toList();
		var movimentosPorInvestimento = investimentosIds.isEmpty() ? Map.<Long, List<com.finisus.domain.model.MovimentoInvestimento>>of()
				: movimentos.listarPorInvestimentos(investimentosIds);
		var posicoesPorInvestimento = investimentosIds.isEmpty()
				? Map.<Long, com.finisus.domain.model.PosicaoInvestimento>of()
				: posicoes.buscarUltimasAte(investimentosIds, referencia);
		List<InvestimentoComSituacao> investimentosPatrimoniais = investimentosCadastrados.stream()
				.map(investimento -> {
					BigDecimal capital = capitalLiquido(movimentosPorInvestimento.getOrDefault(investimento.getId(), List.of()));
					var posicao = java.util.Optional.ofNullable(posicoesPorInvestimento.get(investimento.getId()));
					return new InvestimentoComSituacao(investimento.isAtivo(), investimento.getContaCustodiaId(),
							new PosicaoInvestimento(investimento.getId(),
							investimento.getNome(), investimento.getTipo().name(),
							capital, posicao.map(valor -> valor.getValor().valor()).orElse(capital),
							posicao.map(com.finisus.domain.model.PosicaoInvestimento::getDataReferencia).orElse(null),
							posicao.isPresent()));
				}).filter(investimento -> investimento.ativo()
						|| investimento.posicao().capitalLiquido().signum() != 0
						|| investimento.posicao().posicaoInformada())
				.toList();
		var contasCustodia = investimentosPatrimoniais.stream().map(InvestimentoComSituacao::contaCustodiaId)
				.filter(java.util.Objects::nonNull).collect(Collectors.toSet());
		BigDecimal saldoContas = saldoContas(usuarioId, contasCustodia);
		List<PosicaoInvestimento> investimentosDoUsuario = investimentosPatrimoniais.stream()
				.map(InvestimentoComSituacao::posicao).toList();
		BigDecimal capitalLiquido = investimentosDoUsuario.stream().map(PosicaoInvestimento::capitalLiquido)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal valorAtual = investimentosDoUsuario.stream().map(PosicaoInvestimento::valorAtual)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		var faturasAbertas = faturas.listarEmAbertoPorUsuario(usuarioId);
		BigDecimal faturasEmAberto = valoresEmAberto(usuarioId, faturasAbertas).values().stream()
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal obrigacoesEmAberto = obrigacoes.listarPendentesPorUsuarioEPeriodo(usuarioId, INICIO_HISTORICO,
				LocalDate.of(9999, 12, 31)).stream().map(obrigacao -> obrigacao.getValor().valor())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		List<com.finisus.domain.model.ParcelaFinanciamento> parcelasPendentes = parcelas
				.listarPendentesPorUsuario(usuarioId);
		BigDecimal parcelasEmAberto = parcelasPendentes.stream().map(parcela -> parcela.getValor().valor())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal principalFinanciamentos = parcelasPendentes.stream()
				.map(parcela -> parcela.getPrincipal() == null ? parcela.getValor().valor()
						: parcela.getPrincipal().valor())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal jurosEncargosFuturos = parcelasPendentes.stream().filter(parcela -> parcela.getPrincipal() != null)
				.map(parcela -> parcela.getJuros().valor().add(parcela.getEncargos().valor()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal parcelasSemComposicao = parcelasPendentes.stream().filter(parcela -> parcela.getPrincipal() == null)
				.map(parcela -> parcela.getValor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal comprasParceladas = transacoes
				.listarPorUsuarioEPeriodo(usuarioId, referencia, LocalDate.of(9999, 12, 31)).stream()
				.filter(transacao -> transacao.getCompraParceladaId() != null && !transacao.isEstornada())
				.map(transacao -> transacao.getValor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal dividasPrincipais = faturasEmAberto.add(obrigacoesEmAberto).add(principalFinanciamentos)
				.add(comprasParceladas);
		BigDecimal dividas = faturasEmAberto.add(obrigacoesEmAberto).add(parcelasEmAberto).add(comprasParceladas);
		return new Patrimonio(referencia, saldosEDividasConsultadosEm, false, saldoContas, capitalLiquido, valorAtual,
				faturasEmAberto, obrigacoesEmAberto,
				parcelasEmAberto, principalFinanciamentos, jurosEncargosFuturos, parcelasSemComposicao,
				comprasParceladas, dividasPrincipais, dividas, saldoContas.add(valorAtual).subtract(dividasPrincipais),
				investimentosDoUsuario);
	}

	@Override
	@Transactional(readOnly = true)
	public Compartilhados consultarCompartilhados(Long usuarioId, LocalDate inicio, LocalDate fim) {
		validarPeriodo(inicio, fim, "error.divisao.periodo.invalido");
		var divisoesAtivas = divisoes.listarAtivasPorParticipante(usuarioId);
		List<Long> participantesIds = divisoesAtivas.stream().flatMap(divisao -> divisao.getParticipantes().stream())
				.map(participante -> participante.usuarioId()).distinct().toList();
		Map<Long, String> nomesParticipantes = participantesIds.isEmpty() ? Map.of()
				: usuarios.buscarPorIds(participantesIds).stream()
						.collect(Collectors.toMap(usuario -> usuario.getId(), usuario -> usuario.getNome()));
		List<ResumoDivisaoCompartilhada> divisoesDoUsuario = divisoesAtivas.stream()
				.map(divisao -> resumoCompartilhado(usuarioId, inicio, fim, divisao.getId(), divisao.getNome(),
						nomesParticipantes))
				.toList();
		BigDecimal aReceber = divisoesDoUsuario.stream().map(ResumoDivisaoCompartilhada::meuSaldo)
				.filter(saldo -> saldo.signum() > 0).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal aPagar = divisoesDoUsuario.stream().map(ResumoDivisaoCompartilhada::meuSaldo)
				.filter(saldo -> saldo.signum() < 0).map(BigDecimal::negate).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new Compartilhados(inicio, fim, aReceber, aPagar, divisoesDoUsuario);
	}

	@Override
	@Transactional(readOnly = true)
	public AnaliseReceitasGastos consultarReceitasEGastos(Long usuarioId, String mesFinal, int periodoMeses) {
		var resumo = dashboard.consultarResumoPeriodo(usuarioId, mesFinal, periodoMeses);
		YearMonth inicio = YearMonth.parse(resumo.mesInicial());
		YearMonth fim = YearMonth.parse(resumo.mesFinal());
		List<CategoriaAnalise> categorias = dadosDashboard
				.consultarCategoriasPeriodo(usuarioId, resumo.mesInicial(), resumo.mesFinal()).stream()
				.map(categoria -> new CategoriaAnalise(categoria.categoriaId(), categoria.categoriaNome(),
						categoria.receitas(), categoria.despesas(), categoria.saldo())).toList();
		BigDecimal receitas = resumo.totais().receitas();
		BigDecimal despesas = resumo.totais().despesas();
		return new AnaliseReceitasGastos(resumo.mesInicial(), resumo.mesFinal(), periodoMeses, receitas, despesas,
				receitas.subtract(despesas), despesasFixasPrevistas(usuarioId, inicio, fim), categorias);
	}

	private void adicionarRecorrencias(Long usuarioId, LocalDate inicio, LocalDate fim,
			List<CompromissoFinanceiro> compromissos) {
		var recorrenciasAtivas = recorrencias.listarAtivasPorUsuario(usuarioId);
		YearMonth competencia = YearMonth.from(inicio);
		YearMonth ultimaCompetencia = YearMonth.from(fim);
		var chavesGeradas = recorrencias.listarGeracoesExistentes(
				recorrenciasAtivas.stream().map(com.finisus.domain.model.Recorrencia::getId).toList(),
				AnoMes.parse(competencia.toString()), AnoMes.parse(ultimaCompetencia.toString()));
		while (!competencia.isAfter(ultimaCompetencia)) {
			AnoMes anoMes = AnoMes.parse(competencia.toString());
			for (var recorrencia : recorrenciasAtivas) {
				LocalDate vencimento = competencia.atDay(Math.min(recorrencia.getDiaDoMes(), competencia.lengthOfMonth()));
				if (recorrencia.getTipo() == TipoTransacao.SAIDA && entre(vencimento, inicio, fim)
						&& !chavesGeradas.contains(new RecorrenciaRepositoryPort.ChaveGeracao(recorrencia.getId(), anoMes))) {
					compromissos.add(new CompromissoFinanceiro("RECORRENCIA", recorrencia.getId(), recorrencia.getNome(),
							vencimento, recorrencia.getValorEsperado().valor(), "PREVISTA"));
				}
			}
			competencia = competencia.plusMonths(1);
		}
	}

	private ResumoDivisaoCompartilhada resumoCompartilhado(Long usuarioId, LocalDate inicio, LocalDate fim,
			Long divisaoId, String divisao, Map<Long, String> nomesParticipantes) {
		var resumo = resumosDivisao.consultar(usuarioId, divisaoId, inicio, fim);
		List<ParticipanteCompartilhado> participantes = resumo.participantes().stream().map(participante ->
				new ParticipanteCompartilhado(participante.usuarioId(), nomesParticipantes.get(participante.usuarioId()),
						participante.pago().valor(),
						participante.devido().valor(), participante.saldo())).toList();
		ParticipanteCompartilhado usuario = participantes.stream()
				.filter(participante -> usuarioId.equals(participante.usuarioId())).findFirst()
				.orElseThrow(() -> new DomainException("error.recurso.sem.permissao"));
		return new ResumoDivisaoCompartilhada(resumo.divisaoId(), divisao, resumo.total().valor(), usuario.pago(),
				usuario.devido(), usuario.saldo(), participantes);
	}

	private BigDecimal despesasFixasPrevistas(Long usuarioId, YearMonth inicio, YearMonth fim) {
		long meses = inicio.until(fim, java.time.temporal.ChronoUnit.MONTHS) + 1;
		return recorrencias.listarAtivasPorUsuario(usuarioId).stream()
				.filter(recorrencia -> recorrencia.getTipo() == TipoTransacao.SAIDA)
				.map(recorrencia -> recorrencia.getValorEsperado().valor().multiply(BigDecimal.valueOf(meses)))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal saldoContas(Long usuarioId) {
		return saldoContas(usuarioId, java.util.Set.of());
	}

	private BigDecimal saldoContas(Long usuarioId, java.util.Set<Long> contasCustodia) {
		return contas.listarPorUsuario(usuarioId).stream()
				.filter(conta -> conta.isAtivo() || conta.getSaldo().valor().signum() != 0)
				.filter(conta -> !contasCustodia.contains(conta.getId()))
				.map(conta -> conta.getSaldo().valor())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private record InvestimentoComSituacao(boolean ativo, Long contaCustodiaId, PosicaoInvestimento posicao) {
	}

	private BigDecimal capitalLiquido(List<com.finisus.domain.model.MovimentoInvestimento> movimentosInvestimento) {
		return movimentosInvestimento.stream().filter(movimento -> movimento.getEstornadoEm() == null)
				.map(this::variacaoCapital).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal ajusteInvestimentosDoMes(Long usuarioId, LocalDate referencia) {
		LocalDate inicio = AnoMes.from(referencia).primeiroDia();
		LocalDate fim = AnoMes.from(referencia).proximo().primeiroDia();
		return movimentosDosInvestimentos(usuarioId).stream().filter(movimento -> movimento.getEstornadoEm() == null)
				.filter(movimento -> !movimento.getData().isBefore(inicio) && movimento.getData().isBefore(fim))
				.map(this::variacaoCapital).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal variacaoCapital(com.finisus.domain.model.MovimentoInvestimento movimento) {
		return switch (movimento.getTipo()) {
			case APORTE -> movimento.getValor().valor();
			case RESGATE -> movimento.getValor().valor().negate();
			case RENDIMENTO_REALIZADO, TAXA -> BigDecimal.ZERO;
		};
	}

	private List<com.finisus.domain.model.MovimentoInvestimento> movimentosDosInvestimentos(Long usuarioId) {
		List<Long> investimentosIds = investimentos.listarPorUsuario(usuarioId).stream()
				.map(investimento -> investimento.getId()).toList();
		if (investimentosIds.isEmpty()) return List.of();
		return movimentos.listarPorInvestimentos(investimentosIds).values().stream().flatMap(List::stream).toList();
	}

	private boolean entre(LocalDate data, LocalDate inicio, LocalDate fim) {
		return !data.isBefore(inicio) && !data.isAfter(fim);
	}

	private Map<Long, BigDecimal> valoresEmAberto(Long usuarioId, List<com.finisus.domain.model.Fatura> faturasAbertas) {
		if (faturasAbertas.isEmpty()) return Map.of();
		return detalhesFatura.buscarValoresEmAberto(usuarioId,
				faturasAbertas.stream().map(fatura -> fatura.getId()).toList());
	}

	private void validarPeriodo(LocalDate inicio, LocalDate fim, String codigoErro) {
		if (inicio == null || fim == null || fim.isBefore(inicio)
				|| java.time.temporal.ChronoUnit.DAYS.between(inicio, fim) >= MAXIMO_DIAS_CONSULTA) {
			throw new DomainException(codigoErro);
		}
	}
}
