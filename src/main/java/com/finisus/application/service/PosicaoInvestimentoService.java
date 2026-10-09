package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.InvestimentoUseCase;
import com.finisus.application.ports.in.PosicaoInvestimentoUseCase;
import com.finisus.application.ports.out.PosicaoInvestimentoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.PosicaoInvestimento;
import com.finisus.domain.vo.ValorMonetario;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public class PosicaoInvestimentoService implements PosicaoInvestimentoUseCase {
	private final PosicaoInvestimentoRepositoryPort posicoes;
	private final InvestimentoUseCase investimentos;

	public PosicaoInvestimentoService(PosicaoInvestimentoRepositoryPort posicoes, InvestimentoUseCase investimentos) {
		this.posicoes = posicoes;
		this.investimentos = investimentos;
	}

	@Override
	@Transactional
	public PosicaoInvestimento registrar(Long usuarioId, Long investimentoId, RegistrarCommand command) {
		var investimento = investimentos.buscar(usuarioId, investimentoId);
		if (!investimento.isAtivo()) throw new DomainException("error.investimento.inativo");
		return posicoes.salvar(PosicaoInvestimento.nova(investimentoId, ValorMonetario.of(command.valor()),
				command.dataReferencia()));
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<PosicaoInvestimento> listar(Long usuarioId, Long investimentoId, Paginacao paginacao) {
		investimentos.buscar(usuarioId, investimentoId);
		return posicoes.listarPorInvestimento(investimentoId, paginacao);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PosicaoInvestimento> buscarUltima(Long usuarioId, Long investimentoId, LocalDate referencia) {
		investimentos.buscar(usuarioId, investimentoId);
		return posicoes.buscarUltimaAte(investimentoId, referencia);
	}
}
