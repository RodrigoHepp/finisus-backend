package com.financeiro.application.service;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.GerenciarParcelasFinanciamentoUseCase;
import com.financeiro.application.ports.in.ParcelaFinanciamentoUseCase;
import com.financeiro.application.ports.out.ContaRepositoryPort;
import com.financeiro.application.ports.out.FinanciamentoRepositoryPort;
import com.financeiro.application.ports.out.ObterDataAtualPort;
import com.financeiro.application.ports.out.ParcelaFinanciamentoRepositoryPort;
import com.financeiro.application.ports.out.TransacaoRepositoryPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.Conta;
import com.financeiro.domain.model.Financiamento;
import com.financeiro.domain.model.ParcelaFinanciamento;
import com.financeiro.domain.model.StatusParcelaFinanciamento;
import com.financeiro.domain.model.TipoTransacao;
import com.financeiro.domain.model.Transacao;
import com.financeiro.domain.model.TransacaoHistorico;
import com.financeiro.domain.vo.ValorMonetario;

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

	public ParcelaFinanciamentoService(ParcelaFinanciamentoRepositoryPort parcelas,
			FinanciamentoRepositoryPort financiamentos, ContaRepositoryPort contas, TransacaoRepositoryPort transacoes,
			ObterDataAtualPort dataAtual) {
		this.parcelas = parcelas;
		this.financiamentos = financiamentos;
		this.contas = contas;
		this.transacoes = transacoes;
		this.dataAtual = dataAtual;
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
		parcela.pagar();
		return parcelas.salvar(parcela);
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
		BigDecimal valorParcela = calcularValorParcela(financiamento.getPrincipal().valor(),
				financiamento.getTaxaJurosMensal(), financiamento.getNumeroParcelas());
		List<ParcelaFinanciamento> novas = java.util.stream.IntStream.rangeClosed(1, financiamento.getNumeroParcelas())
				.mapToObj(numero -> ParcelaFinanciamento.nova(financiamento.getId(), numero,
						ValorMonetario.of(valorParcela), financiamento.getDataInicio().plusMonths(numero - 1)))
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
		BigDecimal valor = calcularValorParcela(financiamento.getPrincipal().valor(),
				financiamento.getTaxaJurosMensal(), restantes.size());
		return java.util.stream.IntStream
				.range(0, restantes.size()).mapToObj(indice -> restantes.get(indice).replanejada(indice + 1,
						ValorMonetario.of(valor), financiamento.getDataInicio().plusMonths(indice)))
				.map(parcelas::salvar).toList();
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
}
