package com.financeiro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import com.financeiro.application.ports.in.AutenticarUsuarioUseCase;
import com.financeiro.application.ports.in.CadastrarUsuarioUseCase;
import com.financeiro.application.ports.in.FinanceiroCoreUseCase;
import com.financeiro.application.ports.in.CompraParceladaUseCase;
import com.financeiro.application.ports.in.PrevisaoFluxoCaixaUseCase;
import com.financeiro.application.ports.in.RecorrenciaUseCase;
import com.financeiro.application.ports.in.CartaoCreditoUseCase;
import com.financeiro.application.ports.in.FinanciamentoUseCase;
import com.financeiro.application.ports.in.GerenciarPerfilUseCase;
import com.financeiro.application.ports.in.RenovarTokenUseCase;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.adapters.out.persistence.repository.RefreshTokenJpaRepository;
import com.financeiro.domain.model.TipoConta;
import com.financeiro.domain.model.TipoTransacao;
import com.financeiro.domain.model.StatusFatura;
import com.financeiro.domain.model.StatusParcelaFinanciamento;
import com.financeiro.domain.DomainException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
class AuthApiIntegrationTest {
    @Autowired CadastrarUsuarioUseCase cadastrarUsuario;
    @Autowired AutenticarUsuarioUseCase autenticarUsuario;
    @Autowired FinanceiroCoreUseCase financeiroCore;
    @Autowired CompraParceladaUseCase comprasParceladas;
    @Autowired RecorrenciaUseCase recorrencias;
    @Autowired PrevisaoFluxoCaixaUseCase previsoes;
    @Autowired CartaoCreditoUseCase cartoes;
    @Autowired FinanciamentoUseCase financiamentos;
    @Autowired GerenciarPerfilUseCase perfis;
    @Autowired RenovarTokenUseCase renovarToken;
    @Autowired RefreshTokenJpaRepository refreshTokens;
    @Autowired JwtDecoder jwtDecoder;

    @Test
    void cadastraUsuarioEAutentica() throws Exception {
        cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Ana", "ana@example.com", "senha-segura"));
        var tokens = autenticarUsuario.executar(new AutenticarUsuarioUseCase.Command("ana@example.com", "senha-segura"));
        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
    }

    @Test
    void registraTransacaoComItensEAtualizaSaldoDaConta() {
        Long usuarioId = cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Bia", "bia@example.com", "senha-segura")).id();
        var conta = financeiroCore.criarConta(usuarioId, new FinanceiroCoreUseCase.CriarContaCommand("Carteira", TipoConta.FISICO, null));
        var categoria = financeiroCore.criarCategoria(usuarioId, new FinanceiroCoreUseCase.CriarCategoriaCommand("Alimentação", null));
        var transacao = financeiroCore.registrarTransacao(usuarioId, new FinanceiroCoreUseCase.RegistrarTransacaoCommand(
                TipoTransacao.ENTRADA, new BigDecimal("100.00"), LocalDate.now(), "Freelance", conta.getId(), categoria.getId(), null,
                List.of(new FinanceiroCoreUseCase.ItemCommand("Projeto", new BigDecimal("100.00"), categoria.getId()))));
        assertThat(transacao.getId()).isNotNull();
        assertThat(financeiroCore.listarContas(usuarioId).getFirst().getSaldo().valor()).isEqualByComparingTo("100.00");
    }

    @Test
    void paginaTransacoesNoBancoComMetadadosConsistentes() {
        Long usuarioId = cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Paula", "paula@example.com", "senha-segura")).id();
        var conta = financeiroCore.criarConta(usuarioId, new FinanceiroCoreUseCase.CriarContaCommand("Carteira", TipoConta.FISICO, null));
        for (int indice = 1; indice <= 3; indice++) {
            financeiroCore.registrarTransacao(usuarioId, new FinanceiroCoreUseCase.RegistrarTransacaoCommand(
                    TipoTransacao.ENTRADA, new BigDecimal("10.00"), LocalDate.of(2026, 12, indice), "Entrada " + indice,
                    conta.getId(), null, null, List.of()));
        }

        var pagina = financeiroCore.listarTransacoes(usuarioId, new Paginacao(1, 2));

        assertThat(pagina.totalElementos()).isEqualTo(3);
        assertThat(pagina.totalPaginas()).isEqualTo(2);
        assertThat(pagina.conteudo()).hasSize(1);
        assertThat(pagina.conteudo().getFirst().getDescricao()).isEqualTo("Entrada 1");
    }

    @Test
    void geraParcelasEAPrevisaoComLancamentosFuturosConhecidos() {
        Long usuarioId = cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Caio", "caio@example.com", "senha-segura")).id();
        var conta = financeiroCore.criarConta(usuarioId, new FinanceiroCoreUseCase.CriarContaCommand("Dinheiro", TipoConta.FISICO, null));
        comprasParceladas.criar(usuarioId, new CompraParceladaUseCase.CriarCommand("Notebook", new BigDecimal("100.00"), 3, LocalDate.now(), null, conta.getId()));
        assertThat(financeiroCore.listarTransacoes(usuarioId)).hasSize(3);
        assertThat(previsoes.recalcular(usuarioId, 3)).isNotEmpty();
    }

    @Test
    void corrigeEEstornaTransacaoRecompondoSaldoERegistrandoHistorico() {
        Long usuarioId = cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Dani", "dani@example.com", "senha-segura")).id();
        var conta = financeiroCore.criarConta(usuarioId, new FinanceiroCoreUseCase.CriarContaCommand("Carteira", TipoConta.FISICO, null));
        var transacao = financeiroCore.registrarTransacao(usuarioId, new FinanceiroCoreUseCase.RegistrarTransacaoCommand(TipoTransacao.ENTRADA, new BigDecimal("100.00"), LocalDate.now(), "Venda", conta.getId(), null, null, List.of()));
        financeiroCore.corrigirTransacao(usuarioId, transacao.getId(), new FinanceiroCoreUseCase.RegistrarTransacaoCommand(TipoTransacao.ENTRADA, new BigDecimal("120.00"), LocalDate.now(), "Venda corrigida", conta.getId(), null, null, List.of()));
        assertThat(financeiroCore.buscarConta(usuarioId, conta.getId()).getSaldo().valor()).isEqualByComparingTo("120.00");
        financeiroCore.estornarTransacao(usuarioId, transacao.getId());
        assertThat(financeiroCore.buscarConta(usuarioId, conta.getId()).getSaldo().valor()).isEqualByComparingTo("0.00");
        assertThat(financeiroCore.listarHistoricoTransacao(usuarioId, transacao.getId())).hasSize(3);
    }

    @Test
    void estornarLancamentosSemImpactoNoSaldoNaoAlteraConta() {
        Long usuarioId = cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Nina", "nina@example.com", "senha-segura")).id();
        var conta = financeiroCore.criarConta(usuarioId, new FinanceiroCoreUseCase.CriarContaCommand("Carteira", TipoConta.FISICO, null));
        financeiroCore.registrarTransacao(usuarioId, new FinanceiroCoreUseCase.RegistrarTransacaoCommand(
                TipoTransacao.ENTRADA, new BigDecimal("100.00"), LocalDate.now(), "Saldo inicial", conta.getId(), null, null, List.of()));
        var cartao = cartoes.criarCartao(usuarioId, new CartaoCreditoUseCase.CriarCartaoCommand("Principal", new BigDecimal("500.00"), 10, 20));
        var fatura = cartoes.criarFatura(usuarioId, new CartaoCreditoUseCase.CriarFaturaCommand(cartao.getId(), "2026-08", LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 20), conta.getId()));
        var gastoCartao = cartoes.lancarGasto(usuarioId, new CartaoCreditoUseCase.LancarGastoCommand(
                fatura.getId(), new BigDecimal("30.00"), LocalDate.of(2026, 8, 2), "Mercado", conta.getId(), null, List.of()));

        financeiroCore.estornarTransacao(usuarioId, gastoCartao.getId());

        var compra = comprasParceladas.criar(usuarioId, new CompraParceladaUseCase.CriarCommand(
                "Notebook", new BigDecimal("60.00"), 2, LocalDate.now().plusMonths(1), null, conta.getId()));
        var parcelaFutura = financeiroCore.listarTransacoes(usuarioId).stream()
                .filter(transacao -> compra.getId().equals(transacao.getCompraParceladaId()))
                .findFirst()
                .orElseThrow();
        financeiroCore.estornarTransacao(usuarioId, parcelaFutura.getId());

        assertThat(financeiroCore.buscarConta(usuarioId, conta.getId()).getSaldo().valor()).isEqualByComparingTo("100.00");
    }

    @Test
    void impedeGeracaoDeRecorrenciaParaCompetenciaFutura() {
        Long usuarioId = cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Otto", "otto@example.com", "senha-segura")).id();

        assertThatThrownBy(() -> recorrencias.gerarMes(usuarioId, YearMonth.from(LocalDate.now()).plusMonths(1).toString()))
                .isInstanceOf(DomainException.class)
                .hasMessage("error.recorrencia.periodo.futuro");
    }

    @Test
    void pagaFaturaEParcelaDeFinanciamentoComTransacoesAuditaveis() {
        Long usuarioId = cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Eli", "eli@example.com", "senha-segura")).id();
        var conta = financeiroCore.criarConta(usuarioId, new FinanceiroCoreUseCase.CriarContaCommand("Carteira", TipoConta.FISICO, null));
        financeiroCore.registrarTransacao(usuarioId, new FinanceiroCoreUseCase.RegistrarTransacaoCommand(TipoTransacao.ENTRADA, new BigDecimal("100.00"), LocalDate.of(2026, 8, 1), "Saldo inicial", conta.getId(), null, null, List.of()));
        var cartao = cartoes.criarCartao(usuarioId, new CartaoCreditoUseCase.CriarCartaoCommand("Principal", new BigDecimal("500.00"), 10, 20));
        var fatura = cartoes.criarFatura(usuarioId, new CartaoCreditoUseCase.CriarFaturaCommand(cartao.getId(), "2026-08", LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 20), conta.getId()));
        cartoes.lancarGasto(usuarioId, new CartaoCreditoUseCase.LancarGastoCommand(fatura.getId(), new BigDecimal("50.00"), LocalDate.of(2026, 8, 2), "Mercado", conta.getId(), null, List.of()));
        cartoes.fechar(usuarioId, fatura.getId());
        assertThat(cartoes.pagar(usuarioId, fatura.getId(), LocalDate.of(2026, 8, 20)).getStatus()).isEqualTo(StatusFatura.PAGA);
        var financiamento = financiamentos.criar(usuarioId, new FinanciamentoUseCase.CriarCommand("Computador", new BigDecimal("100.00"), BigDecimal.ZERO, 2, LocalDate.of(2026, 9, 1), conta.getId()));
        var parcela = financiamentos.listarParcelas(usuarioId, financiamento.getId()).getFirst();
        assertThat(financiamentos.pagarParcela(usuarioId, financiamento.getId(), parcela.getId(), LocalDate.of(2026, 9, 1)).getStatus()).isEqualTo(StatusParcelaFinanciamento.PAGA);
        assertThat(financeiroCore.buscarConta(usuarioId, conta.getId()).getSaldo().valor()).isEqualByComparingTo("0.00");
    }

    @Test
    void serializaGastosConcorrentesDaFaturaParaNaoUltrapassarOLimiteDoCartao() throws Exception {
        Long usuarioId = cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Lia", "lia@example.com", "senha-segura")).id();
        var conta = financeiroCore.criarConta(usuarioId, new FinanceiroCoreUseCase.CriarContaCommand("Carteira", TipoConta.FISICO, null));
        var cartao = cartoes.criarCartao(usuarioId, new CartaoCreditoUseCase.CriarCartaoCommand("Principal", new BigDecimal("100.00"), 10, 20));
        var fatura = cartoes.criarFatura(usuarioId, new CartaoCreditoUseCase.CriarFaturaCommand(cartao.getId(), "2026-11", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), conta.getId()));
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch prontos = new CountDownLatch(4);
        CountDownLatch iniciar = new CountDownLatch(1);
        try {
            List<Future<Boolean>> resultados = java.util.stream.IntStream.range(0, 4)
                    .mapToObj(indice -> executor.submit(() -> {
                        prontos.countDown();
                        iniciar.await();
                        try {
                            cartoes.lancarGasto(usuarioId, new CartaoCreditoUseCase.LancarGastoCommand(
                                    fatura.getId(), new BigDecimal("40.00"), LocalDate.of(2026, 11, 1),
                                    "Compra " + indice, conta.getId(), null, List.of()));
                            return true;
                        } catch (DomainException exception) {
                            assertThat(exception).hasMessage("error.cartao.limite.excedido");
                            return false;
                        }
                    }))
                    .toList();
            assertThat(prontos.await(5, TimeUnit.SECONDS)).isTrue();
            iniciar.countDown();

            long gastosAceitos = resultados.stream().filter(resultado -> {
                try {
                    return resultado.get(10, TimeUnit.SECONDS);
                } catch (Exception exception) {
                    throw new AssertionError(exception);
                }
            }).count();
            assertThat(gastosAceitos).isEqualTo(2);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void calculaParcelaComTaxaMensalSemDividirPeloNumeroDeMesesDoAno() {
        Long usuarioId = cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Gabi", "gabi@example.com", "senha-segura")).id();
        var conta = financeiroCore.criarConta(usuarioId, new FinanceiroCoreUseCase.CriarContaCommand("Carteira", TipoConta.FISICO, null));

        var financiamento = financiamentos.criar(usuarioId, new FinanciamentoUseCase.CriarCommand(
                "Crédito mensal", new BigDecimal("1200.00"), new BigDecimal("1.00"), 1,
                LocalDate.of(2026, 10, 1), conta.getId()));

        assertThat(financiamentos.listarParcelas(usuarioId, financiamento.getId()).getFirst().getValor().valor())
                .isEqualByComparingTo("1212.00");
    }

    @Test
    void bloqueiaNovosLancamentosEComprasEmContaInativa() {
        Long usuarioId = cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Hugo", "hugo@example.com", "senha-segura")).id();
        var conta = financeiroCore.criarConta(usuarioId, new FinanceiroCoreUseCase.CriarContaCommand("Carteira", TipoConta.FISICO, null));
        financeiroCore.inativarConta(usuarioId, conta.getId());

        assertThatThrownBy(() -> financeiroCore.registrarTransacao(usuarioId,
                new FinanceiroCoreUseCase.RegistrarTransacaoCommand(TipoTransacao.ENTRADA, new BigDecimal("10.00"), LocalDate.now(), "Entrada", conta.getId(), null, null, List.of())))
                .isInstanceOf(DomainException.class)
                .hasMessage("error.conta.inativa");
        assertThatThrownBy(() -> comprasParceladas.criar(usuarioId,
                new CompraParceladaUseCase.CriarCommand("Compra", new BigDecimal("10.00"), 1, LocalDate.now(), null, conta.getId())))
                .isInstanceOf(DomainException.class)
                .hasMessage("error.conta.inativa");
    }

    @Test
    void atualizaEDesativaPerfilInvalidandoSuasSessoes() {
        Long usuarioId = cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Fabi", "fabi@example.com", "senha-segura")).id();
        assertThat(perfis.atualizar(usuarioId, new GerenciarPerfilUseCase.AtualizarCommand("Fabiana", "fabiana@example.com")).getEmail().valor()).isEqualTo("fabiana@example.com");
        perfis.desativar(usuarioId);
        assertThat(perfis.consultar(usuarioId).isAtivo()).isFalse();
    }

    @Test
    void rotacionaRefreshTokenPorHashEInvalidaAccessTokenAoDesativarPerfil() {
        Long usuarioId = cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Iara", "iara@example.com", "senha-segura")).id();
        var primeiroLogin = autenticarUsuario.executar(new AutenticarUsuarioUseCase.Command("iara@example.com", "senha-segura"));

        assertThat(refreshTokens.findAll()).anySatisfy(token ->
                assertThat(token.getToken()).isNotEqualTo(primeiroLogin.refreshToken()));

        var tokensRotacionados = renovarToken.executar(new RenovarTokenUseCase.Command(primeiroLogin.refreshToken()));
        assertThatThrownBy(() -> renovarToken.executar(new RenovarTokenUseCase.Command(primeiroLogin.refreshToken())))
                .isInstanceOf(DomainException.class)
                .hasMessage("error.auth.refresh.invalid");
        assertThat(jwtDecoder.decode(tokensRotacionados.accessToken()).getSubject()).isNotBlank();

        perfis.desativar(usuarioId);

        assertThatThrownBy(() -> jwtDecoder.decode(tokensRotacionados.accessToken()))
                .isInstanceOf(JwtValidationException.class);
        assertThatThrownBy(() -> renovarToken.executar(new RenovarTokenUseCase.Command(tokensRotacionados.refreshToken())))
                .isInstanceOf(DomainException.class)
                .hasMessage("error.auth.refresh.invalid");
    }
}
