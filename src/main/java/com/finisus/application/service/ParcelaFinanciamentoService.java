package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.GerenciarParcelasFinanciamentoUseCase;
import com.finisus.application.ports.in.ParcelaFinanciamentoUseCase;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.FinanciamentoRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.in.EstornarTransacaoVinculadaUseCase;
import com.finisus.application.ports.out.ParcelaFinanciamentoRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.Financiamento;
import com.finisus.domain.model.ModalidadeAmortizacaoFinanciamento;
import com.finisus.domain.model.ParcelaFinanciamento;
import com.finisus.domain.model.StatusParcelaFinanciamento;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TransacaoHistorico;
import com.finisus.domain.vo.ValorMonetario;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class ParcelaFinanciamentoService implements ParcelaFinanciamentoUseCase, GerenciarParcelasFinanciamentoUseCase {
	private final ParcelaFinanciamentoRepositoryPort parcelas;
	private final FinanciamentoRepositoryPort financiamentos;
	private final ContaRepositoryPort contas;
	private final TransacaoRepositoryPort transacoes;
	private final ObterDataAtualPort dataAtual;
	private final EstornarTransacaoVinculadaUseCase estornos;

	public ParcelaFinanciamentoService(ParcelaFinanciamentoRepositoryPort parcelas,
			FinanciamentoRepositoryPort financiamentos, ContaRepositoryPort contas, TransacaoRepositoryPort transacoes,
			ObterDataAtualPort dataAtual, EstornarTransacaoVinculadaUseCase estornos) {
		this.parcelas = parcelas;
		this.financiamentos = financiamentos;
		this.contas = contas;
		this.transacoes = transacoes;
		this.dataAtual = dataAtual;
		this.estornos = estornos;
	}

	@Override
	public List<ParcelaFinanciamento> listar(Long usuarioId, Long financiamentoId) {
		buscarFinanciamento(usuarioId, financiamentoId);
		return parcelas.listarPorFinanciamento(financiamentoId);
	}

	@Override
	public Pagina<ParcelaFinanciamento> listar(Long usuarioId, Long financiamentoId, Paginacao paginacao) {
		buscarFinanciamento(usuarioId, financiamentoId);
		return parcelas.listarPorFinanciamento(financiamentoId, paginacao);
	}

	@Override
	@Transactional
	public ParcelaFinanciamento pagarParcela(Long usuarioId, Long financiamentoId, Long parcelaId,
			LocalDate dataPagamento) {
		Financiamento financiamento = buscarFinanciamento(usuarioId, financiamentoId);
		if (financiamento.isFinalizado()) {
			throw new DomainException("error.financiamento.finalizado");
		}
		ParcelaFinanciamento parcela = localizarParcela(financiamentoId, parcelaId);
		Conta conta = ContaAtivaValidator.exigirAtiva(contas.buscarPorIdEUsuario(financiamento.getContaId(), usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado")));
		Transacao pagamento = transacoes.salvar(Transacao.nova(usuarioId, TipoTransacao.SAIDA, parcela.getValor(),
				dataPagamento, "Parcela " + parcela.getNumero() + " - " + financiamento.getDescricao(), conta.getId(),
				null, null, List.of()));
		contas.salvar(Conta.reconstituir(conta.getId(), conta.getUsuarioId(), conta.getNome(), conta.getTipo(),
				conta.getBancoId(), conta.getSaldo().subtrair(parcela.getValor()), conta.isAtivo(),
				conta.getVersion()));
		transacoes.salvarHistorico(TransacaoHistorico.registrar(pagamento.getId(), "PAGAMENTO_PARCELA", null,
				parcela.getValor().valor().toPlainString(), usuarioId, dataAtual.obterDataHora()));
		return parcelas.salvar(parcela.pagaCom(pagamento.getId()));
	}

	@Override
	@Transactional
	public ParcelaFinanciamento estornarPagamento(Long usuarioId, Long financiamentoId, Long parcelaId) {
		buscarFinanciamento(usuarioId, financiamentoId);
		ParcelaFinanciamento parcela = localizarParcela(financiamentoId, parcelaId);
		Long transacaoId = parcela.getTransacaoId();
		ParcelaFinanciamento reaberta = parcela.estornarPagamento(dataAtual.obter());
		estornos.estornarVinculada(usuarioId, transacaoId);
		return parcelas.salvar(reaberta);
	}

	@Override
	@Transactional
	public int processarAtrasos(ProcessarAtrasosCommand command) {
		List<ParcelaFinanciamento> vencidas = parcelas.listarVencidas(command.dataReferencia());
		vencidas.forEach(parcela -> {
			parcela.marcarAtrasada(command.dataReferencia());
			parcelas.salvar(parcela);
		});
		return vencidas.size();
	}

	@Override
	@Transactional
	public void gerar(Financiamento financiamento) {
		List<ComposicaoParcela> composicoes = calcularComposicoes(financiamento.getPrincipal().valor(),
				financiamento.getTaxaJurosMensal(), financiamento.getNumeroParcelas());
		List<ParcelaFinanciamento> novas = java.util.stream.IntStream.rangeClosed(1, financiamento.getNumeroParcelas())
				.mapToObj(numero -> novaParcela(financiamento, numero, composicoes.get(numero - 1)))
				.toList();
		parcelas.salvarNovas(financiamento.getId(), novas);
	}

	@Override
	@Transactional
	public int excluirPendentesAPartirDe(Financiamento financiamento, Long parcelaId) {
		ParcelaFinanciamento referencia = localizarParcela(financiamento.getId(), parcelaId);
		return parcelas.excluirPendentesAPartirDe(financiamento.getId(), referencia.getNumero());
	}

	@Override
	@Transactional
	public List<ParcelaFinanciamento> excluirERecalcular(Financiamento financiamento, Long parcelaId) {
		List<ParcelaFinanciamento> atuais = parcelas.listarPorFinanciamento(financiamento.getId());
		if (atuais.stream().anyMatch(parcela -> parcela.getStatus() == StatusParcelaFinanciamento.PAGA)) {
			throw new DomainException("error.financiamento.recalculo.parcela.paga");
		}
		if (atuais.stream().noneMatch(parcela -> parcela.getId().equals(parcelaId))) {
			throw new DomainException("error.recurso.nao.encontrado");
		}
		if (atuais.size() == 1) {
			throw new DomainException("error.financiamento.recalculo.sem.parcelas");
		}
		parcelas.excluir(parcelaId);
		List<ParcelaFinanciamento> restantes = parcelas.listarPorFinanciamento(financiamento.getId());
		List<ComposicaoParcela> composicoes = calcularComposicoes(financiamento.getPrincipal().valor(),
				financiamento.getTaxaJurosMensal(), restantes.size());
		return java.util.stream.IntStream
				.range(0, restantes.size()).mapToObj(indice -> replanejar(restantes.get(indice), indice + 1,
						financiamento.getDataInicio().plusMonths(indice), composicoes.get(indice)))
				.map(parcelas::salvar).toList();
	}

	@Override
	@Transactional
	public AmortizacaoCronograma amortizar(Long usuarioId, Financiamento financiamento, BigDecimal valor,
			LocalDate dataPagamento, Integer numeroParcelasRestantes,
			ModalidadeAmortizacaoFinanciamento modalidade) {
		if (valor == null || valor.signum() <= 0 || dataPagamento == null)
			throw new DomainException("error.financiamento.amortizacao.invalida");
		List<ParcelaFinanciamento> atuais = parcelas.listarPorFinanciamento(financiamento.getId());
		List<ParcelaFinanciamento> pendentes = atuais.stream()
				.filter(parcela -> parcela.getStatus() != StatusParcelaFinanciamento.PAGA).toList();
		if (pendentes.isEmpty() || pendentes.getFirst().getSaldoDevedorInicial() == null)
			throw new DomainException("error.financiamento.amortizacao.invalida");
		BigDecimal saldoAnterior = pendentes.getFirst().getSaldoDevedorInicial().valor();
		BigDecimal saldoAtual = saldoAnterior.subtract(valor);
		int quantidadeRestante = resolverQuantidadeParcelas(saldoAtual, financiamento.getTaxaJurosMensal(), pendentes,
				numeroParcelasRestantes, modalidade);
		if (saldoAtual.signum() < 0)
			throw new DomainException("error.financiamento.amortizacao.invalida");
		Conta conta = ContaAtivaValidator.exigirAtiva(contas.buscarPorIdEUsuario(financiamento.getContaId(), usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado")));
		Transacao amortizacao = transacoes.salvar(Transacao.nova(usuarioId, TipoTransacao.SAIDA,
				ValorMonetario.of(valor), dataPagamento, "Amortização - " + financiamento.getDescricao(), conta.getId(),
				null, null, List.of()));
		contas.salvar(Conta.reconstituir(conta.getId(), conta.getUsuarioId(), conta.getNome(), conta.getTipo(),
				conta.getBancoId(), conta.getSaldo().subtrair(ValorMonetario.of(valor)), conta.isAtivo(),
				conta.getVersion()));
		transacoes.salvarHistorico(TransacaoHistorico.registrar(amortizacao.getId(), "AMORTIZACAO_FINANCIAMENTO",
				saldoAnterior.toPlainString(), saldoAtual.toPlainString(), usuarioId, dataAtual.obterDataHora()));
		parcelas.excluirPendentesAPartirDe(financiamento.getId(), pendentes.getFirst().getNumero());
		if (saldoAtual.signum() > 0) {
			List<ComposicaoParcela> composicoes = calcularComposicoes(saldoAtual,
					financiamento.getTaxaJurosMensal(), quantidadeRestante);
			int primeiroNumero = pendentes.getFirst().getNumero();
			LocalDate primeiroVencimento = pendentes.getFirst().getDataVencimento();
			List<ParcelaFinanciamento> novas = java.util.stream.IntStream.range(0, quantidadeRestante)
					.mapToObj(indice -> ParcelaFinanciamento.nova(financiamento.getId(), primeiroNumero + indice,
							ValorMonetario.of(composicoes.get(indice).valor()),
							ValorMonetario.of(composicoes.get(indice).principal()),
							ValorMonetario.of(composicoes.get(indice).juros()),
							ValorMonetario.of(composicoes.get(indice).encargos()),
							ValorMonetario.of(composicoes.get(indice).saldoInicial()),
							ValorMonetario.of(composicoes.get(indice).saldoFinal()),
							primeiroVencimento.plusMonths(indice))).toList();
			parcelas.salvarNovas(financiamento.getId(), novas);
		}
		return new AmortizacaoCronograma(amortizacao.getId(), saldoAnterior, saldoAtual,
				atuais.size() - pendentes.size(), quantidadeRestante);
	}

	private int resolverQuantidadeParcelas(BigDecimal saldoAtual, BigDecimal taxaJurosMensal,
			List<ParcelaFinanciamento> pendentes, Integer quantidadeLegada,
			ModalidadeAmortizacaoFinanciamento modalidade) {
		if (saldoAtual.signum() == 0) {
			if (quantidadeLegada != null && quantidadeLegada != 0)
				throw new DomainException("error.financiamento.amortizacao.invalida");
			return 0;
		}
		if (saldoAtual.signum() < 0)
			throw new DomainException("error.financiamento.amortizacao.invalida");
		if (modalidade == null) {
			if (quantidadeLegada == null || quantidadeLegada < 1 || quantidadeLegada > pendentes.size())
				throw new DomainException("error.financiamento.amortizacao.invalida");
			return quantidadeLegada;
		}
		if (quantidadeLegada != null)
			throw new DomainException("error.financiamento.amortizacao.invalida");
		if (modalidade == ModalidadeAmortizacaoFinanciamento.REDUZIR_PRESTACAO)
			return pendentes.size();
		BigDecimal prestacaoReferencia = pendentes.getFirst().getValor().valor();
		return java.util.stream.IntStream.rangeClosed(1, pendentes.size())
				.filter(quantidade -> calcularComposicoes(saldoAtual, taxaJurosMensal, quantidade).getFirst().valor()
						.compareTo(prestacaoReferencia) <= 0)
				.findFirst().orElse(pendentes.size());
	}

	private Financiamento buscarFinanciamento(Long usuarioId, Long financiamentoId) {
		return financiamentos.buscarPorIdEUsuario(financiamentoId, usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}

	private ParcelaFinanciamento localizarParcela(Long financiamentoId, Long parcelaId) {
		return parcelas.listarPorFinanciamento(financiamentoId).stream()
				.filter(parcela -> parcela.getId().equals(parcelaId)).findFirst()
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}

	private BigDecimal calcularValorParcela(BigDecimal principal, BigDecimal taxaJurosMensal, int numeroParcelas) {
		BigDecimal taxa = taxaJurosMensal.movePointLeft(2);
		if (taxa.signum() == 0) {
			return principal.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.HALF_UP);
		}
		BigDecimal fator = BigDecimal.ONE.add(taxa).pow(numeroParcelas);
		return principal.multiply(taxa).multiply(fator).divide(fator.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
	}

	private List<BigDecimal> calcularValoresParcelas(BigDecimal principal, BigDecimal taxaJurosMensal,
			int numeroParcelas) {
		if (taxaJurosMensal.signum() != 0) {
			BigDecimal valor = calcularValorParcela(principal, taxaJurosMensal, numeroParcelas);
			return java.util.Collections.nCopies(numeroParcelas, valor);
		}
		BigDecimal valorBase = principal.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.DOWN);
		BigDecimal ultimaParcela = principal.subtract(valorBase.multiply(BigDecimal.valueOf(numeroParcelas - 1)))
				.setScale(2, RoundingMode.UNNECESSARY);
		return java.util.stream.IntStream.rangeClosed(1, numeroParcelas)
				.mapToObj(numero -> numero == numeroParcelas ? ultimaParcela : valorBase).toList();
	}

	private List<ComposicaoParcela> calcularComposicoes(BigDecimal principal, BigDecimal taxaJurosMensal,
			int numeroParcelas) {
		List<BigDecimal> valores = calcularValoresParcelas(principal, taxaJurosMensal, numeroParcelas);
		BigDecimal taxa = taxaJurosMensal.movePointLeft(2);
		BigDecimal saldo = principal.setScale(2, RoundingMode.HALF_UP);
		java.util.ArrayList<ComposicaoParcela> composicoes = new java.util.ArrayList<>(numeroParcelas);
		for (int indice = 0; indice < numeroParcelas; indice++) {
			BigDecimal saldoInicial = saldo;
			BigDecimal juros = saldoInicial.multiply(taxa).setScale(2, RoundingMode.HALF_UP);
			BigDecimal amortizacao = indice == numeroParcelas - 1 ? saldoInicial
					: valores.get(indice).subtract(juros).setScale(2, RoundingMode.HALF_UP);
			BigDecimal saldoFinal = saldoInicial.subtract(amortizacao).setScale(2, RoundingMode.UNNECESSARY);
			BigDecimal encargos = BigDecimal.ZERO.setScale(2);
			BigDecimal valor = amortizacao.add(juros).add(encargos).setScale(2, RoundingMode.UNNECESSARY);
			composicoes.add(new ComposicaoParcela(valor, amortizacao, juros, encargos, saldoInicial, saldoFinal));
			saldo = saldoFinal;
		}
		return List.copyOf(composicoes);
	}

	private ParcelaFinanciamento novaParcela(Financiamento financiamento, int numero,
			ComposicaoParcela composicao) {
		return ParcelaFinanciamento.nova(financiamento.getId(), numero, ValorMonetario.of(composicao.valor()),
				ValorMonetario.of(composicao.principal()), ValorMonetario.of(composicao.juros()),
				ValorMonetario.of(composicao.encargos()), ValorMonetario.of(composicao.saldoInicial()),
				ValorMonetario.of(composicao.saldoFinal()), financiamento.getDataInicio().plusMonths(numero - 1));
	}

	private ParcelaFinanciamento replanejar(ParcelaFinanciamento parcela, int numero, LocalDate vencimento,
			ComposicaoParcela composicao) {
		return parcela.replanejada(numero, ValorMonetario.of(composicao.valor()),
				ValorMonetario.of(composicao.principal()), ValorMonetario.of(composicao.juros()),
				ValorMonetario.of(composicao.encargos()), ValorMonetario.of(composicao.saldoInicial()),
				ValorMonetario.of(composicao.saldoFinal()), vencimento);
	}

	private record ComposicaoParcela(BigDecimal valor, BigDecimal principal, BigDecimal juros, BigDecimal encargos,
			BigDecimal saldoInicial, BigDecimal saldoFinal) { }
}
