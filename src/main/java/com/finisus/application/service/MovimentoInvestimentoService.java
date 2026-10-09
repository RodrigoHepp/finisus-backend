package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.InvestimentoUseCase;
import com.finisus.application.ports.in.MovimentoInvestimentoUseCase;
import com.finisus.application.ports.in.RegistrarTransacaoUseCase;
import com.finisus.application.ports.in.TransacaoUseCase;
import com.finisus.application.ports.out.MovimentoInvestimentoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Investimento;
import com.finisus.domain.model.MovimentoInvestimento;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.vo.ValorMonetario;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import com.finisus.application.ports.out.ObterDataAtualPort;

public class MovimentoInvestimentoService implements MovimentoInvestimentoUseCase {
	private final MovimentoInvestimentoRepositoryPort movimentos;
	private final InvestimentoUseCase investimentos;
	private final RegistrarTransacaoUseCase transacoes;
	private final TransacaoUseCase transacoesConsultas;
	private final ObterDataAtualPort dataAtual;

	public MovimentoInvestimentoService(MovimentoInvestimentoRepositoryPort movimentos,
			InvestimentoUseCase investimentos, RegistrarTransacaoUseCase transacoes,
			TransacaoUseCase transacoesConsultas, ObterDataAtualPort dataAtual) {
		this.movimentos = movimentos;
		this.investimentos = investimentos;
		this.transacoes = transacoes;
		this.transacoesConsultas = transacoesConsultas;
		this.dataAtual = dataAtual;
	}

	@Override
	@Transactional
	public MovimentoInvestimento movimentar(Long usuarioId, MovimentoCommand command) {
		Investimento investimento = investimentos.buscar(usuarioId, command.investimentoId());
		if (!investimento.isAtivo()) {
			throw new DomainException("error.investimento.inativo");
		}
		TipoTransacao tipo = switch (command.tipo()) {
			case APORTE, TAXA -> TipoTransacao.SAIDA;
			case RESGATE, RENDIMENTO_REALIZADO -> TipoTransacao.ENTRADA;
		};
		Transacao transacao = transacoes.registrar(usuarioId,
				new TransacaoUseCase.RegistrarCommand(tipo, command.valor(), command.data(),
						command.tipo() + " - " + investimento.getNome(), investimento.getContaOrigemId(), null, null,
						List.of()));
		return movimentos.salvar(MovimentoInvestimento.reconstituir(null, investimento.getId(), command.tipo(),
				ValorMonetario.of(command.valor()), command.data(), transacao.getId(), null, null));
	}

	@Override
	@Transactional(readOnly = true)
	public List<MovimentoInvestimento> listar(Long usuarioId, Long investimentoId) {
		investimentos.buscar(usuarioId, investimentoId);
		return movimentos.listarPorInvestimento(investimentoId);
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<MovimentoInvestimento> listar(Long usuarioId, Long investimentoId, Paginacao paginacao) {
		investimentos.buscar(usuarioId, investimentoId);
		return movimentos.listarPorInvestimento(investimentoId, paginacao);
	}

	@Override
	@Transactional
	public MovimentoInvestimento estornar(Long usuarioId, Long movimentoId) {
		MovimentoInvestimento original = movimentos.buscarPorId(movimentoId)
				.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
		investimentos.buscar(usuarioId, original.getInvestimentoId());
		if (original.getEstornadoEm() != null || movimentos.existeCompensacao(original.getId())) {
			throw new DomainException("error.movimento.investimento.ja.estornado");
		}
		transacoesConsultas.estornar(usuarioId, original.getTransacaoId());
		java.time.LocalDateTime momento = dataAtual.obterDataHora();
		return movimentos.salvar(original.marcarEstornado(momento));
	}
}
