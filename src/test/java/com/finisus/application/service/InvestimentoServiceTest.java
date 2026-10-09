package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.finisus.application.ports.in.InvestimentoUseCase;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.InvestimentoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.TipoConta;
import com.finisus.domain.model.TipoInvestimento;
import com.finisus.domain.vo.ValorMonetario;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InvestimentoServiceTest {
	private final InvestimentoRepositoryPort investimentos = mock(InvestimentoRepositoryPort.class);
	private final ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
	private final InvestimentoService service = new InvestimentoService(investimentos, contas);

	@Test
	void registraContaDeAplicacaoComoCustodiaExplicita() {
		when(contas.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(conta(10L, TipoConta.CORRENTE)));
		when(contas.buscarPorIdEUsuario(20L, 1L)).thenReturn(Optional.of(conta(20L, TipoConta.APLICACAO)));
		when(investimentos.salvar(org.mockito.ArgumentMatchers.any())).thenAnswer(invocacao -> invocacao.getArgument(0));

		var investimento = service.criar(1L,
				new InvestimentoUseCase.CriarCommand("CDB", TipoInvestimento.RENDA_FIXA, 10L, 20L));

		assertThat(investimento.getContaOrigemId()).isEqualTo(10L);
		assertThat(investimento.getContaCustodiaId()).isEqualTo(20L);
	}

	@Test
	void rejeitaCustodiaQueNaoSejaContaDeAplicacao() {
		when(contas.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(conta(10L, TipoConta.CORRENTE)));
		when(contas.buscarPorIdEUsuario(20L, 1L)).thenReturn(Optional.of(conta(20L, TipoConta.POUPANCA)));

		assertThatThrownBy(() -> service.criar(1L,
				new InvestimentoUseCase.CriarCommand("CDB", TipoInvestimento.RENDA_FIXA, 10L, 20L)))
				.isInstanceOf(DomainException.class).hasMessage("error.investimento.custodia.invalida");
	}

	@Test
	void rejeitaContaDeCustodiaJaVinculadaAOutroInvestimento() {
		when(contas.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(conta(10L, TipoConta.CORRENTE)));
		when(contas.buscarPorIdEUsuario(20L, 1L)).thenReturn(Optional.of(conta(20L, TipoConta.APLICACAO)));
		when(investimentos.existePorContaCustodiaExceto(20L, null)).thenReturn(true);

		assertThatThrownBy(() -> service.criar(1L,
				new InvestimentoUseCase.CriarCommand("Segundo CDB", TipoInvestimento.RENDA_FIXA, 10L, 20L)))
				.isInstanceOf(DomainException.class).hasMessage("error.investimento.custodia.invalida");
	}

	private Conta conta(Long id, TipoConta tipo) {
		return Conta.reconstituir(id, 1L, "Conta " + id, tipo, 99L, ValorMonetario.zero(), true, 0);
	}
}
