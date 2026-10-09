# Finisus Backend

API para gerenciamento de finanças pessoais. O backend concentra cadastro e autenticação de usuários, contas, transações, cartões, faturas, dashboards mensais, por período e anuais, investimentos, financiamentos, recorrências e divisões compartilhadas.

## Escopo e uso

O Finisus é o núcleo de uma solução de organização financeira pessoal. Ele permite que cada usuário mantenha seus dados financeiros estruturados, acompanhe movimentações e planeje compromissos futuros. Também contempla divisões compartilhadas, com responsabilidades, pagamentos e reembolsos entre participantes.

Com a API, um cliente pode:

- organizar bancos, contas, categorias, meios de pagamento e itens de catálogo;
- reconciliar o saldo materializado de uma conta com seus movimentos financeiros eficazes e registrar ajustes justificados;
- registrar, pesquisar com paginação e filtros, alterar e estornar receitas e despesas, preservando o histórico dos lançamentos;
- transferir valores entre duas contas próprias com idempotência, lançamentos vinculados e estorno atômico;
- administrar compras parceladas, cartões de crédito e faturas;
- acompanhar financiamentos, parcelas, investimentos, posições informadas e patrimônio líquido;
- registrar obrigações a pagar sem reduzir o saldo antes da liquidação, realizar e estornar pagamentos integrais ou parciais e consultar painéis de visão geral, agenda, receitas e gastos, patrimônio e compartilhados;
- cadastrar recorrências e consultar previsões de fluxo de caixa;
- agrupar contas compartilhadas, como água, luz, seguro, compras ou PIX, e comparar o valor pago em transações com a responsabilidade percentual ou igualmente dividida entre os participantes;
- importar extratos e faturas em layouts PDF e CSV reconhecidos, revisar os lançamentos detectados e somente então confirmá-los;
- autenticar-se com JWT e gerenciar o próprio perfil; o cadastro de novos usuários é feito por operador autorizado.

O projeto entrega a camada de backend REST: pode ser consumido por uma aplicação web, mobile ou outra integração autorizada. Não inclui uma interface gráfica para o usuário final. Depois de iniciar a aplicação, use o Swagger UI para explorar o contrato, autentique-se e envie o token nas rotas protegidas.

## Stack

- Java 25 e Spring Boot
- Maven, Spring Data JPA e Bean Validation
- MySQL com migrations Flyway
- Spring Security com JWT RSA
- OpenAPI/Swagger, Actuator e métricas
- H2 para a suíte local de testes

## Pré-requisitos

- JDK 25
- Maven 3.9 ou compatível
- MySQL para executar o perfil `dev`
- Um par de chaves RSA para assinar e validar JWTs no ambiente local

## Início rápido

Crie a configuração local a partir do exemplo e informe as variáveis exigidas. O arquivo local não é versionado.

```powershell
Copy-Item src/main/resources/application-dev.example.properties src/main/resources/application-dev.properties
$env:FINISUS_DB_USERNAME = 'seu-usuario'
$env:FINISUS_DB_PASSWORD = 'sua-senha'
$env:FINISUS_JWT_PRIVATE_KEY_LOCATION = 'file:C:/caminho/chave-privada.pem'
$env:FINISUS_JWT_PUBLIC_KEY_LOCATION = 'file:C:/caminho/chave-publica.pem'
mvn "-Dspring-boot.run.profiles=dev" spring-boot:run
```

Com a aplicação em execução, acesse:

- Swagger UI: <http://localhost:8080/api/v1/swagger-ui.html> (exige JWT com `DOCUMENTACAO_API_LER`)
- OpenAPI JSON: <http://localhost:8080/api/v1/docs> (exige JWT com `DOCUMENTACAO_API_LER`)
- Health check: <http://localhost:8080/actuator/health>

## Comandos úteis

```powershell
mvn clean verify
mvn "-Dspring-boot.run.profiles=dev" spring-boot:run
```

## Estrutura do código

O código-fonte usa o pacote-base `com.finisus`. O domínio contém modelos e invariantes; a aplicação define casos de uso e portas; adapters expõem HTTP e persistência; a infraestrutura reúne configuração, segurança, observabilidade, tempo e agendamento.

```text
src/main/java/com/finisus/
├── domain/                    modelos, enums, value objects e exceções
├── application/
│   ├── ports/in/              contratos dos casos de uso
│   ├── ports/out/             contratos de persistência e infraestrutura
│   └── service/               coordenação das regras e transações
├── adapters/
│   ├── in/web/                API REST e DTOs HTTP
│   └── out/                   JPA, segurança e leitura de documentos PDF/CSV
└── infrastructure/            configuração, JWT, erros, métricas, relógio e scheduler
```

O backend é organizado por recursos financeiros, com domínio, casos de uso e adapters HTTP e de persistência. Consulte [Arquitetura](docs/arquitetura.md) para o mapa detalhado e [API](docs/api.md) para os grupos de recursos. O schema de pré-lançamento é criado por uma baseline Flyway consolidada, sem depender de estados intermediários de desenvolvimento.

## Por onde começar

1. Leia [Arquitetura](docs/arquitetura.md) para localizar camadas, módulos e dependências.
2. Consulte [Regras de negócio](docs/regras-negocio.md) para regras `RN-*`, fluxos `FLX-*` e rastreabilidade.
3. Use o Swagger para o contrato HTTP completo e [API](docs/api.md) para convenções e mapa de controllers.
4. Prepare o ambiente por [Desenvolvimento](docs/desenvolvimento.md) e execute a suíte descrita em [Testes](docs/testes.md).
5. Antes de alterar autorização, leia [Autenticação](docs/autenticacao.md).

## Glossário resumido

| Termo | Significado no projeto |
|---|---|
| Competência | Período econômico ao qual compra, fatura ou compromisso pertence. |
| Caixa | Momento em que uma movimentação altera efetivamente o saldo. |
| Ocorrência | Fotografia mensal de uma recorrência; só vira caixa quando realizada. |
| Alocação | Parcela de uma transação real usada como pagamento de uma divisão. |
| Snapshot | Cópia histórica de dados que não deve mudar com o cadastro atual. |
| Porta | Interface da aplicação que separa regra de negócio de HTTP, JPA ou infraestrutura. |

## Licença

Copyright © 2026 Rodrigo Joel Hepp.

O Finisus é disponibilizado sob a [GNU Affero General Public License v3.0 ou posterior](LICENSE). A AGPL permite uso comercial, alteração e redistribuição, desde que suas obrigações sejam cumpridas.

Quem precisar usar, modificar ou distribuir o Finisus sem cumprir a AGPL poderá solicitar uma licença comercial alternativa. Consulte [Licenciamento comercial](COMMERCIAL-LICENSE.md). Contribuições aceitas pelo projeto seguem o [acordo de contribuição](CLA.md).

## Documentação

- [Portal de negócio: uso do sistema, jornadas, requisitos e regras para PO e QA](docs-negocio/00-README.md)
- [Arquitetura](docs/arquitetura.md)
- [API](docs/api.md)
- [Autenticação](docs/autenticacao.md)
- [Privacidade e ciclo de vida dos dados](docs/privacidade-ciclo-vida.md)
- [Desenvolvimento](docs/desenvolvimento.md)
- [Regras de negócio](docs/regras-negocio.md)
- [Testes](docs/testes.md)

## Pontos pendentes de validação

- A política jurídica de retenção e anonimização seletiva precisa ser aprovada pelo controlador dos dados.
- A baseline precisa ser executada em MySQL 8.4 com Docker/Testcontainers disponível; H2 em modo MySQL é uma validação complementar e não comprova o dialeto real.
- O procedimento de deploy e os ambientes além do perfil local não estão definidos neste repositório.

## Informações não encontradas

- Não foram encontrados mensageria, cache distribuído, chamadas HTTP para terceiros, infraestrutura como código ou configuração de container da aplicação.
- Não foi encontrado papel administrativo; bancos globais são somente leitura para usuários comuns.
- Não foram encontrados indicadores de cobertura por linha ou branch.

## Possíveis inconsistências

- `springdoc-openapi` está declarado explicitamente enquanto Spring Boot está em uma geração principal mais nova; a publicação do OpenAPI é coberta por teste, mas a compatibilidade deve ser reavaliada em upgrades.
- O repositório contém documentos históricos além do conjunto canônico listado acima; eles registram auditorias, não substituem a documentação atual.

## Riscos técnicos identificados

- Operações financeiras e locks precisam de validação concorrente em MySQL real; H2 não reproduz integralmente sua semântica.
- `UseCaseConfiguration` concentra grande parte da composição manual dos casos de uso e tende a crescer com novos módulos.
- A exportação de dados pessoais executa consultas explícitas por seção; novas tabelas com dados do titular precisam ser adicionadas conscientemente ao exportador.
