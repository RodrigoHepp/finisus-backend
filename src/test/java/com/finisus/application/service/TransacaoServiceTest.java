package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.finisus.application.ports.in.TransacaoUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.ItemRepositoryPort;
import com.finisus.application.ports.out.MeioPagamentoRepositoryPort;
import com.finisus.application.ports.out.ObrigacaoFinanceiraRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.ParcelaFinanciamentoRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.VinculoTransacaoDivisaoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.Item;
import com.finisus.domain.model.TipoConta;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TransacaoHistorico;
import com.finisus.domain.model.TransacaoItem;
import com.finisus.domain.vo.ValorMonetario;

class TransacaoServiceTest {

	@Test
	void registraMotivoCorrelacaoESnapshotsCompletosNaCorrecao() {
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		ObterDataAtualPort dataAtual = mock(ObterDataAtualPort.class);
		Transacao anterior = Transacao.reconstituir(10L, 1L, TipoTransacao.SAIDA,
				ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 9, 20), "Mercado", 2L, null,
				null, null, null, null, null, null, 0, List.of());
		Conta conta = Conta.reconstituir(2L, 1L, "Conta", TipoConta.CORRENTE, 3L,
				ValorMonetario.of(new BigDecimal("900.00")), 0);
		when(transacoes.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(anterior));
		when(transacoes.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
		when(contas.buscarPorIdEUsuario(2L, 1L)).thenReturn(Optional.of(conta));
		when(dataAtual.obterDataHora()).thenReturn(LocalDateTime.of(2026, 9, 25, 12, 0));
		TransacaoService service = service(contas, transacoes, dataAtual);
		var dados = new TransacaoUseCase.RegistrarCommand(TipoTransacao.SAIDA, new BigDecimal("120.00"),
				LocalDate.of(2026, 9, 21), "Mercado corrigido", 2L, null, null, List.of());

		service.corrigir(1L, 10L, new TransacaoUseCase.CorrigirCommand(dados, "Valor informado incorretamente"));

		ArgumentCaptor<TransacaoHistorico> captor = ArgumentCaptor.forClass(TransacaoHistorico.class);
		verify(transacoes).salvarHistorico(captor.capture());
		TransacaoHistorico historico = captor.getValue();
		assertThat(historico.getMotivo()).isEqualTo("Valor informado incorretamente");
		assertThat(historico.getCorrelacaoId()).matches("[0-9a-f-]{36}");
		assertThat(historico.getSnapshotAnterior()).contains("\"valor\":\"100.00\"", "\"descricao\":\"Mercado\"");
		assertThat(historico.getSnapshotNovo()).contains("\"valor\":\"120.00\"",
				"\"descricao\":\"Mercado corrigido\"");
	}

	@Test
	void recusaCorrecaoSemMotivo() {
		TransacaoService service = service(mock(ContaRepositoryPort.class), mock(TransacaoRepositoryPort.class),
				mock(ObterDataAtualPort.class));
		var dados = new TransacaoUseCase.RegistrarCommand(TipoTransacao.SAIDA, BigDecimal.ONE, LocalDate.now(),
				"Teste", 2L, null, null, List.of());

		assertThatThrownBy(() -> service.corrigir(1L, 10L, new TransacaoUseCase.CorrigirCommand(dados, " ")))
				.isInstanceOf(DomainException.class)
				.hasMessage("error.transacao.correcao.motivo.obrigatorio");
	}

	@Test
	void detalhaOcorrenciaRecorrenteSemRecalcularSaldoEComAuditoria() {
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		ItemRepositoryPort itens = mock(ItemRepositoryPort.class);
		CategoriaRepositoryPort categorias = mock(CategoriaRepositoryPort.class);
		ObterDataAtualPort dataAtual = mock(ObterDataAtualPort.class);
		Transacao recorrente = Transacao.reconstituir(10L, 1L, TipoTransacao.SAIDA,
				ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 9, 20), "Mercado", 2L, null,
				null, null, null, 30L, null, 0, List.of());
		when(transacoes.buscarPorIdEUsuarioParaAtualizacao(10L, 1L)).thenReturn(Optional.of(recorrente));
		when(transacoes.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
		when(itens.buscarPorIdEUsuario(40L, 1L)).thenReturn(Optional.of(Item.reconstituir(40L, 1L, "Alimentos", 50L, true)));
		when(categorias.buscarPorIdEUsuario(50L, 1L))
				.thenReturn(Optional.of(com.finisus.domain.model.Categoria.reconstituir(50L, 1L, "Alimentação", null, true)));
		when(dataAtual.obterDataHora()).thenReturn(LocalDateTime.of(2026, 9, 29, 12, 0));
		TransacaoService service = service(contas, transacoes, dataAtual, itens, categorias,
				mock(VinculoTransacaoDivisaoRepositoryPort.class));

		Transacao detalhada = service.detalhar(1L, 10L, new TransacaoUseCase.DetalharCommand(
				List.of(new TransacaoUseCase.ItemCommand(40L, new BigDecimal("100.00"))), "Detalhamento posterior"));

		assertThat(detalhada.getRecorrenciaId()).isEqualTo(30L);
		assertThat(detalhada.getValor().valor()).isEqualByComparingTo("100.00");
		assertThat(detalhada.getItens()).singleElement().satisfies(item -> {
			assertThat(item.getDescricao()).isEqualTo("Alimentos");
			assertThat(item.getCategoriaId()).isEqualTo(50L);
		});
		verify(contas, never()).salvar(any());
		ArgumentCaptor<TransacaoHistorico> captor = ArgumentCaptor.forClass(TransacaoHistorico.class);
		verify(transacoes).salvarHistorico(captor.capture());
		assertThat(captor.getValue().getCampoAlterado()).isEqualTo("DETALHAMENTO_ITENS");
		assertThat(captor.getValue().getMotivo()).isEqualTo("Detalhamento posterior");
	}

	@Test
	void permiteDetalharGastoDeCartaoMasRecusaParcelaDeCompra() {
		Transacao cartao = Transacao.reconstituir(10L, 1L, TipoTransacao.SAIDA, ValorMonetario.of(BigDecimal.TEN),
				LocalDate.now(), "Cartão", 2L, null, null, 20L, null, null, null, null, 0, List.of());
		Transacao parcela = Transacao.reconstituir(11L, 1L, TipoTransacao.SAIDA, ValorMonetario.of(BigDecimal.TEN),
				LocalDate.now(), "Parcela", 2L, null, null, 20L, null, 30L, null, null, 0, List.of());
		List<TransacaoItem> itens = List.of(TransacaoItem.novo(40L, "Item", ValorMonetario.of(BigDecimal.TEN), null));

		assertThat(cartao.detalhada(itens).getFaturaId()).isEqualTo(20L);
		assertThatThrownBy(() -> parcela.detalhada(itens)).isInstanceOf(DomainException.class)
				.hasMessage("error.transacao.detalhamento.origem.nao.permitida");
	}

	@Test
	void recusaSubstituirItensComCompartilhamentoAtivo() {
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		VinculoTransacaoDivisaoRepositoryPort vinculos = mock(VinculoTransacaoDivisaoRepositoryPort.class);
		TransacaoItem itemAtual = TransacaoItem.reconstituir(20L, 40L, "Item", ValorMonetario.of(BigDecimal.TEN), null);
		Transacao transacao = Transacao.reconstituir(10L, 1L, TipoTransacao.SAIDA, ValorMonetario.of(BigDecimal.TEN),
				LocalDate.now(), "Compra", 2L, null, null, null, null, null, null, null, 0, List.of(itemAtual));
		when(transacoes.buscarPorIdEUsuarioParaAtualizacao(10L, 1L)).thenReturn(Optional.of(transacao));
		when(vinculos.existePorTransacaoId(10L)).thenReturn(true);
		TransacaoService service = service(mock(ContaRepositoryPort.class), transacoes,
				mock(ObterDataAtualPort.class), mock(ItemRepositoryPort.class), vinculos);

		assertThatThrownBy(() -> service.detalhar(1L, 10L, new TransacaoUseCase.DetalharCommand(
				List.of(new TransacaoUseCase.ItemCommand(40L, BigDecimal.TEN)), "Reclassificação")))
				.isInstanceOf(DomainException.class).hasMessage("error.transacao.compartilhamento.ativo");
		verify(transacoes, never()).salvar(any());
	}

	@Test
	void detalhaComLinhaLivreSemExigirCatalogo() {
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		ItemRepositoryPort catalogo = mock(ItemRepositoryPort.class);
		Transacao original = Transacao.reconstituir(10L, 1L, TipoTransacao.SAIDA,
				ValorMonetario.of(new BigDecimal("25.00")), LocalDate.of(2026, 9, 29), "Importação", 2L, null,
				null, null, null, null, null, null, 0, List.of());
		when(transacoes.buscarPorIdEUsuarioParaAtualizacao(10L, 1L)).thenReturn(Optional.of(original));
		when(transacoes.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
		TransacaoService service = service(contas, transacoes, mock(ObterDataAtualPort.class), catalogo,
				mock(VinculoTransacaoDivisaoRepositoryPort.class));

		Transacao detalhada = service.detalhar(1L, 10L, new TransacaoUseCase.DetalharCommand(
				List.of(new TransacaoUseCase.ItemCommand(null, "2 cafés", new BigDecimal("2.000000"),
						new BigDecimal("25.00"), null)), "Descrição importada"));

		assertThat(detalhada.getItens()).singleElement().satisfies(item -> {
			assertThat(item.getItemId()).isNull();
			assertThat(item.getDescricao()).isEqualTo("2 cafés");
			assertThat(item.getQuantidade()).isEqualByComparingTo("2.000000");
		});
		verify(contas, never()).salvar(any());
		verify(catalogo, never()).buscarPorIdEUsuario(any(), any());
	}

	private TransacaoService service(ContaRepositoryPort contas, TransacaoRepositoryPort transacoes,
			ObterDataAtualPort dataAtual) {
		return service(contas, transacoes, dataAtual, mock(ItemRepositoryPort.class),
				mock(VinculoTransacaoDivisaoRepositoryPort.class));
	}

	private TransacaoService service(ContaRepositoryPort contas, TransacaoRepositoryPort transacoes,
			ObterDataAtualPort dataAtual, ItemRepositoryPort itens, VinculoTransacaoDivisaoRepositoryPort vinculos) {
		return service(contas, transacoes, dataAtual, itens, mock(CategoriaRepositoryPort.class), vinculos);
	}

	private TransacaoService service(ContaRepositoryPort contas, TransacaoRepositoryPort transacoes,
			ObterDataAtualPort dataAtual, ItemRepositoryPort itens, CategoriaRepositoryPort categorias,
			VinculoTransacaoDivisaoRepositoryPort vinculos) {
		return new TransacaoService(contas, categorias,
				mock(MeioPagamentoRepositoryPort.class), itens, transacoes, dataAtual, vinculos,
				mock(ObrigacaoFinanceiraRepositoryPort.class),
				mock(ParcelaFinanciamentoRepositoryPort.class));
	}
}
