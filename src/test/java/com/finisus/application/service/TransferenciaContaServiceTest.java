package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.clearInvocations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.finisus.application.ports.in.TransferenciaContaUseCase.CriarCommand;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.TransferenciaContaRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.StatusTransferencia;
import com.finisus.domain.model.TipoConta;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TransferenciaConta;
import com.finisus.domain.vo.ValorMonetario;

class TransferenciaContaServiceTest {
	private static final LocalDate DATA = LocalDate.of(2026, 9, 25);
	private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 25, 10, 0);

	@Test
	void transfereAtomicamenteECriaDoisLancamentosVinculados() {
		Fixture f = new Fixture();
		when(f.transferencias.buscarPorUsuarioEChave(1L, "op-1")).thenReturn(Optional.empty());
		when(f.contas.buscarPorIdsEUsuarioParaAtualizacao(List.of(10L, 20L), 1L))
				.thenReturn(List.of(f.origem(), f.destino()));

		TransferenciaConta resultado = f.service.transferir(1L, "op-1", comando("250.00"));

		assertThat(resultado.id()).isEqualTo(30L);
		ArgumentCaptor<Transacao> lancamentos = ArgumentCaptor.forClass(Transacao.class);
		verify(f.transacoes, times(2)).salvar(lancamentos.capture());
		assertThat(lancamentos.getAllValues()).extracting(Transacao::getTipo)
				.containsExactly(TipoTransacao.SAIDA, TipoTransacao.ENTRADA);
		assertThat(lancamentos.getAllValues()).allSatisfy(t -> assertThat(t.getTransferenciaId()).isEqualTo(30L));
		ArgumentCaptor<Conta> contasSalvas = ArgumentCaptor.forClass(Conta.class);
		verify(f.contas, times(2)).salvar(contasSalvas.capture());
		assertThat(contasSalvas.getAllValues()).extracting(c -> c.getSaldo().valor())
				.containsExactly(new BigDecimal("750.00"), new BigDecimal("350.00"));
	}

	@Test
	void repeteMesmaRequisicaoSemDuplicarEfeito() {
		Fixture f = new Fixture();
		TransferenciaConta existente = f.transferenciaAtiva(f.hash(comando("250.00")));
		when(f.transferencias.buscarPorUsuarioEChave(1L, "op-1")).thenReturn(Optional.of(existente));

		assertThat(f.service.transferir(1L, "op-1", comando("250.00"))).isSameAs(existente);

		verify(f.contas, never()).salvar(any());
		verify(f.transacoes, never()).salvar(any());
	}

	@Test
	void rejeitaReusoDaChaveComOutroPayload() {
		Fixture f = new Fixture();
		when(f.transferencias.buscarPorUsuarioEChave(1L, "op-1"))
				.thenReturn(Optional.of(f.transferenciaAtiva("outro-hash")));

		assertThatThrownBy(() -> f.service.transferir(1L, "op-1", comando("250.00")))
				.isInstanceOf(DomainException.class)
				.extracting("messageKey").isEqualTo("error.transferencia.idempotencia.conflitante");
	}

	@Test
	void estornaOsDoisLancamentosEReverteOsSaldos() {
		Fixture f = new Fixture();
		TransferenciaConta ativa = f.transferenciaAtiva("hash");
		Conta origem = Conta.reconstituir(10L, 1L, "Origem", TipoConta.FISICO, null,
				ValorMonetario.of(new BigDecimal("750.00")), true, 1);
		Conta destino = Conta.reconstituir(20L, 1L, "Destino", TipoConta.FISICO, null,
				ValorMonetario.of(new BigDecimal("350.00")), true, 1);
		when(f.transferencias.buscarPorIdEUsuarioParaAtualizacao(30L, 1L)).thenReturn(Optional.of(ativa));
		when(f.contas.buscarPorIdsEUsuarioParaAtualizacao(List.of(10L, 20L), 1L))
				.thenReturn(List.of(origem, destino));
		when(f.transacoes.listarPorTransferencia(30L)).thenReturn(List.of(
				Transacao.reconstituirComTransferencia(40L, 1L, TipoTransacao.SAIDA,
						ValorMonetario.of(new BigDecimal("250.00")), DATA, "Reserva", 10L, null, null,
						null, null, null, null, 30L, null, 0, List.of()),
				Transacao.reconstituirComTransferencia(41L, 1L, TipoTransacao.ENTRADA,
						ValorMonetario.of(new BigDecimal("250.00")), DATA, "Reserva", 20L, null, null,
						null, null, null, null, 30L, null, 0, List.of())));

		TransferenciaConta resultado = f.service.estornar(1L, 30L);

		assertThat(resultado.status()).isEqualTo(StatusTransferencia.ESTORNADA);
		ArgumentCaptor<Conta> contasSalvas = ArgumentCaptor.forClass(Conta.class);
		verify(f.contas, times(2)).salvar(contasSalvas.capture());
		assertThat(contasSalvas.getAllValues()).extracting(c -> c.getSaldo().valor())
				.containsExactly(new BigDecimal("1000.00"), new BigDecimal("100.00"));
		ArgumentCaptor<Transacao> lancamentos = ArgumentCaptor.forClass(Transacao.class);
		verify(f.transacoes, times(2)).salvar(lancamentos.capture());
		assertThat(lancamentos.getAllValues()).allMatch(Transacao::isEstornada);
	}

	private static CriarCommand comando(String valor) {
		return new CriarCommand(10L, 20L, new BigDecimal(valor), DATA, "Reserva");
	}

	private static class Fixture {
		final TransferenciaContaRepositoryPort transferencias = mock(TransferenciaContaRepositoryPort.class);
		final ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		final TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		final ObterDataAtualPort dataAtual = mock(ObterDataAtualPort.class);
		final TransferenciaContaService service = new TransferenciaContaService(transferencias, contas, transacoes,
				dataAtual);

		Fixture() {
			when(dataAtual.obterDataHora()).thenReturn(AGORA);
			when(transferencias.salvar(any())).thenAnswer(invocation -> {
				TransferenciaConta t = invocation.getArgument(0);
				return new TransferenciaConta(t.id() == null ? 30L : t.id(), t.usuarioId(), t.contaOrigemId(),
						t.contaDestinoId(), t.valor(), t.data(), t.descricao(), t.chaveIdempotencia(),
						t.hashRequisicao(), t.status(), t.estornadaEm(), t.version());
			});
			AtomicLong id = new AtomicLong(40);
			when(transacoes.salvar(any())).thenAnswer(invocation -> {
				Transacao t = invocation.getArgument(0);
				if (t.getId() != null) return t;
				return Transacao.reconstituirComTransferencia(id.getAndIncrement(), t.getUsuarioId(), t.getTipo(),
						t.getValor(), t.getData(), t.getDescricao(), t.getContaId(), null, null, null, null, null,
						null, t.getTransferenciaId(), t.getEstornadoEm(), t.getVersion(), List.of());
			});
		}

		Conta origem() {
			return Conta.reconstituir(10L, 1L, "Origem", TipoConta.FISICO, null,
					ValorMonetario.of(new BigDecimal("1000.00")), true, 0);
		}

		Conta destino() {
			return Conta.reconstituir(20L, 1L, "Destino", TipoConta.FISICO, null,
					ValorMonetario.of(new BigDecimal("100.00")), true, 0);
		}

		TransferenciaConta transferenciaAtiva(String hash) {
			return new TransferenciaConta(30L, 1L, 10L, 20L, ValorMonetario.of(new BigDecimal("250.00")), DATA,
					"Reserva", "op-1", hash, StatusTransferencia.ATIVA, null, 0);
		}

		String hash(CriarCommand command) {
			when(transferencias.buscarPorUsuarioEChave(1L, "captura-hash")).thenReturn(Optional.empty());
			when(contas.buscarPorIdsEUsuarioParaAtualizacao(List.of(10L, 20L), 1L))
					.thenReturn(List.of(origem(), destino()));
			TransferenciaConta criada = service.transferir(1L, "captura-hash", command);
			clearInvocations(transferencias, contas, transacoes);
			return criada.hashRequisicao();
		}
	}
}
