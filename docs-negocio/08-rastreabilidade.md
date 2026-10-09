# Matriz de rastreabilidade e fontes

[Voltar ao portal](00-README.md)

## Base e limite da análise

Checkout local em 1º de outubro de 2026, branch development, HEAD d306b427389cae4ddfa1c3b517576ba70490361b, com alterações rastreadas e arquivos não rastreados preexistentes. O HEAD isolado não reproduz este estado. O inventário cobre todos os controllers encontrados e os principais caminhos de aplicação/domínio, além de persistência e testes selecionados.

Fontes: código executável, contratos de entrada, modelos de estados, consultas de consolidação, mensagens, migrations selecionadas, testes existentes e documentação técnica. Documentos históricos não prevaleceram sobre o código atual. Não foram consultados dados de usuários, credenciais ou configurações privadas.

**Não executado:** aplicação, suíte Maven, MySQL, PDFs reais de usuários, testes de concorrência ou jornadas do frontend. Teste listado é evidência de cenário escrito, não de teste aprovado nesta entrega. Não há garantia de que todo ramo da implementação tenha sido homologado.

A estrutura deste portal usa IDs próprios de FUNC/RF/PROC/JORNADA/VAL/INT/PO. RN-001 a RN-018 preservam o tema do catálogo anterior; RN-019 a RN-035 ampliam o detalhamento. Há uma definição canônica de cada ID no respectivo catálogo; repetições nesta matriz são referências.

## Jornada → funcionalidade → requisito → regra → processo → validação → integração

| Jornada | Funcionalidade | Requisito | Regras principais | Processo | Validação representativa | Integração |
|---|---|---|---|---|---|---|
| JORNADA-001 | FUNC-001 | RF-001 | RN-001 | PROC-001 | VAL-001 | INT-001 |
| JORNADA-001 | FUNC-002 | RF-002 | RN-001, RN-019, RN-026 | PROC-001 | VAL-005 | INT-001 |
| JORNADA-001 | FUNC-003 | RF-003 | RN-001, RN-006, RN-020 | PROC-001 | VAL-008 | INT-001 |
| JORNADA-001 | FUNC-004 | RF-004 | RN-001, RN-002, RN-019, RN-020 | PROC-001 | VAL-007 | INT-001 |
| JORNADA-001 | FUNC-005 | RF-005 | RN-002, RN-005, RN-021 | PROC-001 | VAL-013 | INT-001 |
| JORNADA-001 | FUNC-006 | RF-006 | RN-005, RN-020, RN-021 | PROC-001 | VAL-011 | INT-001 |
| JORNADA-001 | FUNC-007 | RF-007 | RN-003, RN-019 | PROC-001 | VAL-016 | INT-001 |
| JORNADA-001 | FUNC-008 | RF-008 | RN-004, RN-019 | PROC-001 | VAL-018 | INT-001 |
| JORNADA-001 | FUNC-009 | RF-009 | RN-010, RN-022 | PROC-002 | VAL-019 | INT-001 |
| JORNADA-001 | FUNC-010 | RF-010 | RN-007, RN-023 | PROC-002 | VAL-021 | INT-001, INT-003 |
| JORNADA-002 | FUNC-011 | RF-011 | RN-008, RN-024 | PROC-003 | VAL-023 | INT-001 |
| JORNADA-002 | FUNC-012 | RF-012 | RN-009, RN-024 | PROC-003 | VAL-025 | INT-001 |
| JORNADA-002 | FUNC-013 | RF-013 | RN-008, RN-025 | PROC-003 | VAL-027 | INT-001 |
| JORNADA-005 | FUNC-014 | RF-014 | RN-011, RN-027 | PROC-004 | VAL-028 | INT-001 |
| JORNADA-005 | FUNC-015 | RF-015 | RN-011, RN-027 | PROC-004 | VAL-029 | INT-001 |
| JORNADA-006 | FUNC-016 | RF-016 | RN-012, RN-026 | PROC-005 | VAL-030 | INT-001 |
| JORNADA-006 | FUNC-017 | RF-017 | RN-012, RN-026 | PROC-005 | VAL-031 | INT-001 |
| JORNADA-004 | FUNC-018 | RF-018 | RN-028 | PROC-006 | VAL-032 | INT-001 |
| JORNADA-004 | FUNC-019 | RF-019 | RN-028, RN-029 | PROC-006 | VAL-033 | INT-001 |
| JORNADA-004 | FUNC-020 | RF-020 | RN-013, RN-014, RN-030 | PROC-006 | VAL-034 | INT-001 |
| JORNADA-004 | FUNC-021 | RF-021 | RN-014, RN-029, RN-031 | PROC-006 | VAL-036 | INT-001 |
| JORNADA-003 | FUNC-022 | RF-022 | RN-015, RN-016, RN-032 | PROC-007 | VAL-038 | INT-001, INT-002 |
| JORNADA-001 | FUNC-023 | RF-023 | RN-002, RN-012, RN-033 | PROC-008 | VAL-043 | INT-001 |
| JORNADA-001 | FUNC-024 | RF-024 | RN-034 | PROC-008 | VAL-044 | INT-001 |
| JORNADA-001 | FUNC-025 | RF-025 | RN-017, RN-018, RN-035 | PROC-009 | VAL-045 | INT-001 |

## Funcionalidade → fonte executável e cenário escrito

Cada linha é a evidência EV de mesmo número da funcionalidade. Links levam à fonte; os métodos de interesse são os fluxos públicos descritos no catálogo. A lista complementar abaixo cobre referências relacionadas que não cabem numa única fonte principal.

| Evidência / funcionalidade | Regras | Serviço principal | Contrato de operação | Teste existente relacionado |
|---|---|---|---|---|
| EV-001 / FUNC-001 | RN-001 | [AutenticacaoService](../src/main/java/com/finisus/application/service/AutenticacaoService.java) | [AuthController](../src/main/java/com/finisus/adapters/in/web/AuthController.java) | [PerfilUsuarioServiceTest](../src/test/java/com/finisus/application/service/PerfilUsuarioServiceTest.java) |
| EV-002 / FUNC-002 | RN-001, RN-019, RN-026 | [ContaService](../src/main/java/com/finisus/application/service/ContaService.java) | [ContaController](../src/main/java/com/finisus/adapters/in/web/ContaController.java) | [BancoServiceTest](../src/test/java/com/finisus/application/service/BancoServiceTest.java) |
| EV-003 / FUNC-003 | RN-001, RN-006, RN-020 | [CategoriaService](../src/main/java/com/finisus/application/service/CategoriaService.java) | [CategoriaController](../src/main/java/com/finisus/adapters/in/web/CategoriaController.java) | [CategoriaServiceTest](../src/test/java/com/finisus/application/service/CategoriaServiceTest.java) |
| EV-004 / FUNC-004 | RN-001, RN-002, RN-019, RN-020 | [TransacaoService](../src/main/java/com/finisus/application/service/TransacaoService.java) | [TransacaoController](../src/main/java/com/finisus/adapters/in/web/TransacaoController.java) | [TransacaoServiceTest](../src/test/java/com/finisus/application/service/TransacaoServiceTest.java) |
| EV-005 / FUNC-005 | RN-002, RN-005, RN-021 | [TransacaoService](../src/main/java/com/finisus/application/service/TransacaoService.java) | [TransacaoController](../src/main/java/com/finisus/adapters/in/web/TransacaoController.java) | [TransacaoServiceTest](../src/test/java/com/finisus/application/service/TransacaoServiceTest.java) |
| EV-006 / FUNC-006 | RN-005, RN-020, RN-021 | [TransacaoService](../src/main/java/com/finisus/application/service/TransacaoService.java) | [TransacaoController](../src/main/java/com/finisus/adapters/in/web/TransacaoController.java) | [TransacaoServiceTest](../src/test/java/com/finisus/application/service/TransacaoServiceTest.java) |
| EV-007 / FUNC-007 | RN-003, RN-019 | [TransferenciaContaService](../src/main/java/com/finisus/application/service/TransferenciaContaService.java) | [TransferenciaContaController](../src/main/java/com/finisus/adapters/in/web/TransferenciaContaController.java) | [TransferenciaContaServiceTest](../src/test/java/com/finisus/application/service/TransferenciaContaServiceTest.java) |
| EV-008 / FUNC-008 | RN-004, RN-019 | [AjusteSaldoContaService](../src/main/java/com/finisus/application/service/AjusteSaldoContaService.java) | [ContaController](../src/main/java/com/finisus/adapters/in/web/ContaController.java) | [ReconciliacaoSaldoContaIntegrationTest](../src/test/java/com/finisus/ReconciliacaoSaldoContaIntegrationTest.java) |
| EV-009 / FUNC-009 | RN-010, RN-022 | [ObrigacaoFinanceiraService](../src/main/java/com/finisus/application/service/ObrigacaoFinanceiraService.java) | [ObrigacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ObrigacaoFinanceiraController.java) | [ObrigacaoFinanceiraServiceTest](../src/test/java/com/finisus/application/service/ObrigacaoFinanceiraServiceTest.java) |
| EV-010 / FUNC-010 | RN-007, RN-023 | [RecorrenciaService](../src/main/java/com/finisus/application/service/RecorrenciaService.java) | [RecorrenciaController](../src/main/java/com/finisus/adapters/in/web/RecorrenciaController.java) | [RecorrenciaServiceTest](../src/test/java/com/finisus/application/service/RecorrenciaServiceTest.java) |
| EV-011 / FUNC-011 | RN-008, RN-024 | [FaturaService](../src/main/java/com/finisus/application/service/FaturaService.java) | [FaturaController](../src/main/java/com/finisus/adapters/in/web/FaturaController.java) | [FaturaServiceTest](../src/test/java/com/finisus/application/service/FaturaServiceTest.java) |
| EV-012 / FUNC-012 | RN-009, RN-024 | [FaturaService](../src/main/java/com/finisus/application/service/FaturaService.java) | [FaturaController](../src/main/java/com/finisus/adapters/in/web/FaturaController.java) | [FaturaServiceTest](../src/test/java/com/finisus/application/service/FaturaServiceTest.java) |
| EV-013 / FUNC-013 | RN-008, RN-025 | [CompraParceladaService](../src/main/java/com/finisus/application/service/CompraParceladaService.java) | [CompraParceladaController](../src/main/java/com/finisus/adapters/in/web/CompraParceladaController.java) | [CompraParceladaServiceTest](../src/test/java/com/finisus/application/service/CompraParceladaServiceTest.java) |
| EV-014 / FUNC-014 | RN-011, RN-027 | [ParcelaFinanciamentoService](../src/main/java/com/finisus/application/service/ParcelaFinanciamentoService.java) | [ParcelaFinanciamentoController](../src/main/java/com/finisus/adapters/in/web/ParcelaFinanciamentoController.java) | [ParcelaFinanciamentoServiceTest](../src/test/java/com/finisus/application/service/ParcelaFinanciamentoServiceTest.java) |
| EV-015 / FUNC-015 | RN-011, RN-027 | [FinanciamentoService](../src/main/java/com/finisus/application/service/FinanciamentoService.java) | [FinanciamentoController](../src/main/java/com/finisus/adapters/in/web/FinanciamentoController.java) | [FinanciamentoServiceTest](../src/test/java/com/finisus/application/service/FinanciamentoServiceTest.java) |
| EV-016 / FUNC-016 | RN-012, RN-026 | [MovimentoInvestimentoService](../src/main/java/com/finisus/application/service/MovimentoInvestimentoService.java) | [MovimentoInvestimentoController](../src/main/java/com/finisus/adapters/in/web/MovimentoInvestimentoController.java) | [MovimentoInvestimentoServiceTest](../src/test/java/com/finisus/application/service/MovimentoInvestimentoServiceTest.java) |
| EV-017 / FUNC-017 | RN-012, RN-026 | [PosicaoInvestimentoService](../src/main/java/com/finisus/application/service/PosicaoInvestimentoService.java) | [PosicaoInvestimentoController](../src/main/java/com/finisus/adapters/in/web/PosicaoInvestimentoController.java) | [PainelFinanceiroServiceTest](../src/test/java/com/finisus/application/service/PainelFinanceiroServiceTest.java) |
| EV-018 / FUNC-018 | RN-028 | [DespesaCompartilhadaService](../src/main/java/com/finisus/application/service/DespesaCompartilhadaService.java) | [DespesaCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DespesaCompartilhadaController.java) | [DespesaCompartilhadaServiceTest](../src/test/java/com/finisus/application/service/DespesaCompartilhadaServiceTest.java) |
| EV-019 / FUNC-019 | RN-028, RN-029 | [RateioDespesaService](../src/main/java/com/finisus/application/service/RateioDespesaService.java) | [RateioDespesaController](../src/main/java/com/finisus/adapters/in/web/RateioDespesaController.java) | [RateioDespesaServiceTest](../src/test/java/com/finisus/application/service/RateioDespesaServiceTest.java) |
| EV-020 / FUNC-020 | RN-013, RN-014, RN-030 | [DivisaoCompartilhadaService](../src/main/java/com/finisus/application/service/DivisaoCompartilhadaService.java) | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java) | [DivisaoCompartilhadaServiceTest](../src/test/java/com/finisus/application/service/DivisaoCompartilhadaServiceTest.java) |
| EV-021 / FUNC-021 | RN-014, RN-029, RN-031 | [DivisaoCompartilhadaService](../src/main/java/com/finisus/application/service/DivisaoCompartilhadaService.java) | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java) | [DivisaoCompartilhadaServiceTest](../src/test/java/com/finisus/application/service/DivisaoCompartilhadaServiceTest.java) |
| EV-022 / FUNC-022 | RN-015, RN-016, RN-032 | [ImportacaoFinanceiraService](../src/main/java/com/finisus/application/service/ImportacaoFinanceiraService.java) | [ImportacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ImportacaoFinanceiraController.java) | [ImportacaoFinanceiraServiceTest](../src/test/java/com/finisus/application/service/ImportacaoFinanceiraServiceTest.java) |
| EV-023 / FUNC-023 | RN-002, RN-012, RN-033 | [PainelFinanceiroService](../src/main/java/com/finisus/application/service/PainelFinanceiroService.java) | [PainelFinanceiroController](../src/main/java/com/finisus/adapters/in/web/PainelFinanceiroController.java) | [PainelFinanceiroServiceTest](../src/test/java/com/finisus/application/service/PainelFinanceiroServiceTest.java) |
| EV-024 / FUNC-024 | RN-034 | [PrevisaoFluxoCaixaService](../src/main/java/com/finisus/application/service/PrevisaoFluxoCaixaService.java) | [PrevisaoFluxoCaixaController](../src/main/java/com/finisus/adapters/in/web/PrevisaoFluxoCaixaController.java) | Não localizado teste dedicado neste levantamento; não presumir cobertura. |
| EV-025 / FUNC-025 | RN-017, RN-018, RN-035 | [PerfilUsuarioService](../src/main/java/com/finisus/application/service/PerfilUsuarioService.java) | [PerfilUsuarioController](../src/main/java/com/finisus/adapters/in/web/PerfilUsuarioController.java) | [PerfilUsuarioServiceTest](../src/test/java/com/finisus/application/service/PerfilUsuarioServiceTest.java) |

## Fontes complementares

| Assunto | Fontes que sustentam os detalhes |
|---|---|
| Bancos, referências e tipos | [BancoService](../src/main/java/com/finisus/application/service/BancoService.java), [MeioPagamentoService](../src/main/java/com/finisus/application/service/MeioPagamentoService.java), [ItemService](../src/main/java/com/finisus/application/service/ItemService.java), [Conta](../src/main/java/com/finisus/domain/model/Conta.java), [TipoConta](../src/main/java/com/finisus/domain/model/TipoConta.java). |
| Dinheiro, itens e restrições de origem | [ValorMonetario](../src/main/java/com/finisus/domain/vo/ValorMonetario.java), [Transacao](../src/main/java/com/finisus/domain/model/Transacao.java), [TransacaoItem](../src/main/java/com/finisus/domain/model/TransacaoItem.java), [histórico](../src/main/java/com/finisus/application/service/ConsultarHistoricoTransacaoService.java). |
| Reconciliação | [ReconciliacaoSaldoContaService](../src/main/java/com/finisus/application/service/ReconciliacaoSaldoContaService.java). |
| Cartão e fatura | [CartaoCreditoService](../src/main/java/com/finisus/application/service/CartaoCreditoService.java), [Fatura](../src/main/java/com/finisus/domain/model/Fatura.java), [pagamento](../src/main/java/com/finisus/domain/model/PagamentoFatura.java). |
| Obrigação e saldo pendente | [ObrigacaoFinanceira](../src/main/java/com/finisus/domain/model/ObrigacaoFinanceira.java), [persistência](../src/main/java/com/finisus/adapters/out/persistence/ObrigacaoFinanceiraPersistenceAdapter.java). |
| Investimento e custódia | [InvestimentoService](../src/main/java/com/finisus/application/service/InvestimentoService.java), [teste](../src/test/java/com/finisus/application/service/InvestimentoServiceTest.java). |
| Compartilhamento | [DivisaoCompartilhada](../src/main/java/com/finisus/domain/model/DivisaoCompartilhada.java), [RateioDespesa](../src/main/java/com/finisus/domain/model/RateioDespesa.java), [configuração](../src/main/java/com/finisus/application/service/ConfiguracaoCompartilhamentoService.java). |
| Estados da importação | [ImportacaoFinanceira](../src/main/java/com/finisus/domain/model/ImportacaoFinanceira.java), [LancamentoImportado](../src/main/java/com/finisus/domain/model/LancamentoImportado.java). |
| PDF e limites | [LeitorDocumentoFinanceiroPdfAdapter](../src/main/java/com/finisus/adapters/out/pdf/LeitorDocumentoFinanceiroPdfAdapter.java), [leitores](../src/main/java/com/finisus/adapters/out/pdf). |
| Indicadores | [DashboardFinanceiroService](../src/main/java/com/finisus/application/service/DashboardFinanceiroService.java), [consultas de consolidação](../src/main/java/com/finisus/adapters/out/persistence/DashboardFinanceiroPersistenceAdapter.java), [consulta de transações](../src/main/java/com/finisus/adapters/out/persistence/TransacaoPersistenceAdapter.java), [teste de dashboard](../src/test/java/com/finisus/application/service/DashboardFinanceiroServiceTest.java). |
| Segurança e erros | [SecurityConfig](../src/main/java/com/finisus/infrastructure/config/SecurityConfig.java), [sessão](../src/main/java/com/finisus/infrastructure/config/UsuarioSessaoJwtValidator.java), [erros](../src/main/java/com/finisus/infrastructure/config/ApiExceptionHandler.java), [mensagens](../src/main/resources/messages.properties). |
| Agendamento | [RecorrenciaScheduler](../src/main/java/com/finisus/infrastructure/scheduling/RecorrenciaScheduler.java), [observabilidade](../src/main/java/com/finisus/infrastructure/observability). |
| Exportação e privacidade | [DadosPessoaisJdbcAdapter](../src/main/java/com/finisus/adapters/out/persistence/DadosPessoaisJdbcAdapter.java), [SolicitacaoPrivacidade](../src/main/java/com/finisus/domain/model/SolicitacaoPrivacidade.java), [PrivacidadeIntegrationTest](../src/test/java/com/finisus/PrivacidadeIntegrationTest.java). |
| Persistência histórica | [V53 — alocações](../src/main/resources/db/migration/V53__criar_alocacoes_pagamento_divisao.sql), [V60 — revisões de importação](../src/main/resources/db/migration/V60__auditar_revisoes_importacao.sql), [V65 — solicitações de privacidade](../src/main/resources/db/migration/V65__criar_solicitacoes_privacidade.sql). Inspeção de scripts não é execução de migração. |
| Contexto técnico | [README técnico](../README.md), [regras anteriores](../docs/regras-negocio.md), [autenticação](../docs/autenticacao.md), [privacidade](../docs/privacidade-ciclo-vida.md), [testes](../docs/testes.md). |

## Localização dos pontos para PO

PO-001 a PO-004: EV-009, EV-010, EV-013, EV-023 e EV-024, mais fontes de consolidação/persistência.
PO-005: valor monetário e EV-004.
PO-006: EV-018 a EV-020.
PO-007: EV-005 e EV-010.
PO-008: EV-013.
PO-009: EV-014/EV-015.
PO-010: EV-005/EV-020.
PO-011: EV-021.
PO-012: EV-025.
PO-013: contexto de produto e limite do levantamento.
PO-014: documentos anteriores comparados a EV-006/EV-012/EV-019.

## Inventário de operações HTTP

### Revisão documental realizada

- Conferidas as definições únicas e referências de 25 funcionalidades, 25 requisitos funcionais, 10 requisitos não funcionais, 35 regras, 45 validações, 34 cenários, 6 jornadas, 9 processos, 3 integrações e 14 pendências.
- Verificados 352 links locais do portal, a inclusão dos nove documentos detalhados no índice e a cobertura das 25 funcionalidades na matriz.
- Conferidas 150 operações distintas e suas linhas de origem nos 27 controllers.
- Verificada a estrutura das cercas Markdown; o diagrama Mermaid foi revisado textualmente, sem renderização gráfica nesta entrega.
- Revisados uso de ponta a ponta, objetivo principal/final, participantes, pré-condições, exceções, estados e resultados de conclusão. Inferências e inconsistências foram separadas das confirmações estáticas.
- Comparados hashes dos arquivos preexistentes: somente README.md foi alterado para acrescentar o link do portal. Os arquivos de aplicação, testes, migrations e demais documentos preexistentes foram preservados.
- Verificação de espaços pelo Git não apontou erros no README; os arquivos novos também passaram pela verificação documental de estrutura, referências e codificação. Não houve execução da suíte da aplicação ou homologação em banco/interface.

### Operações inventariadas

Inventário extraído das anotações dos controllers do checkout. É um apêndice de rastreabilidade, não um manual de uso: campos, objetivos e regras estão nos catálogos funcionais. Operação exposta pode ser bloqueada pelo serviço, como a marcação de pagamento do rateio. Contagem e linhas abaixo são geradas a partir das fontes atuais.


**Cobertura do inventário:** 27 controllers e 150 operações HTTP.

| Método | Operação | Funcionalidade | Fonte e linha da anotação |
|---|---|---|---|
| POST | `/api/v1/auth/cadastro` | FUNC-001 | [AuthController](../src/main/java/com/finisus/adapters/in/web/AuthController.java), linha 39 |
| POST | `/api/v1/auth/login` | FUNC-001 | [AuthController](../src/main/java/com/finisus/adapters/in/web/AuthController.java), linha 47 |
| POST | `/api/v1/auth/refresh` | FUNC-001 | [AuthController](../src/main/java/com/finisus/adapters/in/web/AuthController.java), linha 52 |
| GET | `/api/v1/bancos` | FUNC-002 | [BancoController](../src/main/java/com/finisus/adapters/in/web/BancoController.java), linha 43 |
| POST | `/api/v1/bancos` | FUNC-002 | [BancoController](../src/main/java/com/finisus/adapters/in/web/BancoController.java), linha 50 |
| GET | `/api/v1/bancos/{bancoId}` | FUNC-002 | [BancoController](../src/main/java/com/finisus/adapters/in/web/BancoController.java), linha 56 |
| PATCH | `/api/v1/bancos/{bancoId}` | FUNC-002 | [BancoController](../src/main/java/com/finisus/adapters/in/web/BancoController.java), linha 61 |
| DELETE | `/api/v1/bancos/{bancoId}` | FUNC-002 | [BancoController](../src/main/java/com/finisus/adapters/in/web/BancoController.java), linha 68 |
| GET | `/api/v1/cartoes` | FUNC-011 | [CartaoCreditoController](../src/main/java/com/finisus/adapters/in/web/CartaoCreditoController.java), linha 47 |
| POST | `/api/v1/cartoes` | FUNC-011 | [CartaoCreditoController](../src/main/java/com/finisus/adapters/in/web/CartaoCreditoController.java), linha 55 |
| GET | `/api/v1/cartoes/{cartaoId}` | FUNC-011 | [CartaoCreditoController](../src/main/java/com/finisus/adapters/in/web/CartaoCreditoController.java), linha 62 |
| PATCH | `/api/v1/cartoes/{cartaoId}` | FUNC-011 | [CartaoCreditoController](../src/main/java/com/finisus/adapters/in/web/CartaoCreditoController.java), linha 67 |
| DELETE | `/api/v1/cartoes/{cartaoId}` | FUNC-011 | [CartaoCreditoController](../src/main/java/com/finisus/adapters/in/web/CartaoCreditoController.java), linha 74 |
| GET | `/api/v1/categorias` | FUNC-003 | [CategoriaController](../src/main/java/com/finisus/adapters/in/web/CategoriaController.java), linha 43 |
| POST | `/api/v1/categorias` | FUNC-003 | [CategoriaController](../src/main/java/com/finisus/adapters/in/web/CategoriaController.java), linha 50 |
| GET | `/api/v1/categorias/{categoriaId}` | FUNC-003 | [CategoriaController](../src/main/java/com/finisus/adapters/in/web/CategoriaController.java), linha 57 |
| PATCH | `/api/v1/categorias/{categoriaId}` | FUNC-003 | [CategoriaController](../src/main/java/com/finisus/adapters/in/web/CategoriaController.java), linha 62 |
| DELETE | `/api/v1/categorias/{categoriaId}` | FUNC-003 | [CategoriaController](../src/main/java/com/finisus/adapters/in/web/CategoriaController.java), linha 69 |
| GET | `/api/v1/compras-parceladas` | FUNC-013 | [CompraParceladaController](../src/main/java/com/finisus/adapters/in/web/CompraParceladaController.java), linha 46 |
| POST | `/api/v1/compras-parceladas` | FUNC-013 | [CompraParceladaController](../src/main/java/com/finisus/adapters/in/web/CompraParceladaController.java), linha 53 |
| GET | `/api/v1/compras-parceladas/{compraId}` | FUNC-013 | [CompraParceladaController](../src/main/java/com/finisus/adapters/in/web/CompraParceladaController.java), linha 60 |
| POST | `/api/v1/compras-parceladas/{compraId}/cancelar` | FUNC-013 | [CompraParceladaController](../src/main/java/com/finisus/adapters/in/web/CompraParceladaController.java), linha 65 |
| GET | `/api/v1/compartilhamentos/opt-in` | FUNC-019 | [ConfiguracaoCompartilhamentoController](../src/main/java/com/finisus/adapters/in/web/ConfiguracaoCompartilhamentoController.java), linha 25 |
| PUT | `/api/v1/compartilhamentos/opt-in` | FUNC-019 | [ConfiguracaoCompartilhamentoController](../src/main/java/com/finisus/adapters/in/web/ConfiguracaoCompartilhamentoController.java), linha 30 |
| GET | `/api/v1/contas` | FUNC-002 | [ContaController](../src/main/java/com/finisus/adapters/in/web/ContaController.java), linha 56 |
| POST | `/api/v1/contas` | FUNC-002 | [ContaController](../src/main/java/com/finisus/adapters/in/web/ContaController.java), linha 63 |
| GET | `/api/v1/contas/{contaId}` | FUNC-002 | [ContaController](../src/main/java/com/finisus/adapters/in/web/ContaController.java), linha 70 |
| GET | `/api/v1/contas/{contaId}/reconciliacao` | FUNC-008 | [ContaController](../src/main/java/com/finisus/adapters/in/web/ContaController.java), linha 75 |
| POST | `/api/v1/contas/{contaId}/ajustes-saldo` | FUNC-008 | [ContaController](../src/main/java/com/finisus/adapters/in/web/ContaController.java), linha 80 |
| GET | `/api/v1/contas/{contaId}/ajustes-saldo` | FUNC-008 | [ContaController](../src/main/java/com/finisus/adapters/in/web/ContaController.java), linha 90 |
| PATCH | `/api/v1/contas/{contaId}` | FUNC-002 | [ContaController](../src/main/java/com/finisus/adapters/in/web/ContaController.java), linha 99 |
| DELETE | `/api/v1/contas/{contaId}` | FUNC-002 | [ContaController](../src/main/java/com/finisus/adapters/in/web/ContaController.java), linha 106 |
| GET | `/api/v1/dashboard/mensal` | FUNC-023 | [DashboardFinanceiroController](../src/main/java/com/finisus/adapters/in/web/DashboardFinanceiroController.java), linha 34 |
| GET | `/api/v1/dashboard/resumo` | FUNC-023 | [DashboardFinanceiroController](../src/main/java/com/finisus/adapters/in/web/DashboardFinanceiroController.java), linha 40 |
| GET | `/api/v1/dashboard/anual` | FUNC-023 | [DashboardFinanceiroController](../src/main/java/com/finisus/adapters/in/web/DashboardFinanceiroController.java), linha 47 |
| GET | `/api/v1/dashboard/anual/composicao` | FUNC-023 | [DashboardFinanceiroController](../src/main/java/com/finisus/adapters/in/web/DashboardFinanceiroController.java), linha 53 |
| GET | `/api/v1/dashboard/balancete` | FUNC-023 | [DashboardFinanceiroController](../src/main/java/com/finisus/adapters/in/web/DashboardFinanceiroController.java), linha 59 |
| GET | `/api/v1/dashboard/balancete/anual` | FUNC-023 | [DashboardFinanceiroController](../src/main/java/com/finisus/adapters/in/web/DashboardFinanceiroController.java), linha 72 |
| GET | `/api/v1/compartilhamentos` | FUNC-018 | [DespesaCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DespesaCompartilhadaController.java), linha 40 |
| POST | `/api/v1/compartilhamentos` | FUNC-018 | [DespesaCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DespesaCompartilhadaController.java), linha 48 |
| GET | `/api/v1/compartilhamentos/{despesaId}` | FUNC-018 | [DespesaCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DespesaCompartilhadaController.java), linha 62 |
| POST | `/api/v1/compartilhamentos/{despesaId}/cancelar` | FUNC-018 | [DespesaCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DespesaCompartilhadaController.java), linha 69 |
| POST | `/api/v1/compartilhamentos/{despesaId}/migrar-divisao` | FUNC-021 | [DespesaCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DespesaCompartilhadaController.java), linha 76 |
| GET | `/api/v1/divisoes-compartilhadas` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 62 |
| POST | `/api/v1/divisoes-compartilhadas` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 69 |
| GET | `/api/v1/divisoes-compartilhadas/{divisaoId}` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 76 |
| GET | `/api/v1/divisoes-compartilhadas/{divisaoId}/participantes/historico` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 81 |
| PATCH | `/api/v1/divisoes-compartilhadas/{divisaoId}/participantes` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 90 |
| DELETE | `/api/v1/divisoes-compartilhadas/{divisaoId}` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 97 |
| POST | `/api/v1/divisoes-compartilhadas/{divisaoId}/transacoes` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 103 |
| DELETE | `/api/v1/divisoes-compartilhadas/{divisaoId}/transacoes/{transacaoId}` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 114 |
| GET | `/api/v1/divisoes-compartilhadas/{divisaoId}/transacoes/{transacaoId}/alocacoes` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 121 |
| PUT | `/api/v1/divisoes-compartilhadas/{divisaoId}/transacoes/{transacaoId}/alocacoes` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 130 |
| DELETE | `/api/v1/divisoes-compartilhadas/{divisaoId}/transacoes/{transacaoId}/alocacoes/{alocacaoId}` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 141 |
| POST | `/api/v1/divisoes-compartilhadas/{divisaoId}/reembolsos` | FUNC-021 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 148 |
| GET | `/api/v1/divisoes-compartilhadas/{divisaoId}/reembolsos` | FUNC-021 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 156 |
| DELETE | `/api/v1/divisoes-compartilhadas/{divisaoId}/reembolsos/{reembolsoId}` | FUNC-021 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 162 |
| GET | `/api/v1/divisoes-compartilhadas/{divisaoId}/transacoes/{transacaoId}/pagamento` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 169 |
| GET | `/api/v1/divisoes-compartilhadas/{divisaoId}/transacoes/pendentes-revisao` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 179 |
| PUT | `/api/v1/divisoes-compartilhadas/{divisaoId}/transacoes/{transacaoId}/responsabilidades` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 185 |
| GET | `/api/v1/divisoes-compartilhadas/{divisaoId}/resumo` | FUNC-020 | [DivisaoCompartilhadaController](../src/main/java/com/finisus/adapters/in/web/DivisaoCompartilhadaController.java), linha 194 |
| GET | `/api/v1/cartoes/{cartaoId}/faturas` | FUNC-011 | [FaturaController](../src/main/java/com/finisus/adapters/in/web/FaturaController.java), linha 52 |
| POST | `/api/v1/cartoes/faturas` | FUNC-011 | [FaturaController](../src/main/java/com/finisus/adapters/in/web/FaturaController.java), linha 60 |
| GET | `/api/v1/cartoes/faturas/{faturaId}` | FUNC-011 | [FaturaController](../src/main/java/com/finisus/adapters/in/web/FaturaController.java), linha 67 |
| POST | `/api/v1/cartoes/faturas/{faturaId}/gastos` | FUNC-011 | [FaturaController](../src/main/java/com/finisus/adapters/in/web/FaturaController.java), linha 72 |
| POST | `/api/v1/cartoes/faturas/{faturaId}/fechar` | FUNC-012 | [FaturaController](../src/main/java/com/finisus/adapters/in/web/FaturaController.java), linha 84 |
| POST | `/api/v1/cartoes/faturas/processar-ciclos` | FUNC-012 | [FaturaController](../src/main/java/com/finisus/adapters/in/web/FaturaController.java), linha 89 |
| POST | `/api/v1/cartoes/faturas/{faturaId}/pagar` | FUNC-012 | [FaturaController](../src/main/java/com/finisus/adapters/in/web/FaturaController.java), linha 94 |
| POST | `/api/v1/cartoes/faturas/{faturaId}/estornar-pagamento` | FUNC-012 | [FaturaController](../src/main/java/com/finisus/adapters/in/web/FaturaController.java), linha 102 |
| PATCH | `/api/v1/cartoes/faturas/{faturaId}` | FUNC-011 | [FaturaController](../src/main/java/com/finisus/adapters/in/web/FaturaController.java), linha 107 |
| POST | `/api/v1/cartoes/faturas/{faturaId}/cancelar` | FUNC-011 | [FaturaController](../src/main/java/com/finisus/adapters/in/web/FaturaController.java), linha 114 |
| GET | `/api/v1/financiamentos` | FUNC-014 | [FinanciamentoController](../src/main/java/com/finisus/adapters/in/web/FinanciamentoController.java), linha 45 |
| GET | `/api/v1/financiamentos/{financiamentoId}` | FUNC-014 | [FinanciamentoController](../src/main/java/com/finisus/adapters/in/web/FinanciamentoController.java), linha 52 |
| GET | `/api/v1/financiamentos/{financiamentoId}/cronogramas/{versao}` | FUNC-014 | [FinanciamentoController](../src/main/java/com/finisus/adapters/in/web/FinanciamentoController.java), linha 57 |
| POST | `/api/v1/financiamentos` | FUNC-014 | [FinanciamentoController](../src/main/java/com/finisus/adapters/in/web/FinanciamentoController.java), linha 64 |
| POST | `/api/v1/financiamentos/{financiamentoId}/cancelar` | FUNC-015 | [FinanciamentoController](../src/main/java/com/finisus/adapters/in/web/FinanciamentoController.java), linha 72 |
| POST | `/api/v1/financiamentos/{financiamentoId}/refinanciar` | FUNC-015 | [FinanciamentoController](../src/main/java/com/finisus/adapters/in/web/FinanciamentoController.java), linha 77 |
| POST | `/api/v1/financiamentos/{financiamentoId}/amortizar` | FUNC-015 | [FinanciamentoController](../src/main/java/com/finisus/adapters/in/web/FinanciamentoController.java), linha 90 |
| POST | `/api/v1/importacoes-financeiras` | FUNC-022 | [ImportacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ImportacaoFinanceiraController.java), linha 48 |
| GET | `/api/v1/importacoes-financeiras/{importacaoId}` | FUNC-022 | [ImportacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ImportacaoFinanceiraController.java), linha 64 |
| PUT | `/api/v1/importacoes-financeiras/{importacaoId}/revisao` | FUNC-022 | [ImportacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ImportacaoFinanceiraController.java), linha 69 |
| POST | `/api/v1/importacoes-financeiras/{importacaoId}/confirmar` | FUNC-022 | [ImportacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ImportacaoFinanceiraController.java), linha 77 |
| GET | `/api/v1/investimentos` | FUNC-016 | [InvestimentoController](../src/main/java/com/finisus/adapters/in/web/InvestimentoController.java), linha 45 |
| POST | `/api/v1/investimentos` | FUNC-016 | [InvestimentoController](../src/main/java/com/finisus/adapters/in/web/InvestimentoController.java), linha 52 |
| GET | `/api/v1/investimentos/{investimentoId}` | FUNC-016 | [InvestimentoController](../src/main/java/com/finisus/adapters/in/web/InvestimentoController.java), linha 60 |
| PATCH | `/api/v1/investimentos/{investimentoId}` | FUNC-016 | [InvestimentoController](../src/main/java/com/finisus/adapters/in/web/InvestimentoController.java), linha 65 |
| DELETE | `/api/v1/investimentos/{investimentoId}` | FUNC-016 | [InvestimentoController](../src/main/java/com/finisus/adapters/in/web/InvestimentoController.java), linha 73 |
| GET | `/api/v1/itens` | FUNC-003 | [ItemController](../src/main/java/com/finisus/adapters/in/web/ItemController.java), linha 41 |
| POST | `/api/v1/itens` | FUNC-003 | [ItemController](../src/main/java/com/finisus/adapters/in/web/ItemController.java), linha 48 |
| GET | `/api/v1/itens/{itemId}` | FUNC-003 | [ItemController](../src/main/java/com/finisus/adapters/in/web/ItemController.java), linha 54 |
| PATCH | `/api/v1/itens/{itemId}` | FUNC-003 | [ItemController](../src/main/java/com/finisus/adapters/in/web/ItemController.java), linha 59 |
| DELETE | `/api/v1/itens/{itemId}` | FUNC-003 | [ItemController](../src/main/java/com/finisus/adapters/in/web/ItemController.java), linha 65 |
| GET | `/api/v1/meios-pagamento` | FUNC-003 | [MeioPagamentoController](../src/main/java/com/finisus/adapters/in/web/MeioPagamentoController.java), linha 43 |
| POST | `/api/v1/meios-pagamento` | FUNC-003 | [MeioPagamentoController](../src/main/java/com/finisus/adapters/in/web/MeioPagamentoController.java), linha 50 |
| GET | `/api/v1/meios-pagamento/{meioPagamentoId}` | FUNC-003 | [MeioPagamentoController](../src/main/java/com/finisus/adapters/in/web/MeioPagamentoController.java), linha 56 |
| PATCH | `/api/v1/meios-pagamento/{meioPagamentoId}` | FUNC-003 | [MeioPagamentoController](../src/main/java/com/finisus/adapters/in/web/MeioPagamentoController.java), linha 61 |
| DELETE | `/api/v1/meios-pagamento/{meioPagamentoId}` | FUNC-003 | [MeioPagamentoController](../src/main/java/com/finisus/adapters/in/web/MeioPagamentoController.java), linha 68 |
| GET | `/api/v1/investimentos/{investimentoId}/movimentos` | FUNC-016 | [MovimentoInvestimentoController](../src/main/java/com/finisus/adapters/in/web/MovimentoInvestimentoController.java), linha 45 |
| POST | `/api/v1/investimentos/movimentos` | FUNC-016 | [MovimentoInvestimentoController](../src/main/java/com/finisus/adapters/in/web/MovimentoInvestimentoController.java), linha 53 |
| POST | `/api/v1/investimentos/movimentos/{movimentoId}/estornar` | FUNC-016 | [MovimentoInvestimentoController](../src/main/java/com/finisus/adapters/in/web/MovimentoInvestimentoController.java), linha 60 |
| GET | `/api/v1/obrigacoes-financeiras` | FUNC-009 | [ObrigacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ObrigacaoFinanceiraController.java), linha 43 |
| POST | `/api/v1/obrigacoes-financeiras` | FUNC-009 | [ObrigacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ObrigacaoFinanceiraController.java), linha 53 |
| GET | `/api/v1/obrigacoes-financeiras/{obrigacaoId}` | FUNC-009 | [ObrigacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ObrigacaoFinanceiraController.java), linha 61 |
| POST | `/api/v1/obrigacoes-financeiras/{obrigacaoId}/pagar` | FUNC-009 | [ObrigacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ObrigacaoFinanceiraController.java), linha 66 |
| GET | `/api/v1/obrigacoes-financeiras/{obrigacaoId}/pagamentos` | FUNC-009 | [ObrigacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ObrigacaoFinanceiraController.java), linha 73 |
| POST | `/api/v1/obrigacoes-financeiras/{obrigacaoId}/pagamentos/{pagamentoId}/estornar` | FUNC-009 | [ObrigacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ObrigacaoFinanceiraController.java), linha 79 |
| POST | `/api/v1/obrigacoes-financeiras/{obrigacaoId}/estornar-pagamento` | FUNC-009 | [ObrigacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ObrigacaoFinanceiraController.java), linha 85 |
| POST | `/api/v1/obrigacoes-financeiras/{obrigacaoId}/cancelar` | FUNC-009 | [ObrigacaoFinanceiraController](../src/main/java/com/finisus/adapters/in/web/ObrigacaoFinanceiraController.java), linha 90 |
| GET | `/api/v1/dashboard/visao-geral` | FUNC-023 | [PainelFinanceiroController](../src/main/java/com/finisus/adapters/in/web/PainelFinanceiroController.java), linha 32 |
| GET | `/api/v1/dashboard/agenda` | FUNC-023 | [PainelFinanceiroController](../src/main/java/com/finisus/adapters/in/web/PainelFinanceiroController.java), linha 38 |
| GET | `/api/v1/dashboard/patrimonio` | FUNC-023 | [PainelFinanceiroController](../src/main/java/com/finisus/adapters/in/web/PainelFinanceiroController.java), linha 44 |
| GET | `/api/v1/dashboard/compartilhados` | FUNC-023 | [PainelFinanceiroController](../src/main/java/com/finisus/adapters/in/web/PainelFinanceiroController.java), linha 49 |
| GET | `/api/v1/dashboard/receitas-gastos` | FUNC-023 | [PainelFinanceiroController](../src/main/java/com/finisus/adapters/in/web/PainelFinanceiroController.java), linha 55 |
| GET | `/api/v1/financiamentos/{financiamentoId}/parcelas` | FUNC-014 | [ParcelaFinanciamentoController](../src/main/java/com/finisus/adapters/in/web/ParcelaFinanciamentoController.java), linha 46 |
| POST | `/api/v1/financiamentos/{financiamentoId}/parcelas/{parcelaId}/pagar` | FUNC-014 | [ParcelaFinanciamentoController](../src/main/java/com/finisus/adapters/in/web/ParcelaFinanciamentoController.java), linha 54 |
| POST | `/api/v1/financiamentos/{financiamentoId}/parcelas/{parcelaId}/estornar-pagamento` | FUNC-014 | [ParcelaFinanciamentoController](../src/main/java/com/finisus/adapters/in/web/ParcelaFinanciamentoController.java), linha 60 |
| POST | `/api/v1/financiamentos/{financiamentoId}/parcelas/{parcelaId}/refinanciamento` | FUNC-015 | [ParcelaFinanciamentoController](../src/main/java/com/finisus/adapters/in/web/ParcelaFinanciamentoController.java), linha 66 |
| DELETE | `/api/v1/financiamentos/{financiamentoId}/parcelas/{parcelaId}/erro-de-lancamento` | FUNC-015 | [ParcelaFinanciamentoController](../src/main/java/com/finisus/adapters/in/web/ParcelaFinanciamentoController.java), linha 73 |
| GET | `/api/v1/usuarios/me` | FUNC-001 | [PerfilUsuarioController](../src/main/java/com/finisus/adapters/in/web/PerfilUsuarioController.java), linha 42 |
| PATCH | `/api/v1/usuarios/me` | FUNC-001 | [PerfilUsuarioController](../src/main/java/com/finisus/adapters/in/web/PerfilUsuarioController.java), linha 47 |
| DELETE | `/api/v1/usuarios/me` | FUNC-025 | [PerfilUsuarioController](../src/main/java/com/finisus/adapters/in/web/PerfilUsuarioController.java), linha 53 |
| GET | `/api/v1/usuarios/me/dados` | FUNC-025 | [PerfilUsuarioController](../src/main/java/com/finisus/adapters/in/web/PerfilUsuarioController.java), linha 62 |
| POST | `/api/v1/usuarios/me/solicitacoes-anonimizacao` | FUNC-025 | [PerfilUsuarioController](../src/main/java/com/finisus/adapters/in/web/PerfilUsuarioController.java), linha 69 |
| GET | `/api/v1/usuarios/me/solicitacoes-privacidade` | FUNC-025 | [PerfilUsuarioController](../src/main/java/com/finisus/adapters/in/web/PerfilUsuarioController.java), linha 76 |
| GET | `/api/v1/investimentos/{investimentoId}/posicoes` | FUNC-017 | [PosicaoInvestimentoController](../src/main/java/com/finisus/adapters/in/web/PosicaoInvestimentoController.java), linha 40 |
| POST | `/api/v1/investimentos/{investimentoId}/posicoes` | FUNC-017 | [PosicaoInvestimentoController](../src/main/java/com/finisus/adapters/in/web/PosicaoInvestimentoController.java), linha 48 |
| POST | `/api/v1/previsoes/recalcular` | FUNC-024 | [PrevisaoFluxoCaixaController](../src/main/java/com/finisus/adapters/in/web/PrevisaoFluxoCaixaController.java), linha 38 |
| GET | `/api/v1/previsoes/{anoMes}` | FUNC-024 | [PrevisaoFluxoCaixaController](../src/main/java/com/finisus/adapters/in/web/PrevisaoFluxoCaixaController.java), linha 44 |
| GET | `/api/v1/compartilhamentos/{despesaId}/rateios` | FUNC-019 | [RateioDespesaController](../src/main/java/com/finisus/adapters/in/web/RateioDespesaController.java), linha 33 |
| POST | `/api/v1/compartilhamentos/rateios/{rateioId}/resposta` | FUNC-019 | [RateioDespesaController](../src/main/java/com/finisus/adapters/in/web/RateioDespesaController.java), linha 42 |
| POST | `/api/v1/compartilhamentos/rateios/{rateioId}/pagar` | FUNC-019 | [RateioDespesaController](../src/main/java/com/finisus/adapters/in/web/RateioDespesaController.java), linha 49 |
| GET | `/api/v1/compartilhamentos/rateios/recebidos` | FUNC-019 | [RateioDespesaController](../src/main/java/com/finisus/adapters/in/web/RateioDespesaController.java), linha 55 |
| GET | `/api/v1/recorrencias` | FUNC-010 | [RecorrenciaController](../src/main/java/com/finisus/adapters/in/web/RecorrenciaController.java), linha 53 |
| POST | `/api/v1/recorrencias` | FUNC-010 | [RecorrenciaController](../src/main/java/com/finisus/adapters/in/web/RecorrenciaController.java), linha 60 |
| GET | `/api/v1/recorrencias/{recorrenciaId}` | FUNC-010 | [RecorrenciaController](../src/main/java/com/finisus/adapters/in/web/RecorrenciaController.java), linha 67 |
| PATCH | `/api/v1/recorrencias/{recorrenciaId}` | FUNC-010 | [RecorrenciaController](../src/main/java/com/finisus/adapters/in/web/RecorrenciaController.java), linha 72 |
| DELETE | `/api/v1/recorrencias/{recorrenciaId}` | FUNC-010 | [RecorrenciaController](../src/main/java/com/finisus/adapters/in/web/RecorrenciaController.java), linha 79 |
| POST | `/api/v1/recorrencias/geracoes/{anoMes}` | FUNC-010 | [RecorrenciaController](../src/main/java/com/finisus/adapters/in/web/RecorrenciaController.java), linha 85 |
| GET | `/api/v1/recorrencias/ocorrencias/{anoMes}` | FUNC-010 | [RecorrenciaController](../src/main/java/com/finisus/adapters/in/web/RecorrenciaController.java), linha 91 |
| POST | `/api/v1/recorrencias/ocorrencias/{ocorrenciaId}/realizar` | FUNC-010 | [RecorrenciaController](../src/main/java/com/finisus/adapters/in/web/RecorrenciaController.java), linha 97 |
| GET | `/api/v1/transacoes` | FUNC-004 | [TransacaoController](../src/main/java/com/finisus/adapters/in/web/TransacaoController.java), linha 59 |
| POST | `/api/v1/transacoes` | FUNC-004 | [TransacaoController](../src/main/java/com/finisus/adapters/in/web/TransacaoController.java), linha 79 |
| GET | `/api/v1/transacoes/{transacaoId}` | FUNC-004 | [TransacaoController](../src/main/java/com/finisus/adapters/in/web/TransacaoController.java), linha 85 |
| PATCH | `/api/v1/transacoes/{transacaoId}` | FUNC-005 | [TransacaoController](../src/main/java/com/finisus/adapters/in/web/TransacaoController.java), linha 90 |
| PUT | `/api/v1/transacoes/{transacaoId}/itens` | FUNC-006 | [TransacaoController](../src/main/java/com/finisus/adapters/in/web/TransacaoController.java), linha 97 |
| POST | `/api/v1/transacoes/{transacaoId}/estorno` | FUNC-005 | [TransacaoController](../src/main/java/com/finisus/adapters/in/web/TransacaoController.java), linha 106 |
| GET | `/api/v1/transacoes/{transacaoId}/historico` | FUNC-006 | [TransacaoController](../src/main/java/com/finisus/adapters/in/web/TransacaoController.java), linha 111 |
| POST | `/api/v1/transferencias` | FUNC-007 | [TransferenciaContaController](../src/main/java/com/finisus/adapters/in/web/TransferenciaContaController.java), linha 44 |
| GET | `/api/v1/transferencias/{transferenciaId}` | FUNC-007 | [TransferenciaContaController](../src/main/java/com/finisus/adapters/in/web/TransferenciaContaController.java), linha 54 |
| POST | `/api/v1/transferencias/{transferenciaId}/estornar` | FUNC-007 | [TransferenciaContaController](../src/main/java/com/finisus/adapters/in/web/TransferenciaContaController.java), linha 59 |
