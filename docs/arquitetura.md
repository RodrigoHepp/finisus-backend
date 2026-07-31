# Arquitetura

O Finisus usa o pacote-base `com.finisus` e organiza o backend em camadas com dependências voltadas ao núcleo do negócio. Spring, HTTP, JPA e JWT ficam nas bordas; os modelos de domínio não dependem desses frameworks.

## Organização do código

- `domain`: entidades, value objects, enums, exceções e invariantes do negócio.
- `application`: casos de uso, serviços, paginação e portas de entrada e saída.
- `adapters.in.web`: controllers REST, DTOs de transporte e obtenção do usuário autenticado.
- `adapters.out.persistence`: adapters JPA, entidades, repositórios e mapeamentos de persistência.
- `adapters.out.security`: implementação de criptografia de senha e emissão de tokens.
- `infrastructure`: configuração Spring, segurança, tratamento de erros, observabilidade, relógio e agendamentos.

O fluxo principal é `HTTP → controller → porta de entrada → serviço de aplicação → porta de saída → adapter JPA`. O retorno percorre o caminho inverso por DTOs; entidades JPA não fazem parte do contrato HTTP.

## Domínio e aplicação

Os serviços de aplicação coordenam casos de uso e definem limites transacionais. O domínio concentra validações que não dependem de transporte ou banco, como regras de financiamento, parcelas, rateio, fatura, transação e investimento.

As portas mantêm a aplicação independente de implementação. Por exemplo, um caso de uso depende de um repositório de domínio, enquanto o adapter JPA decide consultas, entidades e mapeamentos. A data operacional é obtida por `ObterDataAtualPort`, implementado na infraestrutura com `Clock`.

Operações que alteram mais de uma entidade são tratadas como processos explícitos. Pagamento de parcela, refinanciamento, criação de despesa compartilhada, fechamento de fatura e estorno de movimento de investimento preservam consistência entre os registros envolvidos.

## Persistência e integridade

O ambiente de execução usa MySQL e Flyway. As migrations versionadas em `src/main/resources/db/migration` são aplicadas na inicialização; alterações de schema devem ser novas migrations, sem reescrever versões já aplicadas.

O modelo mantém histórico quando uma referência pode mudar no futuro. `TransacaoItem`, por exemplo, referencia o catálogo de itens e também armazena snapshots de nome, categoria e valor. Assim, a edição ou inativação do item não reescreve lançamentos existentes.

## API e segurança

Controllers recebem entradas validadas e delegam a regra de negócio aos casos de uso. O tratamento centralizado converte falhas esperadas em `ProblemDetail`, mantendo detalhes internos fora da resposta.

O Spring Security valida JWTs na borda. Depois da validação, o token é convertido em `UsuarioAutenticado`; controllers obtêm apenas o identificador por `@UsuarioAtual`, e os casos de uso recebem esse identificador sem depender de `SecurityContext`. Consulte [Autenticação](autenticacao.md) para o fluxo de tokens.

## Operação

O Actuator expõe health, informações e métricas. A aplicação também possui agendamento para geração mensal de recorrências, configurável por cron e fuso horário. Logs não devem registrar senhas, chaves ou tokens.
