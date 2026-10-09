package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.FinanciamentoUseCase;
import com.finisus.application.ports.in.GerenciarParcelasFinanciamentoUseCase;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.FinanciamentoRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Financiamento;
import com.finisus.domain.vo.ValorMonetario;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import com.finisus.application.ports.out.ParcelaFinanciamentoRepositoryPort;

public class FinanciamentoService implements FinanciamentoUseCase {
	private final FinanciamentoRepositoryPort financiamentos;
	private final ContaRepositoryPort contas;
	private final GerenciarParcelasFinanciamentoUseCase parcelas;
	private final ObterDataAtualPort dataAtual;
	private final ParcelaFinanciamentoRepositoryPort parcelasPersistidas;

	public FinanciamentoService(FinanciamentoRepositoryPort financiamentos, ContaRepositoryPort contas,
			GerenciarParcelasFinanciamentoUseCase parcelas, ParcelaFinanciamentoRepositoryPort parcelasPersistidas,
			ObterDataAtualPort dataAtual) {
		this.financiamentos = financiamentos;
		this.contas = contas;
		this.parcelas = parcelas;
		this.parcelasPersistidas = parcelasPersistidas;
		this.dataAtual = dataAtual;
	}

	@Override
	@Transactional
	public Financiamento criar(Long usuarioId, CriarCommand command) {
		if (command.numeroParcelas() < 1 || command.taxaJurosMensal().signum() < 0) {
			throw new DomainException("error.financiamento.invalido");
		}
		ContaAtivaValidator.exigirAtiva(contas.buscarPorIdEUsuario(command.contaId(), usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado")));
		Financiamento financiamento = financiamentos
				.salvar(Financiamento.novo(usuarioId, command.descricao(), ValorMonetario.of(command.principal()),
						command.taxaJurosMensal(), command.numeroParcelas(), command.dataInicio(), command.contaId()));
		parcelas.gerar(financiamento);
		return financiamento;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Financiamento> listar(Long usuarioId) {
		return financiamentos.listarPorUsuario(usuarioId);
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<Financiamento> listar(Long usuarioId, Paginacao paginacao) {
		return financiamentos.listarPorUsuario(usuarioId, paginacao);
	}

	@Override
	@Transactional(readOnly = true)
	public Financiamento buscar(Long usuarioId, Long financiamentoId) {
		return financiamentos.buscarPorIdEUsuario(financiamentoId, usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}

	@Override
	@Transactional
	public RefinanciamentoResult excluirPorRefinanciamento(Long usuarioId, Long financiamentoId, Long parcelaId) {
		Financiamento financiamento = buscar(usuarioId, financiamentoId);
		if (financiamento.isFinalizado()) {
			throw new DomainException("error.financiamento.finalizado");
		}
		parcelasPersistidas.registrarSnapshotCronograma(financiamentoId, financiamento.getCronogramaVersao());
		int excluidas = parcelas.excluirPendentesAPartirDe(financiamento, parcelaId);
		Financiamento finalizado = financiamentos.salvar(financiamento
				.comNovoCronograma(financiamento.getNumeroParcelas() - excluidas).finalizar(dataAtual.obterDataHora()));
		return new RefinanciamentoResult(finalizado, excluidas);
	}

	@Override
	@Transactional
	public RefinanciamentoCompletoResult refinanciar(Long usuarioId, Long financiamentoId, RefinanciarCommand command) {
		Financiamento origem = buscar(usuarioId, financiamentoId);
		if (origem.isFinalizado())
			throw new DomainException("error.financiamento.finalizado");
		CriarCommand novo = command.novoFinanciamento();
		if (command.parcelaId() == null || novo == null || novo.numeroParcelas() < 1
				|| novo.taxaJurosMensal().signum() < 0)
			throw new DomainException("error.financiamento.invalido");
		ContaAtivaValidator.exigirAtiva(contas.buscarPorIdEUsuario(novo.contaId(), usuarioId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado")));
		parcelasPersistidas.registrarSnapshotCronograma(financiamentoId, origem.getCronogramaVersao());
		int excluidas = parcelas.excluirPendentesAPartirDe(origem, command.parcelaId());
		Financiamento origemFinalizada = financiamentos.salvar(origem
				.comNovoCronograma(origem.getNumeroParcelas() - excluidas).finalizar(dataAtual.obterDataHora()));
		Financiamento refinanciado = financiamentos.salvar(Financiamento.novoRefinanciado(usuarioId, novo.descricao(),
				ValorMonetario.of(novo.principal()), novo.taxaJurosMensal(), novo.numeroParcelas(), novo.dataInicio(),
				novo.contaId(), origem.getId()));
		parcelas.gerar(refinanciado);
		return new RefinanciamentoCompletoResult(origemFinalizada, refinanciado, excluidas);
	}

	@Override
	@Transactional
	public int excluirPorErroDeLancamento(Long usuarioId, Long financiamentoId, Long parcelaId) {
		Financiamento financiamento = buscar(usuarioId, financiamentoId);
		if (financiamento.isFinalizado()) {
			throw new DomainException("error.financiamento.finalizado");
		}
		parcelasPersistidas.registrarSnapshotCronograma(financiamentoId, financiamento.getCronogramaVersao());
		int numeroParcelas = parcelas.excluirERecalcular(financiamento, parcelaId).size();
		financiamentos.salvar(financiamento.comNovoCronograma(numeroParcelas));
		return numeroParcelas;
	}

	@Override
	@Transactional
	public Financiamento cancelar(Long usuarioId, Long financiamentoId) {
		Financiamento financiamento = buscar(usuarioId, financiamentoId);
		if (parcelasPersistidas.existePagaPorFinanciamento(financiamentoId)) {
			throw new DomainException("error.financiamento.cancelamento.invalido");
		}
		return financiamentos.salvar(financiamento.cancelar(dataAtual.obterDataHora()));
	}

	@Override
	@Transactional
	public AmortizacaoResult amortizar(Long usuarioId, Long financiamentoId, AmortizarCommand command) {
		Financiamento financiamento = buscar(usuarioId, financiamentoId);
		if (financiamento.isFinalizado() || financiamento.getStatus() != com.finisus.domain.model.StatusFinanciamento.ATIVO
				|| command == null)
			throw new DomainException("error.financiamento.amortizacao.invalida");
		parcelasPersistidas.registrarSnapshotCronograma(financiamentoId, financiamento.getCronogramaVersao());
		var amortizacao = parcelas.amortizar(usuarioId, financiamento, command.valor(), command.dataPagamento(),
				command.numeroParcelasRestantes(), command.modalidade());
		Financiamento atualizado = financiamento.comNovoCronograma(
				amortizacao.parcelasPagasPreservadas() + amortizacao.parcelasRestantes());
		if (amortizacao.saldoDevedorAtual().signum() == 0)
			atualizado = atualizado.finalizar(dataAtual.obterDataHora());
		atualizado = financiamentos.salvar(atualizado);
		return new AmortizacaoResult(atualizado, amortizacao.transacaoId(), amortizacao.saldoDevedorAnterior(),
				amortizacao.saldoDevedorAtual(), amortizacao.parcelasRestantes());
	}

	@Override
	@Transactional(readOnly = true)
	public List<ParcelaFinanciamentoRepositoryPort.ParcelaHistorica> consultarCronogramaHistorico(Long usuarioId,
			Long financiamentoId, int versao) {
		Financiamento financiamento = buscar(usuarioId, financiamentoId);
		if (versao < 1 || versao >= financiamento.getCronogramaVersao())
			throw new DomainException("error.recurso.nao.encontrado");
		List<ParcelaFinanciamentoRepositoryPort.ParcelaHistorica> historico = parcelasPersistidas
				.listarCronogramaHistorico(financiamentoId, versao);
		if (historico.isEmpty())
			throw new DomainException("error.recurso.nao.encontrado");
		return historico;
	}
}
