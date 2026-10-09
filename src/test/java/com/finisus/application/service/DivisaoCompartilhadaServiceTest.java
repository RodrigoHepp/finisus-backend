package com.finisus.application.service;

import com.finisus.application.ports.out.DivisaoCompartilhadaRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.application.ports.out.VinculoTransacaoDivisaoRepositoryPort;
import com.finisus.application.ports.out.AlocacaoPagamentoDivisaoRepositoryPort;
import com.finisus.application.ports.out.ReembolsoDivisaoRepositoryPort;
import com.finisus.domain.model.DivisaoCompartilhada;
import com.finisus.domain.model.ParticipanteDivisao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.StatusSnapshotDivisao;
import com.finisus.domain.model.AlocacaoPagamentoDivisao;
import com.finisus.domain.model.StatusPagamentoDivisao;
import com.finisus.domain.model.ReembolsoDivisao;
import com.finisus.domain.model.Usuario;
import com.finisus.application.ports.in.AssociarTransacaoDivisaoUseCase;
import com.finisus.domain.vo.ValorMonetario;
import com.finisus.domain.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DivisaoCompartilhadaServiceTest {
    private final DivisaoCompartilhadaRepositoryPort divisoes = mock(DivisaoCompartilhadaRepositoryPort.class);
    private final VinculoTransacaoDivisaoRepositoryPort vinculos = mock(VinculoTransacaoDivisaoRepositoryPort.class);
    private final TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
    private final UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
    private final AlocacaoPagamentoDivisaoRepositoryPort alocacoes = mock(AlocacaoPagamentoDivisaoRepositoryPort.class);
    private final ReembolsoDivisaoRepositoryPort reembolsos = mock(ReembolsoDivisaoRepositoryPort.class);
    private final DivisaoCompartilhadaService service = new DivisaoCompartilhadaService(divisoes, vinculos,
            transacoes, usuarios, alocacoes, reembolsos);

    @Test
    void apuraCreditoQuandoUmParticipantePagaTodaADespesa() {
        DivisaoCompartilhada divisao = divisao(50, 50);
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(vinculos.listarLancamentosAtivos(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(lancamento(1L, "200.00", responsabilidade(1L, "50", "100.00"),
                        responsabilidade(2L, "50", "100.00"))));
        when(alocacoes.listarAtivas(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(alocacao(20L, 1L, "200.00")));

        var resumo = service.consultarResumo(1L, 10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertThat(resumo.total().valor()).isEqualByComparingTo("200.00");
        assertThat(resumo.participantes().get(0).pago().valor()).isEqualByComparingTo("200.00");
        assertThat(resumo.participantes().get(0).devido().valor()).isEqualByComparingTo("100.00");
        assertThat(resumo.participantes().get(0).saldo()).isEqualByComparingTo("100.00");
        assertThat(resumo.participantes().get(1).saldo()).isEqualByComparingTo("-100.00");
    }

    @Test
    void zeraOsSaldosQuandoCadaParticipantePagaSuaMetade() {
        DivisaoCompartilhada divisao = divisao(50, 50);
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(vinculos.listarLancamentosAtivos(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(
                        lancamento(1L, "100.00", responsabilidade(1L, "50", "50.00"),
                                responsabilidade(2L, "50", "50.00")),
                        lancamento(2L, "100.00", responsabilidade(1L, "50", "50.00"),
                                responsabilidade(2L, "50", "50.00"))));
        when(alocacoes.listarAtivas(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(alocacao(20L, 1L, "100.00"), alocacao(21L, 2L, "100.00")));

        var resumo = service.consultarResumo(2L, 10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertThat(resumo.participantes()).isNotEmpty().allSatisfy(participante ->
                assertThat(participante.saldo()).isEqualByComparingTo("0.00"));
    }

    @Test
    void distribuiCentavosSemPerderValorEmDivisaoComTresParticipantes() {
        DivisaoCompartilhada divisao = DivisaoCompartilhada.reconstituir(10L, 1L, "Mercado",
                List.of(new ParticipanteDivisao(1L, new BigDecimal("33.33")),
                        new ParticipanteDivisao(2L, new BigDecimal("33.33")),
                        new ParticipanteDivisao(3L, new BigDecimal("33.34"))), null);
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(vinculos.listarLancamentosAtivos(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(lancamento(3L, "100.00", responsabilidade(1L, "33.33", "33.33"),
                        responsabilidade(2L, "33.33", "33.33"),
                        responsabilidade(3L, "33.34", "33.34"))));
        when(alocacoes.listarAtivas(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(alocacao(20L, 3L, "100.00")));

        var resumo = service.consultarResumo(3L, 10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertThat(resumo.participantes().stream().map(participante -> participante.devido().valor())
                .reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo("100.00");
        assertThat(resumo.participantes().get(2).devido().valor()).isEqualByComparingTo("33.34");
    }

    @Test
    void materializaRateioPelosMaioresRestosComDesempatePeloIdDoUsuario() {
        DivisaoCompartilhada divisao = DivisaoCompartilhada.reconstituir(10L, 1L, "Mercado",
                List.of(new ParticipanteDivisao(3L, new BigDecimal("33.333333")),
                        new ParticipanteDivisao(1L, new BigDecimal("33.333334")),
                        new ParticipanteDivisao(2L, new BigDecimal("33.333333"))), null);
        Transacao transacao = Transacao.nova(1L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 8, 10), "Mercado", 5L,
                null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(transacoes.buscarPorId(20L)).thenReturn(Optional.of(transacao));

        service.associar(1L, 10L, 20L);

        verify(vinculos).associar(eq(10L), eq(20L), argThat(responsabilidades ->
                responsabilidades.size() == 3
                        && responsabilidades.get(0).usuarioId().equals(3L)
                        && responsabilidades.get(0).valorDevido().valor().compareTo(new BigDecimal("33.33")) == 0
                        && responsabilidades.get(1).usuarioId().equals(1L)
                        && responsabilidades.get(1).valorDevido().valor().compareTo(new BigDecimal("33.34")) == 0
                        && responsabilidades.get(2).usuarioId().equals(2L)
                        && responsabilidades.get(2).valorDevido().valor().compareTo(new BigDecimal("33.33")) == 0));
    }

    @Test
    void desempataRestosIguaisPeloMenorIdSemDependerDaOrdemDaColecao() {
        DivisaoCompartilhada divisao = DivisaoCompartilhada.reconstituir(10L, 1L, "Mercado",
                List.of(new ParticipanteDivisao(3L, new BigDecimal("33.33")),
                        new ParticipanteDivisao(1L, new BigDecimal("33.33")),
                        new ParticipanteDivisao(2L, new BigDecimal("33.34"))), null);
        Transacao transacao = Transacao.nova(1L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("0.02")), LocalDate.of(2026, 8, 10), "Mercado", 5L,
                null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(transacoes.buscarPorId(20L)).thenReturn(Optional.of(transacao));

        service.associar(1L, 10L, 20L);

        verify(vinculos).associar(eq(10L), eq(20L), argThat(responsabilidades ->
                responsabilidades.get(0).valorDevido().valor().compareTo(BigDecimal.ZERO) == 0
                        && responsabilidades.get(1).valorDevido().valor().compareTo(new BigDecimal("0.01")) == 0
                        && responsabilidades.get(2).valorDevido().valor().compareTo(new BigDecimal("0.01")) == 0));
    }

    @Test
    void divideIgualmenteQuandoOGrupoNaoPossuiPercentuaisFixos() {
        DivisaoCompartilhada divisao = DivisaoCompartilhada.reconstituir(10L, 1L, "Casa",
                List.of(new ParticipanteDivisao(3L, null), new ParticipanteDivisao(1L, null),
                        new ParticipanteDivisao(2L, null)), null);
        Transacao transacao = Transacao.nova(1L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 8, 10), "Mercado", 5L,
                null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(transacoes.buscarPorId(20L)).thenReturn(Optional.of(transacao));

        service.associar(1L, 10L, 20L);

        verify(vinculos).associar(eq(10L), eq(20L), argThat(responsabilidades ->
                responsabilidades.get(0).percentual().compareTo(new BigDecimal("33.33")) == 0
                        && responsabilidades.get(0).valorDevido().valor().compareTo(new BigDecimal("33.33")) == 0
                        && responsabilidades.get(1).percentual().compareTo(new BigDecimal("33.34")) == 0
                        && responsabilidades.get(1).valorDevido().valor().compareTo(new BigDecimal("33.34")) == 0
                        && responsabilidades.get(2).percentual().compareTo(new BigDecimal("33.33")) == 0
                        && responsabilidades.get(2).valorDevido().valor().compareTo(new BigDecimal("33.33")) == 0));
    }

    @Test
    void associaComResponsabilidadesDefinidasSomentePorValor() {
        DivisaoCompartilhada divisao = divisao(50, 50);
        Transacao transacao = Transacao.nova(1L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("300.00")), LocalDate.of(2026, 8, 10), "Hospedagem", 5L,
                null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(transacoes.buscarPorId(20L)).thenReturn(Optional.of(transacao));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(mock(Usuario.class)));
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(mock(Usuario.class)));

        service.associar(1L, 10L, 20L, null, List.of(
                new AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand(1L, null, new BigDecimal("180.00")),
                new AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand(2L, null, new BigDecimal("120.00"))));

        verify(vinculos).associar(eq(10L), eq(20L), argThat(responsabilidades ->
                responsabilidades.get(0).percentual().compareTo(new BigDecimal("60.00")) == 0
                        && responsabilidades.get(0).valorDevido().valor().compareTo(new BigDecimal("180.00")) == 0
                        && responsabilidades.get(1).percentual().compareTo(new BigDecimal("40.00")) == 0
                        && responsabilidades.get(1).valorDevido().valor().compareTo(new BigDecimal("120.00")) == 0));
    }

    @Test
    void associaComResponsabilidadesDefinidasSomentePorPercentual() {
        DivisaoCompartilhada divisao = divisao(50, 50);
        Transacao transacao = Transacao.nova(1L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 8, 10), "Hospedagem", 5L,
                null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(transacoes.buscarPorId(20L)).thenReturn(Optional.of(transacao));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(mock(Usuario.class)));
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(mock(Usuario.class)));

        service.associar(1L, 10L, 20L, null, List.of(
                new AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand(1L, new BigDecimal("20"), null),
                new AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand(2L, new BigDecimal("80"), null)));

        verify(vinculos).associar(eq(10L), eq(20L), argThat(responsabilidades ->
                responsabilidades.get(0).valorDevido().valor().compareTo(new BigDecimal("20.00")) == 0
                        && responsabilidades.get(1).valorDevido().valor().compareTo(new BigDecimal("80.00")) == 0));
    }

    @Test
    void recusaResponsabilidadeNovaParaUsuarioForaDaDivisao() {
        DivisaoCompartilhada divisao = divisao(50, 50);
        Transacao transacao = Transacao.nova(1L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 8, 10), "Hospedagem", 5L,
                null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(transacoes.buscarPorId(20L)).thenReturn(Optional.of(transacao));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(mock(Usuario.class)));
        when(usuarios.buscarPorId(3L)).thenReturn(Optional.of(mock(Usuario.class)));

        assertThatThrownBy(() -> service.associar(1L, 10L, 20L, null, List.of(
                new AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand(1L, null, new BigDecimal("50.00")),
                new AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand(3L, null, new BigDecimal("50.00")))))
                .isInstanceOf(DomainException.class).hasMessage("error.divisao.responsabilidades.invalidas");
        verify(vinculos, never()).associar(any(), any(), any());
    }

    @Test
    void compartilhaSomenteAParteInformadaDaTransacao() {
        DivisaoCompartilhada divisao = divisao(50, 50);
        Transacao transacao = Transacao.nova(1L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("300.00")), LocalDate.of(2026, 8, 10), "Mercado", 5L,
                null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(transacoes.buscarPorId(20L)).thenReturn(Optional.of(transacao));

        service.associar(1L, 10L, 20L, new BigDecimal("60.00"), null);

        verify(vinculos).associar(eq(10L), eq(20L), argThat(responsabilidades ->
                responsabilidades.stream().map(r -> r.valorDevido().valor()).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .compareTo(new BigDecimal("60.00")) == 0),
                argThat(base -> base.valor().compareTo(new BigDecimal("60.00")) == 0));
        verify(alocacoes).criarInicial(eq(10L), eq(20L), eq(1L),
                argThat(valor -> valor.valor().compareTo(new BigDecimal("60.00")) == 0));
        verify(vinculos, never()).associar(any(), any(), any());
    }

    @Test
    void recusaBaseCompartilhadaMaiorQueATransacao() {
        DivisaoCompartilhada divisao = divisao(50, 50);
        Transacao transacao = Transacao.nova(1L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("300.00")), LocalDate.of(2026, 8, 10), "Mercado", 5L,
                null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(transacoes.buscarPorId(20L)).thenReturn(Optional.of(transacao));

        assertThatThrownBy(() -> service.associar(1L, 10L, 20L, new BigDecimal("300.01"), null))
                .isInstanceOf(DomainException.class).hasMessage("error.divisao.base.compartilhada.invalida");
        verify(vinculos, never()).associar(any(), any(), any(), any());
    }

    @Test
    void cancelaVinculoSemSolicitarExclusaoDoHistorico() {
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao(50, 50)));
        when(vinculos.cancelar(10L, 20L, 1L)).thenReturn(true);

        service.desassociar(1L, 10L, 20L);

        verify(vinculos).cancelar(10L, 20L, 1L);
    }

    @Test
    void listaHistoricoSomenteParaParticipanteDaDivisao() {
        DivisaoCompartilhada divisao = divisao(50, 50);
        var historico = new com.finisus.domain.model.HistoricoParticipanteDivisao(2L, new BigDecimal("50"),
                LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 6, 1, 10, 0));
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(divisoes.listarHistoricoParticipantes(10L)).thenReturn(List.of(historico));

        assertThat(service.listarHistoricoParticipantes(1L, 10L)).containsExactly(historico);

        assertThatThrownBy(() -> service.listarHistoricoParticipantes(3L, 10L))
                .isInstanceOf(DomainException.class).hasMessage("error.recurso.sem.permissao");
    }

    @Test
    void preservaResponsabilidadesDoLancamentoDepoisDeAlterarParticipantes() {
        DivisaoCompartilhada atual = DivisaoCompartilhada.reconstituir(10L, 1L, "Água",
                List.of(new ParticipanteDivisao(1L, new BigDecimal("70")),
                        new ParticipanteDivisao(3L, new BigDecimal("30"))), null);
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(atual));
        when(vinculos.listarLancamentosAtivos(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(lancamento(2L, "100.00", responsabilidade(1L, "50", "50.00"),
                        responsabilidade(2L, "50", "50.00"))));
        when(alocacoes.listarAtivas(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(alocacao(20L, 2L, "100.00")));

        var resumo = service.consultarResumo(1L, 10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertThat(resumo.participantes()).extracting(p -> p.usuarioId()).containsExactly(1L, 3L, 2L);
        assertThat(resumo.participantes().get(0).devido().valor()).isEqualByComparingTo("50.00");
        assertThat(resumo.participantes().get(2).saldo()).isEqualByComparingTo("50.00");
    }

    @Test
    void recusaCompraDeCartaoQueAindaNaoImpactouOSaldo() {
        DivisaoCompartilhada divisao = divisao(50, 50);
        Transacao compra = Transacao.gastoCartao(1L, ValorMonetario.of(new BigDecimal("100.00")),
                LocalDate.of(2026, 8, 10), "Compra", 5L, null, 9L, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(transacoes.buscarPorId(20L)).thenReturn(Optional.of(compra));

        assertThatThrownBy(() -> service.associar(1L, 10L, 20L)).isInstanceOf(DomainException.class)
                .hasMessage("error.divisao.transacao.invalida");
        verify(vinculos, never()).associar(any(), any(), any());
    }

    @Test
    void revisaVinculoPendenteComResponsabilidadesInformadasExplicitamente() {
        DivisaoCompartilhada divisao = divisao(50, 50);
        Transacao transacao = Transacao.nova(1L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 8, 10), "Água", 5L,
                null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(vinculos.buscarStatusParaAtualizacao(10L, 20L))
                .thenReturn(Optional.of(StatusSnapshotDivisao.PENDENTE_REVISAO));
        when(transacoes.buscarPorId(20L)).thenReturn(Optional.of(transacao));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(mock(Usuario.class)));
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(mock(Usuario.class)));

        service.revisar(1L, 10L, 20L, List.of(
                new AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand(1L, new BigDecimal("50"),
                        new BigDecimal("50.00")),
                new AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand(2L, new BigDecimal("50"),
                        new BigDecimal("50.00"))));

        verify(vinculos).substituirResponsabilidades(org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.eq(20L), org.mockito.ArgumentMatchers.argThat(responsabilidades ->
                        responsabilidades.size() == 2
                                && responsabilidades.stream().map(r -> r.valorDevido().valor())
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .compareTo(new BigDecimal("100.00")) == 0));
    }

    @Test
    void recusaRevisaoQuandoValoresNaoFechamComATransacao() {
        DivisaoCompartilhada divisao = divisao(50, 50);
        Transacao transacao = Transacao.nova(1L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 8, 10), "Água", 5L,
                null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(vinculos.buscarStatusParaAtualizacao(10L, 20L))
                .thenReturn(Optional.of(StatusSnapshotDivisao.PENDENTE_REVISAO));
        when(transacoes.buscarPorId(20L)).thenReturn(Optional.of(transacao));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(mock(Usuario.class)));
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(mock(Usuario.class)));

        assertThatThrownBy(() -> service.revisar(1L, 10L, 20L, List.of(
                new AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand(1L, new BigDecimal("50"),
                        new BigDecimal("40.00")),
                new AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand(2L, new BigDecimal("50"),
                        new BigDecimal("50.00")))))
                .isInstanceOf(DomainException.class).hasMessage("error.divisao.responsabilidades.invalidas");
        verify(vinculos, never()).substituirResponsabilidades(any(), any(), any());
    }

    @Test
    void substituiAlocacaoInicialPorPagamentosParciaisDeVariosParticipantes() {
        DivisaoCompartilhada divisao = divisao(50, 50);
        Transacao pagamentoUm = Transacao.nova(1L, TipoTransacao.SAIDA, ValorMonetario.of(new BigDecimal("180.00")),
                LocalDate.of(2026, 8, 10), "Pagamento A", 5L, null, null, List.of());
        Transacao pagamentoDois = Transacao.nova(2L, TipoTransacao.SAIDA, ValorMonetario.of(new BigDecimal("200.00")),
                LocalDate.of(2026, 8, 11), "Pagamento B", 6L, null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(alocacoes.buscarBaseCompartilhadaAtivaParaAtualizacao(10L, 20L))
                .thenReturn(Optional.of(ValorMonetario.of(new BigDecimal("300.00"))));
        when(transacoes.buscarPorIdParaAtualizacao(30L)).thenReturn(Optional.of(pagamentoUm));
        when(transacoes.buscarPorIdParaAtualizacao(31L)).thenReturn(Optional.of(pagamentoDois));
        when(alocacoes.totalAtivoPorTransacaoExcluindoVinculo(30L, 10L, 20L)).thenReturn(BigDecimal.ZERO);
        when(alocacoes.totalAtivoPorTransacaoExcluindoVinculo(31L, 10L, 20L)).thenReturn(BigDecimal.ZERO);

        service.substituirAlocacoes(1L, 10L, 20L, List.of(
                new AssociarTransacaoDivisaoUseCase.AlocacaoCommand(30L, new BigDecimal("180.00")),
                new AssociarTransacaoDivisaoUseCase.AlocacaoCommand(31L, new BigDecimal("120.00"))));

        verify(alocacoes).substituir(eq(10L), eq(20L), eq(1L), argThat(novas -> novas.size() == 2
                && novas.get(0).pagadorId().equals(1L)
                && novas.get(1).pagadorId().equals(2L)
                && novas.stream().map(a -> a.valor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .compareTo(new BigDecimal("300.00")) == 0));
    }

    @Test
    void recusaAlocacoesQueExcedemABaseCompartilhada() {
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao(50, 50)));
        when(alocacoes.buscarBaseCompartilhadaAtivaParaAtualizacao(10L, 20L))
                .thenReturn(Optional.of(ValorMonetario.of(new BigDecimal("100.00"))));

        assertThatThrownBy(() -> service.substituirAlocacoes(1L, 10L, 20L, List.of(
                new AssociarTransacaoDivisaoUseCase.AlocacaoCommand(30L, new BigDecimal("100.01")))))
                .isInstanceOf(DomainException.class).hasMessage("error.divisao.alocacoes.excedem.base");
        verify(alocacoes, never()).substituir(any(), any(), any(), any());
    }

    @Test
    void recusaQuandoATransacaoDePagamentoJaEstaIntegralmenteAlocada() {
        Transacao pagamento = Transacao.nova(2L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 8, 11), "Pagamento", 6L,
                null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao(50, 50)));
        when(alocacoes.buscarBaseCompartilhadaAtivaParaAtualizacao(10L, 20L))
                .thenReturn(Optional.of(ValorMonetario.of(new BigDecimal("80.00"))));
        when(transacoes.buscarPorIdParaAtualizacao(30L)).thenReturn(Optional.of(pagamento));
        when(alocacoes.totalAtivoPorTransacaoExcluindoVinculo(30L, 10L, 20L))
                .thenReturn(new BigDecimal("30.00"));

        assertThatThrownBy(() -> service.substituirAlocacoes(1L, 10L, 20L, List.of(
                new AssociarTransacaoDivisaoUseCase.AlocacaoCommand(30L, new BigDecimal("80.00")))))
                .isInstanceOf(DomainException.class).hasMessage("error.divisao.alocacao.excede.transacao");
        verify(alocacoes, never()).substituir(any(), any(), any(), any());
    }

    @Test
    void resumoUsaAlocacoesAtivasEmVezDoPagadorOriginalDaDespesa() {
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao(50, 50)));
        when(vinculos.listarLancamentosAtivos(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(lancamento(1L, "300.00", responsabilidade(1L, "50", "150.00"),
                        responsabilidade(2L, "50", "150.00"))));
        when(alocacoes.listarAtivas(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(alocacao(30L, 1L, "180.00"), alocacao(31L, 2L, "120.00")));

        var resumo = service.consultarResumo(1L, 10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertThat(resumo.participantes().get(0).pago().valor()).isEqualByComparingTo("180.00");
        assertThat(resumo.participantes().get(0).saldo()).isEqualByComparingTo("30.00");
        assertThat(resumo.participantes().get(1).pago().valor()).isEqualByComparingTo("120.00");
        assertThat(resumo.participantes().get(1).saldo()).isEqualByComparingTo("-30.00");
    }

    @Test
    void informaSituacaoPendenteParcialEQuitadaDaDespesa() {
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao(50, 50)));
        when(alocacoes.buscarBaseCompartilhadaAtiva(10L, 20L))
                .thenReturn(Optional.of(ValorMonetario.of(new BigDecimal("100.00"))));
        when(alocacoes.listarAtivas(10L, 20L)).thenReturn(List.of(),
                List.of(alocacao(30L, 1L, "40.00")),
                List.of(alocacao(30L, 1L, "40.00"), alocacao(31L, 2L, "60.00")));

        var pendente = service.consultarPagamento(1L, 10L, 20L);
        var parcial = service.consultarPagamento(1L, 10L, 20L);
        var quitado = service.consultarPagamento(1L, 10L, 20L);

        assertThat(pendente.status()).isEqualTo(StatusPagamentoDivisao.PENDENTE);
        assertThat(pendente.pago().valor()).isEqualByComparingTo("0.00");
        assertThat(pendente.pendente().valor()).isEqualByComparingTo("100.00");
        assertThat(parcial.status()).isEqualTo(StatusPagamentoDivisao.PARCIAL);
        assertThat(parcial.pago().valor()).isEqualByComparingTo("40.00");
        assertThat(parcial.pendente().valor()).isEqualByComparingTo("60.00");
        assertThat(quitado.status()).isEqualTo(StatusPagamentoDivisao.QUITADO);
        assertThat(quitado.pendente().valor()).isEqualByComparingTo("0.00");
    }

    @Test
    void criadorCancelaUmaAlocacaoSemApagarSeuHistorico() {
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao(50, 50)));
        when(alocacoes.cancelar(10L, 20L, 30L, 1L)).thenReturn(true);

        service.cancelarAlocacao(1L, 10L, 20L, 30L);

        verify(alocacoes).cancelar(10L, 20L, 30L, 1L);
    }

    @Test
    void participanteQueNaoECriadorNaoCancelaAlocacao() {
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao(50, 50)));

        assertThatThrownBy(() -> service.cancelarAlocacao(2L, 10L, 20L, 30L))
                .isInstanceOf(DomainException.class).hasMessage("error.recurso.sem.permissao");
        verify(alocacoes, never()).cancelar(any(), any(), any(), any());
    }

    @Test
    void registraReembolsoComTransacaoRealDoPagador() {
        Transacao transacao = Transacao.nova(2L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 8, 20), "Acerto", 6L,
                null, null, List.of());
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao(50, 50)));
        when(divisoes.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(divisao(50, 50)));
        when(transacoes.buscarPorIdParaAtualizacao(30L)).thenReturn(Optional.of(transacao));
        when(alocacoes.totalAtivoPorTransacaoExcluindoVinculo(30L, -1L, -1L)).thenReturn(BigDecimal.ZERO);
        when(vinculos.listarLancamentosAtivos(10L, LocalDate.of(1000, 1, 1), LocalDate.of(2026, 8, 20)))
                .thenReturn(List.of(lancamento(1L, "200.00", responsabilidade(1L, "50", "100.00"),
                        responsabilidade(2L, "50", "100.00"))));
        when(alocacoes.listarAtivas(10L, LocalDate.of(1000, 1, 1), LocalDate.of(2026, 8, 20)))
                .thenReturn(List.of(alocacao(20L, 1L, "200.00")));

        service.registrarReembolso(2L, 10L, 30L, 1L, new BigDecimal("100.00"));

        verify(reembolsos).salvar(eq(10L), eq(30L), eq(2L), eq(1L),
                argThat(v -> v.valor().compareTo(new BigDecimal("100.00")) == 0));
    }

    @Test
    void recusaReembolsoMaiorQueODebitoEOCreditoEmAberto() {
        Transacao transacao = Transacao.nova(2L, TipoTransacao.SAIDA,
                ValorMonetario.of(new BigDecimal("150.00")), LocalDate.of(2026, 8, 20), "Acerto", 6L,
                null, null, List.of());
        DivisaoCompartilhada divisao = divisao(50, 50);
        when(divisoes.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(divisao));
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao));
        when(transacoes.buscarPorIdParaAtualizacao(30L)).thenReturn(Optional.of(transacao));
        when(alocacoes.totalAtivoPorTransacaoExcluindoVinculo(30L, -1L, -1L)).thenReturn(BigDecimal.ZERO);
        when(vinculos.listarLancamentosAtivos(10L, LocalDate.of(1000, 1, 1), LocalDate.of(2026, 8, 20)))
                .thenReturn(List.of(lancamento(1L, "200.00", responsabilidade(1L, "50", "100.00"),
                        responsabilidade(2L, "50", "100.00"))));
        when(alocacoes.listarAtivas(10L, LocalDate.of(1000, 1, 1), LocalDate.of(2026, 8, 20)))
                .thenReturn(List.of(alocacao(20L, 1L, "200.00")));

        assertThatThrownBy(() -> service.registrarReembolso(2L, 10L, 30L, 1L, new BigDecimal("100.01")))
                .isInstanceOf(DomainException.class).hasMessage("error.divisao.reembolso.excede.saldo");
        verify(reembolsos, never()).salvar(any(), any(), any(), any(), any());
    }

    @Test
    void reembolsoTransfereCreditoSemAlterarASomaDosSaldos() {
        when(divisoes.buscarPorId(10L)).thenReturn(Optional.of(divisao(50, 50)));
        when(vinculos.listarLancamentosAtivos(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(lancamento(1L, "200.00", responsabilidade(1L, "50", "100.00"),
                        responsabilidade(2L, "50", "100.00"))));
        when(alocacoes.listarAtivas(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(alocacao(20L, 1L, "200.00")));
        when(reembolsos.listarAtivos(10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(new ReembolsoDivisao(30L, 40L, 2L, 1L,
                        ValorMonetario.of(new BigDecimal("100.00")), LocalDate.of(2026, 8, 20),
                        LocalDateTime.now(), null, null)));

        var resumo = service.consultarResumo(1L, 10L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertThat(resumo.participantes()).isNotEmpty()
                .allSatisfy(p -> assertThat(p.saldo()).isEqualByComparingTo("0.00"));
    }

    @Test
    void criadorCancelaReembolsoSemApagarHistorico() {
        when(divisoes.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(divisao(50, 50)));
        when(reembolsos.cancelar(10L, 30L, 1L)).thenReturn(true);

        service.cancelarReembolso(1L, 10L, 30L);

        verify(reembolsos).cancelar(10L, 30L, 1L);
    }

    @Test
    void participanteQueNaoECriadorNaoCancelaReembolso() {
        when(divisoes.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(divisao(50, 50)));

        assertThatThrownBy(() -> service.cancelarReembolso(2L, 10L, 30L))
                .isInstanceOf(DomainException.class).hasMessage("error.recurso.sem.permissao");
        verify(reembolsos, never()).cancelar(any(), any(), any());
    }

    @Test
    void recusaCancelarReembolsoInexistenteOuJaCancelado() {
        when(divisoes.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(divisao(50, 50)));

        assertThatThrownBy(() -> service.cancelarReembolso(1L, 10L, 30L))
                .isInstanceOf(DomainException.class).hasMessage("error.recurso.nao.encontrado");
    }

    private DivisaoCompartilhada divisao(int percentualPrimeiro, int percentualSegundo) {
        return DivisaoCompartilhada.reconstituir(10L, 1L, "Água",
                List.of(new ParticipanteDivisao(1L, BigDecimal.valueOf(percentualPrimeiro)),
                        new ParticipanteDivisao(2L, BigDecimal.valueOf(percentualSegundo))), null);
    }

    private VinculoTransacaoDivisaoRepositoryPort.LancamentoDivisao lancamento(Long usuarioId, String valor,
            VinculoTransacaoDivisaoRepositoryPort.Responsabilidade... responsabilidades) {
        return new VinculoTransacaoDivisaoRepositoryPort.LancamentoDivisao(usuarioId,
                ValorMonetario.of(new BigDecimal(valor)), List.of(responsabilidades), false);
    }

    private VinculoTransacaoDivisaoRepositoryPort.Responsabilidade responsabilidade(Long usuarioId,
            String percentual, String valor) {
        return new VinculoTransacaoDivisaoRepositoryPort.Responsabilidade(usuarioId, new BigDecimal(percentual),
                ValorMonetario.of(new BigDecimal(valor)));
    }

    private AlocacaoPagamentoDivisao alocacao(Long transacaoId, Long pagadorId, String valor) {
        return new AlocacaoPagamentoDivisao(null, transacaoId, pagadorId,
                ValorMonetario.of(new BigDecimal(valor)), LocalDateTime.now(), null, null);
    }
}
