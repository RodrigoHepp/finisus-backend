package com.financeiro.infrastructure.config;

import com.financeiro.application.ports.in.*;
import com.financeiro.application.ports.out.*;
import com.financeiro.application.service.*;
import com.financeiro.infrastructure.observability.OperacaoFinanceiraMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Supplier;

@Configuration
public class UseCaseConfiguration {

    @Bean
    AutenticacaoService autenticacaoService(UsuarioRepositoryPort usuarios, PasswordEncoderPort passwordEncoder,
                                            TokenPort tokens, RefreshTokenRepositoryPort refreshTokens,
                                            ObterDataAtualPort dataAtual) {
        return new AutenticacaoService(usuarios, passwordEncoder, tokens, refreshTokens, dataAtual);
    }

    @Bean @Primary
    CadastrarUsuarioUseCase cadastrarUsuarioUseCase(AutenticacaoService service, PlatformTransactionManager manager) {
        return transacional(CadastrarUsuarioUseCase.class, service, manager, null, Map.of());
    }

    @Bean @Primary
    AutenticarUsuarioUseCase autenticarUsuarioUseCase(AutenticacaoService service, PlatformTransactionManager manager) {
        return transacional(AutenticarUsuarioUseCase.class, service, manager, null, Map.of());
    }

    @Bean @Primary
    RenovarTokenUseCase renovarTokenUseCase(AutenticacaoService service, PlatformTransactionManager manager) {
        return transacional(RenovarTokenUseCase.class, service, manager, null, Map.of());
    }

    @Bean
    CartaoCreditoService cartaoCreditoService(CartaoCreditoRepositoryPort cartoes, FaturaRepositoryPort faturas,
                                              ContaRepositoryPort contas, CategoriaRepositoryPort categorias,
                                              TransacaoRepositoryPort transacoes, ObterDataAtualPort dataAtual) {
        return new CartaoCreditoService(cartoes, faturas, contas, categorias, transacoes, dataAtual);
    }

    @Bean @Primary
    CartaoCreditoUseCase cartaoCreditoUseCase(CartaoCreditoService service, PlatformTransactionManager manager,
                                              OperacaoFinanceiraMetrics metrics) {
        return transacional(CartaoCreditoUseCase.class, service, manager, metrics,
                Map.of("lancarGasto", "gasto_cartao", "pagar", "pagamento_fatura"));
    }

    @Bean
    CompartilhamentoService compartilhamentoService(CompartilhamentoRepositoryPort compartilhamentos,
                                                    TransacaoRepositoryPort transacoes, UsuarioRepositoryPort usuarios) {
        return new CompartilhamentoService(compartilhamentos, transacoes, usuarios);
    }

    @Bean @Primary
    CompartilhamentoUseCase compartilhamentoUseCase(CompartilhamentoService service, PlatformTransactionManager manager) {
        return transacional(CompartilhamentoUseCase.class, service, manager, null, Map.of());
    }

    @Bean
    CompraParceladaService compraParceladaService(CompraParceladaRepositoryPort compras, ContaRepositoryPort contas,
                                                  CategoriaRepositoryPort categorias, TransacaoRepositoryPort transacoes,
                                                  ObterDataAtualPort dataAtual) {
        return new CompraParceladaService(compras, contas, categorias, transacoes, dataAtual);
    }

    @Bean @Primary
    CompraParceladaUseCase compraParceladaUseCase(CompraParceladaService service, PlatformTransactionManager manager) {
        return transacional(CompraParceladaUseCase.class, service, manager, null, Map.of());
    }

    @Bean
    FinanceiroCoreService financeiroCoreService(BancoRepositoryPort bancos, ContaRepositoryPort contas,
                                                CategoriaRepositoryPort categorias, MeioPagamentoRepositoryPort meios,
                                                TransacaoRepositoryPort transacoes, ObterDataAtualPort dataAtual) {
        return new FinanceiroCoreService(bancos, contas, categorias, meios, transacoes, dataAtual);
    }

    @Bean @Primary
    FinanceiroCoreUseCase financeiroCoreUseCase(FinanceiroCoreService service, PlatformTransactionManager manager,
                                                OperacaoFinanceiraMetrics metrics) {
        return transacional(FinanceiroCoreUseCase.class, service, manager, metrics, Map.of("registrarTransacao", "transacao"));
    }

    @Bean
    InvestimentoService investimentoService(InvestimentoRepositoryPort investimentos, ContaRepositoryPort contas,
                                            FinanceiroCoreUseCase transacoes) {
        return new InvestimentoService(investimentos, contas, transacoes);
    }

    @Bean @Primary
    InvestimentoUseCase investimentoUseCase(InvestimentoService service, PlatformTransactionManager manager) {
        return transacional(InvestimentoUseCase.class, service, manager, null, Map.of());
    }

    @Bean
    PerfilUsuarioService perfilUsuarioService(UsuarioRepositoryPort usuarios, RefreshTokenRepositoryPort refreshTokens) {
        return new PerfilUsuarioService(usuarios, refreshTokens);
    }

    @Bean @Primary
    GerenciarPerfilUseCase gerenciarPerfilUseCase(PerfilUsuarioService service, PlatformTransactionManager manager) {
        return transacional(GerenciarPerfilUseCase.class, service, manager, null, Map.of());
    }

    @Bean
    RecorrenciaService recorrenciaService(RecorrenciaRepositoryPort recorrencias, ContaRepositoryPort contas,
                                          CategoriaRepositoryPort categorias, MeioPagamentoRepositoryPort meios,
                                          TransacaoRepositoryPort transacoes, ObterDataAtualPort dataAtual) {
        return new RecorrenciaService(recorrencias, contas, categorias, meios, transacoes, dataAtual);
    }

    @Bean @Primary
    RecorrenciaUseCase recorrenciaUseCase(RecorrenciaService service, PlatformTransactionManager manager) {
        return transacional(RecorrenciaUseCase.class, service, manager, null, Map.of());
    }

    @Bean
    FinanciamentoService financiamentoService(FinanciamentoRepositoryPort financiamentos, ContaRepositoryPort contas,
                                              TransacaoRepositoryPort transacoes, ObterDataAtualPort dataAtual) {
        return new FinanciamentoService(financiamentos, contas, transacoes, dataAtual);
    }

    @Bean @Primary
    FinanciamentoUseCase financiamentoUseCase(FinanciamentoService service, PlatformTransactionManager manager,
                                              OperacaoFinanceiraMetrics metrics) {
        return transacional(FinanciamentoUseCase.class, service, manager, metrics,
                Map.of("pagarParcela", "pagamento_parcela"));
    }

    @Bean
    PrevisaoFluxoCaixaService previsaoFluxoCaixaService(PrevisaoMensalRepositoryPort previsoes,
                                                         RecorrenciaRepositoryPort recorrencias,
                                                         TransacaoRepositoryPort transacoes,
                                                         FinanciamentoRepositoryPort financiamentos,
                                                         ObterDataAtualPort dataAtual) {
        return new PrevisaoFluxoCaixaService(previsoes, recorrencias, transacoes, financiamentos, dataAtual);
    }

    @Bean @Primary
    PrevisaoFluxoCaixaUseCase previsaoFluxoCaixaUseCase(PrevisaoFluxoCaixaService service,
                                                        PlatformTransactionManager manager) {
        return transacional(PrevisaoFluxoCaixaUseCase.class, service, manager, null, Map.of());
    }

    private <T> T transacional(Class<T> contract, T delegate, PlatformTransactionManager manager,
                               OperacaoFinanceiraMetrics metrics, Map<String, String> metricas) {
        TransactionTemplate escrita = new TransactionTemplate(manager);
        TransactionTemplate leitura = somenteLeitura(manager);
        return contract.cast(Proxy.newProxyInstance(contract.getClassLoader(), new Class<?>[]{contract}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(delegate, args);
            }
            Supplier<Object> acao = () -> invocar(method, delegate, args);
            Supplier<Object> observada = () -> {
                String operacao = metricas.get(method.getName());
                return operacao == null ? acao.get() : metrics.medir(operacao, acao);
            };
            return (consulta(method.getName()) ? leitura : escrita).execute(status -> observada.get());
        }));
    }

    private static boolean consulta(String metodo) {
        return metodo.startsWith("listar") || metodo.startsWith("buscar") || metodo.startsWith("consultar");
    }

    private static Object invocar(java.lang.reflect.Method method, Object delegate, Object[] args) {
        try {
            return method.invoke(delegate, args);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Não foi possível invocar o caso de uso", exception);
        } catch (InvocationTargetException exception) {
            Throwable causa = exception.getCause();
            if (causa instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (causa instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Falha ao executar o caso de uso", causa);
        }
    }

    private static TransactionTemplate somenteLeitura(PlatformTransactionManager manager) {
        TransactionTemplate template = new TransactionTemplate(manager);
        template.setReadOnly(true);
        return template;
    }
}
