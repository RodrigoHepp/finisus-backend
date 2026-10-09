package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.MeioPagamentoRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.RecorrenciaRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.OcorrenciaRecorrencia;
import com.finisus.domain.model.Recorrencia;
import com.finisus.domain.model.StatusOcorrenciaRecorrencia;
import com.finisus.domain.model.TipoConta;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.vo.ValorMonetario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RecorrenciaServiceTest {
	@Test
	void gerarMesCriaOcorrenciaPendenteSemMovimentarCaixa() {
		RecorrenciaRepositoryPort recorrencias = mock(RecorrenciaRepositoryPort.class);
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		ObterDataAtualPort dataAtual = mock(ObterDataAtualPort.class);
		Recorrencia recorrencia = Recorrencia.reconstituir(5L, 10L, "Aluguel", TipoTransacao.SAIDA,
				ValorMonetario.of(new BigDecimal("900.00")), 10, 3L, 2L, null, true);
		Conta conta = Conta.reconstituir(2L, 10L, "Carteira", TipoConta.FISICO, null,
				ValorMonetario.of(new BigDecimal("1000.00")), 0);
		when(dataAtual.obter()).thenReturn(LocalDate.of(2026, 9, 23));
		when(recorrencias.listarAtivasPorUsuario(10L)).thenReturn(List.of(recorrencia));
		when(contas.buscarPorIdEUsuario(2L, 10L)).thenReturn(Optional.of(conta));
		when(recorrencias.salvarOcorrencia(any())).thenAnswer(invocation -> invocation.getArgument(0));

		RecorrenciaService service = new RecorrenciaService(recorrencias, contas,
				mock(CategoriaRepositoryPort.class), mock(MeioPagamentoRepositoryPort.class), transacoes, dataAtual);
		List<OcorrenciaRecorrencia> resultado = service.gerarMes(10L, "2026-09");

		assertThat(resultado).singleElement().satisfies(o -> {
			assertThat(o.status()).isEqualTo(StatusOcorrenciaRecorrencia.PENDENTE);
			assertThat(o.vencimento()).isEqualTo(LocalDate.of(2026, 9, 10));
		});
		verify(transacoes, never()).salvar(any());
		verify(contas, never()).salvar(any());
	}
}
