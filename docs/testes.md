# Testes

Execute a verificação completa com:

```powershell
mvn clean verify
```

## Cobertura atual

A suíte combina testes de domínio, serviços de aplicação, conversão de segurança, paginação e integração HTTP. Os testes de integração inicializam o contexto Spring, usam H2 em memória e executam a baseline Flyway desde um schema vazio antes das verificações.

Os cenários cobrem autenticação, publicação do OpenAPI, matriz estrutural de autenticação dos controllers, pesquisa paginada de transações, auditoria completa de correções, transferências entre contas com idempotência e estorno, reconciliação e ajuste auditado de saldo com idempotência e isolamento entre usuários, regras de financiamento e parcelas, obrigações com pagamento integral ou parcial, juros, encargos, desconto, saldo pendente e estorno, ciclo automático de fatura com meses curtos, distribuição de compras parceladas pelas competências das faturas conforme o fechamento, pagamento parcial de fatura, crédito excedente, transporte do crédito sem novo caixa e repetição idempotente, rateio, resumo financeiro por período, origem e ano, importação de documentos PDF e CSV — incluindo layouts de fatura e extrato Sicredi, extrato C6 em PDF, associação sem novo efeito financeiro, identificação da transferência de origem e recusa de vínculos com tipo, data ou valor incompatíveis —, exportação do titular sem credenciais, solicitação idempotente de anonimização, posição de investimento, saldo livre da visão geral, consolidação de compartilhados e conversão do `sub` validado do JWT para `UsuarioAutenticado`. A integração financeira também habilita temporariamente as estatísticas do Hibernate para manter a agenda anual em até 10 statements SQL e o patrimônio em até 12 no cenário de referência, protegendo os carregamentos em lote contra regressões N+1. A importação possui orçamento próprio de até oito statements no cenário de cinco lançamentos.

## Banco de dados

H2 dá retorno rápido para a suíte local, mas não substitui a validação contra MySQL. `MySqlMigrationsIntegrationTest` sobe MySQL 8.4 com Testcontainers, instala todas as migrations em um schema vazio e verifica a versão final e estruturas críticas, incluindo transferências e ajustes de saldo. Execute-o isoladamente com:

```powershell
mvn "-Dtest=MySqlMigrationsIntegrationTest" test
```

O teste exige Docker disponível. Quando nenhum ambiente Docker é encontrado, ele aparece explicitamente como ignorado; esse resultado não comprova compatibilidade com MySQL.

## Organização e ferramentas

- JUnit Jupiter e AssertJ para domínio e serviços.
- Mockito para isolar portas em testes unitários.
- `@SpringBootTest`/MockMvc para contexto, segurança e OpenAPI.
- H2 2.4 em modo MySQL com Flyway para integrações rápidas.
- MySQL 8.4 em Testcontainers para comprovar a criação da baseline, constraints, índices e reaplicação idempotente. O teste é pulado quando Docker não está disponível.
- Testcontainers com MySQL 8.4 para compatibilidade real de migrations.
- Estatísticas do Hibernate para orçamentos de queries.

Registre a quantidade efetiva de testes a partir do relatório da execução atual. Quando Docker não estiver disponível, o teste MySQL será ignorado explicitamente e essa limitação deve acompanhar o resultado.

## Mapa de riscos cobertos

| Área | Evidência principal |
|---|---|
| Segurança web | `AuthApiIntegrationTest`, `OwnershipWebContractIntegrationTest`, testes do validador JWT e CORS |
| Integridade monetária | testes de transação, fatura, obrigação, financiamento, transferência e investimento |
| Contratos | `OpenApiIntegrationTest` e testes MockMvc |
| Banco | migrations H2 em toda integração e `MySqlMigrationsIntegrationTest` quando Docker existe |
| Desempenho | limites de statements de agenda, patrimônio e importação |
| Privacidade | `PrivacidadeIntegrationTest` e testes do perfil |
| PDF | `LeitorDocumentoFinanceiroPdfAdapterTest` e testes dos leitores por instituição |
| CSV | testes do parser limitado, dos layouts de fatura/Sicredi e da seleção por conteúdo |

## Limites conhecidos

- Não há relatório de cobertura por linha/branch configurado no `pom.xml`.
- O teste Testcontainers valida schema MySQL, mas a suíte não comprova carga prolongada nem todos os conflitos concorrentes reais.
- Integrações externas não são simuladas porque não foram encontradas chamadas externas no código atual.
- Os testes dos leitores usam conteúdo sintético; não garantem layouts ainda não amostrados, como documentos Cresol, nem variações futuras emitidas pelos bancos.
- PDFs protegidos por senha, digitalizados sem camada de texto e dependentes de OCR permanecem fora do suporte.
