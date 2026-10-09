package com.finisus.adapters.out.persistence;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase.ConsultaBalancete;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase.ConsultaBalanceteAnual;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase.LinhaBalancete;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase.OrigemLinha;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase.ResumoCategoria;
import com.finisus.application.ports.in.DashboardFinanceiroUseCase.OrigemResumo;
import com.finisus.application.ports.out.DashboardFinanceiroRepositoryPort;
import com.finisus.domain.model.TipoTransacao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class DashboardFinanceiroPersistenceAdapter implements DashboardFinanceiroRepositoryPort {
	private final EntityManager entityManager;

	public DashboardFinanceiroPersistenceAdapter(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public DadosMensais consultarMensal(Long usuarioId, String anoMes) {
		List<ResumoCategoria> categorias = consultarCategorias(usuarioId, anoMes);
		BigDecimal gastosFaturas = valorFaturas(usuarioId, anoMes);
		BigDecimal valorPago = valorFaturasPago(usuarioId, anoMes);
		return new DadosMensais(categorias, gastosFaturas, valorPago, gastosFaturas.subtract(valorPago).max(BigDecimal.ZERO),
				resultadoCaixa(usuarioId, anoMes));
	}

	@Override
	public List<ValorPorOrigem> consultarResumoPeriodo(Long usuarioId, String mesInicial, String mesFinal) {
		Query query = entityManager.createNativeQuery("""
				SELECT CASE WHEN EXISTS (SELECT 1 FROM movimento_investimento mi
				                             WHERE mi.transacao_id = t.id AND mi.estornado_em IS NULL) THEN 'INVESTIMENTO'
				            WHEN t.fatura_id IS NOT NULL THEN 'CARTAO'
				            WHEN t.fatura_pagamento_id IS NOT NULL THEN 'PAGAMENTO_FATURA'
				            WHEN t.recorrencia_id IS NOT NULL THEN 'RECORRENCIA'
				            WHEN t.compra_parcelada_id IS NOT NULL THEN 'COMPRA_PARCELADA'
				            ELSE 'MOVIMENTACAO_DIRETA' END,
				       t.tipo, COALESCE(SUM(t.valor), 0)
				FROM transacao t
				LEFT JOIN fatura f ON f.id = t.fatura_id
				WHERE t.usuario_id = :usuarioId AND t.estornado_em IS NULL AND t.transferencia_id IS NULL AND (
					(t.fatura_id IS NULL AND t.fatura_pagamento_id IS NULL AND t.data >= :inicio AND t.data < :fim)
					OR (t.fatura_id IS NOT NULL AND f.ano_mes >= :mesInicial AND f.ano_mes <= :mesFinal
					    AND f.status <> 'CANCELADA')
					OR (t.fatura_pagamento_id IS NOT NULL AND t.data >= :inicio AND t.data < :fim)
				)
				GROUP BY 1, 2
				""");
		YearMonth inicio = YearMonth.parse(mesInicial);
		YearMonth fim = YearMonth.parse(mesFinal).plusMonths(1);
		query.setParameter("usuarioId", usuarioId).setParameter("mesInicial", mesInicial).setParameter("mesFinal", mesFinal)
				.setParameter("inicio", inicio.atDay(1)).setParameter("fim", fim.atDay(1));
		@SuppressWarnings("unchecked")
		List<Object[]> linhas = query.getResultList();
		return linhas.stream().map(linha -> new ValorPorOrigem(OrigemResumo.valueOf((String) linha[0]),
				TipoTransacao.valueOf((String) linha[1]), decimal(linha[2]))).toList();
	}

	@Override
	public List<ValorMensal> consultarResumoAnual(Long usuarioId, String ano) {
		Query query = entityManager.createNativeQuery("""
				SELECT f.ano_mes, MONTH(t.data),
				       CASE WHEN EXISTS (SELECT 1 FROM movimento_investimento mi
				                                WHERE mi.transacao_id = t.id AND mi.estornado_em IS NULL) THEN 'INVESTIMENTO'
				            WHEN t.fatura_id IS NOT NULL THEN 'CARTAO'
				            WHEN t.fatura_pagamento_id IS NOT NULL THEN 'PAGAMENTO_FATURA'
				            WHEN t.recorrencia_id IS NOT NULL THEN 'RECORRENCIA'
				            WHEN t.compra_parcelada_id IS NOT NULL THEN 'COMPRA_PARCELADA'
				            ELSE 'MOVIMENTACAO_DIRETA' END,
				       t.tipo, COALESCE(SUM(t.valor), 0)
				FROM transacao t
				LEFT JOIN fatura f ON f.id = t.fatura_id
				WHERE t.usuario_id = :usuarioId AND t.estornado_em IS NULL AND t.transferencia_id IS NULL AND (
					(t.fatura_id IS NULL AND t.fatura_pagamento_id IS NULL AND t.data >= :inicio AND t.data < :fim)
					OR (t.fatura_id IS NOT NULL AND f.ano_mes >= :mesInicial AND f.ano_mes <= :mesFinal
					    AND f.status <> 'CANCELADA')
					OR (t.fatura_pagamento_id IS NOT NULL AND t.data >= :inicio AND t.data < :fim)
				)
				GROUP BY 1, 2, 3, 4
				""");
		aplicarParametrosAno(query, usuarioId, ano);
		@SuppressWarnings("unchecked")
		List<Object[]> linhas = query.getResultList();
		return linhas.stream().map(linha -> new ValorMensal(linha[0] == null ? null : (String) linha[0],
				numero(linha[1]).intValue(), OrigemResumo.valueOf((String) linha[2]),
				TipoTransacao.valueOf((String) linha[3]), decimal(linha[4]))).toList();
	}

	@Override
	public List<ResumoCategoria> consultarCategoriasAnual(Long usuarioId, String ano) {
		return consultarCategorias(usuarioId, condicaoCompetenciaAnual(), query -> aplicarParametrosAno(query, usuarioId, ano));
	}

	@Override
	public List<ResumoCategoria> consultarCategoriasPeriodo(Long usuarioId, String mesInicial, String mesFinal) {
		return consultarCategorias(usuarioId, condicaoCompetenciaPeriodo(),
				query -> aplicarParametrosPeriodo(query, usuarioId, mesInicial, mesFinal));
	}

	@Override
	public Pagina<LinhaBalancete> consultarBalancete(Long usuarioId, ConsultaBalancete consulta, Paginacao paginacao) {
		String filtros = filtrosBalancete(consulta);
		Query query = entityManager.createNativeQuery("""
				SELECT t.id, t.data, t.descricao, t.tipo, t.valor, t.conta_id, t.categoria_id,
				       COALESCE(c.nome, 'Sem categoria'), t.fatura_id, t.fatura_pagamento_id
				FROM transacao t
				LEFT JOIN fatura fg ON fg.id = t.fatura_id
				LEFT JOIN fatura fp ON fp.id = t.fatura_pagamento_id
				LEFT JOIN categoria c ON c.id = t.categoria_id
				WHERE """ + condicaoBalancete() + filtros + " ORDER BY t.data DESC, t.id DESC");
		aplicarParametros(query, usuarioId, consulta);
		query.setFirstResult(paginacao.pagina() * paginacao.tamanho());
		query.setMaxResults(paginacao.tamanho());
		@SuppressWarnings("unchecked")
		List<Object[]> linhas = query.getResultList();
		Query count = entityManager.createNativeQuery("SELECT COUNT(t.id) FROM transacao t LEFT JOIN fatura fg ON fg.id = t.fatura_id "
				+ "LEFT JOIN fatura fp ON fp.id = t.fatura_pagamento_id WHERE " + condicaoBalancete() + filtros);
		aplicarParametros(count, usuarioId, consulta);
		long total = numero(count.getSingleResult()).longValue();
		int totalPaginas = total == 0 ? 0 : (int) Math.ceil((double) total / paginacao.tamanho());
		return new Pagina<>(linhas.stream().map(this::linha).toList(), paginacao.pagina(), paginacao.tamanho(), total,
				totalPaginas);
	}

	@Override
	public Pagina<LinhaBalancete> consultarBalanceteAnual(Long usuarioId, ConsultaBalanceteAnual consulta,
			Paginacao paginacao) {
		String filtros = filtrosBalancete(consulta);
		Query query = entityManager.createNativeQuery("""
				SELECT t.id, t.data, t.descricao, t.tipo, t.valor, t.conta_id, t.categoria_id,
				       COALESCE(c.nome, 'Sem categoria'), t.fatura_id, t.fatura_pagamento_id
				FROM transacao t
				LEFT JOIN fatura fg ON fg.id = t.fatura_id
				LEFT JOIN categoria c ON c.id = t.categoria_id
				WHERE """ + condicaoBalanceteAnual() + filtros + " ORDER BY t.data DESC, t.id DESC");
		aplicarParametros(query, usuarioId, consulta);
		query.setFirstResult(paginacao.pagina() * paginacao.tamanho());
		query.setMaxResults(paginacao.tamanho());
		@SuppressWarnings("unchecked")
		List<Object[]> linhas = query.getResultList();
		Query count = entityManager.createNativeQuery("SELECT COUNT(t.id) FROM transacao t LEFT JOIN fatura fg ON fg.id = t.fatura_id "
				+ "WHERE " + condicaoBalanceteAnual() + filtros);
		aplicarParametros(count, usuarioId, consulta);
		long total = numero(count.getSingleResult()).longValue();
		int totalPaginas = total == 0 ? 0 : (int) Math.ceil((double) total / paginacao.tamanho());
		return new Pagina<>(linhas.stream().map(this::linha).toList(), paginacao.pagina(), paginacao.tamanho(), total,
				totalPaginas);
	}

	@Override
	public Totais consultarTotaisBalancete(Long usuarioId, ConsultaBalancete consulta) {
		String filtros = filtrosBalancete(consulta);
		Query query = entityManager.createNativeQuery("""
				SELECT t.tipo,
				       CASE WHEN EXISTS (SELECT 1 FROM movimento_investimento mi
				                                WHERE mi.transacao_id = t.id AND mi.estornado_em IS NULL) THEN 'INVESTIMENTO'
				            WHEN t.fatura_id IS NOT NULL THEN 'FATURA'
				            WHEN t.fatura_pagamento_id IS NOT NULL THEN 'PAGAMENTO_FATURA'
				            WHEN t.compra_parcelada_id IS NOT NULL THEN 'COMPRA_PARCELADA'
				            ELSE 'DIRETA' END,
				       COALESCE(SUM(t.valor), 0)
				FROM transacao t
				LEFT JOIN fatura fg ON fg.id = t.fatura_id
				LEFT JOIN fatura fp ON fp.id = t.fatura_pagamento_id
				WHERE """ + condicaoBalancete() + filtros + " GROUP BY t.tipo, 2");
		aplicarParametros(query, usuarioId, consulta);
		return totais(query);
	}

	@Override
	public Totais consultarTotaisBalanceteAnual(Long usuarioId, ConsultaBalanceteAnual consulta) {
		String filtros = filtrosBalancete(consulta);
		Query query = entityManager.createNativeQuery("""
				SELECT t.tipo,
				       CASE WHEN EXISTS (SELECT 1 FROM movimento_investimento mi
				                                WHERE mi.transacao_id = t.id AND mi.estornado_em IS NULL) THEN 'INVESTIMENTO'
				            WHEN t.fatura_id IS NOT NULL THEN 'FATURA'
				            WHEN t.fatura_pagamento_id IS NOT NULL THEN 'PAGAMENTO_FATURA'
				            WHEN t.compra_parcelada_id IS NOT NULL THEN 'COMPRA_PARCELADA'
				            ELSE 'DIRETA' END,
				       COALESCE(SUM(t.valor), 0)
				FROM transacao t
				LEFT JOIN fatura fg ON fg.id = t.fatura_id
				WHERE """ + condicaoBalanceteAnual() + filtros + " GROUP BY t.tipo, 2");
		aplicarParametros(query, usuarioId, consulta);
		return totais(query);
	}

	private Totais totais(Query query) {
		@SuppressWarnings("unchecked")
		List<Object[]> linhas = query.getResultList();
		BigDecimal receitas = BigDecimal.ZERO;
		BigDecimal gastosDiretos = BigDecimal.ZERO;
		BigDecimal gastosFaturas = BigDecimal.ZERO;
		BigDecimal pagamentosFaturas = BigDecimal.ZERO;
		BigDecimal comprasParceladas = BigDecimal.ZERO;
		BigDecimal resultadoCaixaInvestimentos = BigDecimal.ZERO;
		for (Object[] linha : linhas) {
			BigDecimal valor = decimal(linha[2]);
			String origem = (String) linha[1];
			if ("INVESTIMENTO".equals(origem)) {
				resultadoCaixaInvestimentos = TipoTransacao.ENTRADA.name().equals(linha[0])
						? resultadoCaixaInvestimentos.add(valor) : resultadoCaixaInvestimentos.subtract(valor);
			} else if ("FATURA".equals(origem)) gastosFaturas = gastosFaturas.add(valor);
			else if ("PAGAMENTO_FATURA".equals(origem)) pagamentosFaturas = pagamentosFaturas.add(valor);
			else if ("COMPRA_PARCELADA".equals(origem)) comprasParceladas = comprasParceladas.add(valor);
			else if (TipoTransacao.ENTRADA.name().equals(linha[0])) receitas = receitas.add(valor);
			else gastosDiretos = gastosDiretos.add(valor);
		}
		return new Totais(receitas, gastosDiretos, gastosFaturas, pagamentosFaturas, comprasParceladas,
				resultadoCaixaInvestimentos);
	}

	private List<ResumoCategoria> consultarCategorias(Long usuarioId, String anoMes) {
		return consultarCategorias(usuarioId, condicaoCompetencia(), query -> aplicarParametrosMes(query, usuarioId, anoMes));
	}

	private List<ResumoCategoria> consultarCategorias(Long usuarioId, String condicaoCompetencia,
			Consumer<Query> aplicarParametros) {
		String sql = """
				SELECT valores.categoria_id, COALESCE(c.nome, 'Sem categoria'), valores.tipo, SUM(valores.valor)
				FROM (
					SELECT COALESCE(i.categoria_id, t.categoria_id) AS categoria_id, t.tipo, i.valor
					FROM transacao t
					LEFT JOIN fatura f ON f.id = t.fatura_id
					JOIN transacao_item i ON i.transacao_id = t.id
					WHERE """ + condicaoCompetencia + """
					UNION ALL
					SELECT t.categoria_id, t.tipo, t.valor
					FROM transacao t
					LEFT JOIN fatura f ON f.id = t.fatura_id
					WHERE """ + condicaoCompetencia + " AND NOT EXISTS " + """
						(SELECT 1 FROM transacao_item i WHERE i.transacao_id = t.id)
				) valores
				LEFT JOIN categoria c ON c.id = valores.categoria_id
				GROUP BY valores.categoria_id, c.nome, valores.tipo
				ORDER BY c.nome, valores.categoria_id
				""";
		Query query = entityManager.createNativeQuery(sql);
		aplicarParametros.accept(query);
		@SuppressWarnings("unchecked")
		List<Object[]> linhas = query.getResultList();
		Map<Long, AcumuladoCategoria> acumulados = new LinkedHashMap<>();
		for (Object[] linha : linhas) {
			Long categoriaId = linha[0] == null ? null : numero(linha[0]).longValue();
			AcumuladoCategoria acumulado = acumulados.computeIfAbsent(categoriaId,
					_ -> new AcumuladoCategoria((String) linha[1]));
			if (TipoTransacao.ENTRADA.name().equals(linha[2])) acumulado.entradas = acumulado.entradas.add(decimal(linha[3]));
			else acumulado.saidas = acumulado.saidas.add(decimal(linha[3]));
		}
		return acumulados.entrySet().stream().map(entry -> new ResumoCategoria(entry.getKey(), entry.getValue().nome,
				entry.getValue().entradas, entry.getValue().saidas, entry.getValue().entradas.subtract(entry.getValue().saidas)))
				.toList();
	}

	private BigDecimal valorFaturas(Long usuarioId, String anoMes) {
		Query query = entityManager.createNativeQuery("""
				SELECT COALESCE(SUM(t.valor), 0) FROM transacao t
				JOIN fatura f ON f.id = t.fatura_id
				WHERE t.usuario_id = :usuarioId AND t.estornado_em IS NULL
				  AND f.ano_mes = :anoMes AND f.status <> 'CANCELADA'
				""");
		query.setParameter("usuarioId", usuarioId).setParameter("anoMes", anoMes);
		return decimal(query.getSingleResult());
	}

	private BigDecimal valorFaturasPago(Long usuarioId, String anoMes) {
		Query query = entityManager.createNativeQuery("""
				SELECT COALESCE(SUM(t.valor), 0) FROM transacao t
				JOIN fatura f ON f.id = t.fatura_pagamento_id
				WHERE t.usuario_id = :usuarioId AND t.estornado_em IS NULL AND f.ano_mes = :anoMes
				""");
		query.setParameter("usuarioId", usuarioId).setParameter("anoMes", anoMes);
		return decimal(query.getSingleResult());
	}

	private BigDecimal resultadoCaixa(Long usuarioId, String anoMes) {
		Query query = entityManager.createNativeQuery("""
				SELECT COALESCE(SUM(CASE WHEN t.tipo = 'ENTRADA' THEN t.valor ELSE -t.valor END), 0)
				FROM transacao t
				WHERE t.usuario_id = :usuarioId AND t.estornado_em IS NULL AND t.transferencia_id IS NULL AND t.fatura_id IS NULL
				  AND t.compra_parcelada_id IS NULL
				  AND t.data >= :inicio AND t.data < :fim
				""");
		YearMonth periodo = YearMonth.parse(anoMes);
		query.setParameter("usuarioId", usuarioId).setParameter("inicio", periodo.atDay(1))
				.setParameter("fim", periodo.plusMonths(1).atDay(1));
		return decimal(query.getSingleResult());
	}

	private String condicaoCompetencia() {
		return " t.usuario_id = :usuarioId AND t.estornado_em IS NULL AND t.transferencia_id IS NULL AND ((t.fatura_id IS NULL "
				+ "AND t.fatura_pagamento_id IS NULL AND t.data >= :inicio AND t.data < :fim) "
				+ "OR (t.fatura_id IS NOT NULL AND f.ano_mes = :anoMes AND f.status <> 'CANCELADA')) "
				+ excluirMovimentosInvestimento();
	}

	private String condicaoCompetenciaAnual() {
		return " t.usuario_id = :usuarioId AND t.estornado_em IS NULL AND t.transferencia_id IS NULL AND ((t.fatura_id IS NULL "
				+ "AND t.fatura_pagamento_id IS NULL AND t.data >= :inicio AND t.data < :fim) "
				+ "OR (t.fatura_id IS NOT NULL AND f.ano_mes >= :mesInicial AND f.ano_mes <= :mesFinal "
				+ "AND f.status <> 'CANCELADA')) " + excluirMovimentosInvestimento();
	}

	private String condicaoCompetenciaPeriodo() {
		return " t.usuario_id = :usuarioId AND t.estornado_em IS NULL AND t.transferencia_id IS NULL AND ((t.fatura_id IS NULL AND t.fatura_pagamento_id IS NULL "
				+ "AND t.data >= :inicio AND t.data < :fim) OR (t.fatura_id IS NOT NULL "
				+ "AND f.ano_mes >= :mesInicial AND f.ano_mes <= :mesFinal AND f.status <> 'CANCELADA')) "
				+ excluirMovimentosInvestimento();
	}

	private String excluirMovimentosInvestimento() {
		return " AND NOT EXISTS (SELECT 1 FROM movimento_investimento mi WHERE mi.transacao_id = t.id "
				+ "AND mi.estornado_em IS NULL) ";
	}

	private String condicaoBalancete() {
		return " t.usuario_id = :usuarioId AND t.estornado_em IS NULL AND t.transferencia_id IS NULL AND ((t.fatura_id IS NULL "
				+ "AND t.fatura_pagamento_id IS NULL AND t.data >= :inicio AND t.data < :fim) "
				+ "OR (t.fatura_id IS NOT NULL AND fg.ano_mes = :anoMes AND fg.status <> 'CANCELADA') "
				+ "OR (t.fatura_pagamento_id IS NOT NULL AND fp.ano_mes = :anoMes)) ";
	}

	private String condicaoBalanceteAnual() {
		return " t.usuario_id = :usuarioId AND t.estornado_em IS NULL AND t.transferencia_id IS NULL AND ((t.fatura_id IS NULL "
				+ "AND t.fatura_pagamento_id IS NULL AND t.data >= :inicio AND t.data < :fim) "
				+ "OR (t.fatura_id IS NOT NULL AND fg.ano_mes >= :mesInicial AND fg.ano_mes <= :mesFinal "
				+ "AND fg.status <> 'CANCELADA') "
				+ "OR (t.fatura_pagamento_id IS NOT NULL AND t.data >= :inicio AND t.data < :fim)) ";
	}

	private String filtrosBalancete(ConsultaBalancete consulta) {
		return filtrosBalancete(consulta.tipo(), consulta.categoriaId(), consulta.contaId());
	}

	private String filtrosBalancete(ConsultaBalanceteAnual consulta) {
		return filtrosBalancete(consulta.tipo(), consulta.categoriaId(), consulta.contaId());
	}

	private String filtrosBalancete(TipoTransacao tipo, Long categoriaId, Long contaId) {
		StringBuilder filtros = new StringBuilder();
		if (tipo != null) filtros.append(" AND t.tipo = :tipo");
		if (categoriaId != null) filtros.append(" AND t.categoria_id = :categoriaId");
		if (contaId != null) filtros.append(" AND t.conta_id = :contaId");
		return filtros.toString();
	}

	private void aplicarParametros(Query query, Long usuarioId, ConsultaBalancete consulta) {
		aplicarParametrosMes(query, usuarioId, consulta.anoMes());
		if (consulta.tipo() != null) query.setParameter("tipo", consulta.tipo().name());
		if (consulta.categoriaId() != null) query.setParameter("categoriaId", consulta.categoriaId());
		if (consulta.contaId() != null) query.setParameter("contaId", consulta.contaId());
	}

	private void aplicarParametros(Query query, Long usuarioId, ConsultaBalanceteAnual consulta) {
		aplicarParametrosAno(query, usuarioId, consulta.ano());
		if (consulta.tipo() != null) query.setParameter("tipo", consulta.tipo().name());
		if (consulta.categoriaId() != null) query.setParameter("categoriaId", consulta.categoriaId());
		if (consulta.contaId() != null) query.setParameter("contaId", consulta.contaId());
	}

	private void aplicarParametrosMes(Query query, Long usuarioId, String anoMes) {
		YearMonth periodo = YearMonth.parse(anoMes);
		query.setParameter("usuarioId", usuarioId).setParameter("anoMes", anoMes)
				.setParameter("inicio", periodo.atDay(1)).setParameter("fim", periodo.plusMonths(1).atDay(1));
	}

	private void aplicarParametrosAno(Query query, Long usuarioId, String ano) {
		Year periodo = Year.parse(ano);
		query.setParameter("usuarioId", usuarioId).setParameter("mesInicial", ano + "-01")
				.setParameter("mesFinal", ano + "-12").setParameter("inicio", periodo.atDay(1))
				.setParameter("fim", periodo.plusYears(1).atDay(1));
	}

	private void aplicarParametrosPeriodo(Query query, Long usuarioId, String mesInicial, String mesFinal) {
		YearMonth inicio = YearMonth.parse(mesInicial);
		YearMonth fim = YearMonth.parse(mesFinal).plusMonths(1);
		query.setParameter("usuarioId", usuarioId).setParameter("mesInicial", mesInicial)
				.setParameter("mesFinal", mesFinal).setParameter("inicio", inicio.atDay(1))
				.setParameter("fim", fim.atDay(1));
	}

	private LinhaBalancete linha(Object[] linha) {
		Long faturaId = null;
		if (linha[8] != null) faturaId = numero(linha[8]).longValue();
		else if (linha[9] != null) faturaId = numero(linha[9]).longValue();
		OrigemLinha origem = linha[8] != null ? OrigemLinha.FATURA
				: linha[9] != null ? OrigemLinha.PAGAMENTO_FATURA : OrigemLinha.DIRETA;
		LocalDate data = linha[1] instanceof LocalDate localDate ? localDate : ((java.sql.Date) linha[1]).toLocalDate();
		return new LinhaBalancete(numero(linha[0]).longValue(), data, (String) linha[2],
				TipoTransacao.valueOf((String) linha[3]), decimal(linha[4]), numero(linha[5]).longValue(),
				linha[6] == null ? null : numero(linha[6]).longValue(), (String) linha[7], origem, faturaId);
	}

	private BigDecimal decimal(Object valor) {
		return valor == null ? BigDecimal.ZERO : valor instanceof BigDecimal decimal ? decimal : new BigDecimal(valor.toString());
	}

	private Number numero(Object valor) {
		return (Number) valor;
	}

	private static class AcumuladoCategoria {
		private final String nome;
		private BigDecimal entradas = BigDecimal.ZERO;
		private BigDecimal saidas = BigDecimal.ZERO;

		private AcumuladoCategoria(String nome) {
			this.nome = nome;
		}
	}
}
