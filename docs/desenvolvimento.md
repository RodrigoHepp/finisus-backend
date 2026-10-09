# Desenvolvimento

## Ambiente local

Para executar o perfil `dev`, instale JDK 25, Maven e MySQL. O datasource padrão aponta para um banco local chamado `finisus`; Flyway cria e atualiza o schema durante a inicialização.

Crie `src/main/resources/application-dev.properties` a partir do arquivo de exemplo e defina as variáveis de banco e JWT:

```powershell
Copy-Item src/main/resources/application-dev.example.properties src/main/resources/application-dev.properties
$env:FINISUS_DB_USERNAME = 'seu-usuario'
$env:FINISUS_DB_PASSWORD = 'sua-senha'
$env:FINISUS_JWT_PRIVATE_KEY_LOCATION = 'file:C:/caminho/chave-privada.pem'
$env:FINISUS_JWT_PUBLIC_KEY_LOCATION = 'file:C:/caminho/chave-publica.pem'
$env:APP_CORS_ALLOWED_ORIGINS = 'http://localhost:4200'
```

Inicie a aplicação com:

```powershell
mvn "-Dspring-boot.run.profiles=dev" spring-boot:run
```

## Configuração confirmada

| Propriedade/variável | Classe | Obrigatória | Padrão/uso |
|---|---|---:|---|
| `FINISUS_DB_USERNAME` | segredo | sim no perfil `dev` | usuário do MySQL |
| `FINISUS_DB_PASSWORD` | segredo | sim no perfil `dev` | senha do MySQL |
| `FINISUS_JWT_PRIVATE_KEY_LOCATION` | segredo/localização | sim | chave RSA para emissão |
| `FINISUS_JWT_PUBLIC_KEY_LOCATION` | ambiente | sim | chave RSA para validação |
| `APP_CORS_ALLOWED_ORIGINS` | ambiente | não | `http://localhost:4200`; somente origens exatas |
| `app.jwt.access-token-expiration-minutes` | aplicação | não | 15 |
| `app.jwt.refresh-token-expiration-days` | aplicação | não | 7 |
| `app.recorrencias.cron` | operação | não | `0 5 0 1 * *` |
| `app.recorrencias.timezone` | operação | não | `America/Sao_Paulo` |
| `app.importacao.pdf.*` | operação | não | limites defensivos descritos abaixo |

O datasource confirmado é `jdbc:mysql://localhost:3306/finisus`. A opção `createDatabaseIfNotExist=true` pede ao driver para criar o banco quando a credencial possuir permissão; Flyway cria e evolui as tabelas.

## Build e automação

O projeto não possui Maven Wrapper; use um Maven instalado. O pipeline `.github/workflows/ci.yml` executa `mvn -B clean verify` com Temurin 25 em pushes e pull requests para `master` e `development`.

O artefato empacotado é `target/finisus-backend-0.1.0-SNAPSHOT.jar`. **⚠️ Não confirmado:** não há Dockerfile, manifesto de deploy ou publicação do artefato neste repositório.

Os limites defensivos da importação PDF ficam em `app.importacao.pdf.max-pages`, `max-extracted-characters`, `max-processing-time` e `max-main-memory-bytes`. Os valores padrão são, respectivamente, 100 páginas, 2.000.000 de caracteres, 5 segundos e 8 MB de cache principal. Mantenha o limite multipart de 10 MB ou reduza todos esses valores conforme a capacidade do ambiente.

## Verificação

```powershell
mvn clean verify
```

O comando compila o projeto e executa a suíte. Com a aplicação iniciada, use `/actuator/health` para verificar disponibilidade e o Swagger UI para explorar a API.

## Observabilidade

Os endpoints Actuator expostos são `health`, `info`, `metrics` e `prometheus`. As métricas `financeiro.operacoes`, `financeiro.operacoes.duracao` e `financeiro.operacoes.concorrentes` usam somente as tags controladas `operacao` e `resultado`. O resultado diferencia `sucesso`, `falha` e `conflito`; não adicione IDs, e-mails, descrições, valores ou outros dados pessoais como labels.

`financeiro.eventos{evento="divergencia_saldo"}` conta reconciliações cujo saldo materializado difere do saldo calculado. Falhas da consulta de reconciliação aparecem em `financeiro.operacoes{operacao="reconciliacao_saldo",resultado="falha"}`. Os contadores representam ocorrências operacionais e não substituem trilhas de auditoria financeira.

## Arquivos locais

`application-dev.properties`, metadados de IDE e `target/` não devem ser versionados. Enquanto não existe ambiente persistente nem dado a preservar, a baseline de desenvolvimento pode ser consolidada e os bancos locais/testes confirmados como descartáveis podem ser recriados. Depois do primeiro uso persistente, migrations aplicadas não são editadas e toda evolução deve criar uma nova versão.

## Troubleshooting

| Problema | Causa provável | Como investigar |
|---|---|---|
| Aplicação não inicia por datasource | MySQL indisponível ou variáveis ausentes | valide serviço, banco `finisus` e variáveis `FINISUS_DB_*` |
| Falha ao carregar JWT | caminho/formatos das chaves RSA inválidos | confira prefixo `file:` e permissões; não imprima a chave |
| Inicialização recusa CORS | lista vazia ou com `*` | informe origens exatas separadas por vírgula |
| Flyway acusa checksum | baseline local mudou ou uma migration persistente foi alterada | em banco local descartável confirmado, recrie desde vazio; em ambiente persistente, restaure a migration e crie uma versão aditiva |
| PDF retorna OCR não suportado | arquivo não possui camada textual | use documento pesquisável; OCR não está implementado |
| Teste MySQL é ignorado | Docker indisponível | inicie Docker e execute `MySqlMigrationsIntegrationTest` isoladamente |
| 409 em operação financeira | lock, versão ou chave idempotente divergente | recarregue o recurso e compare chave/payload antes de repetir |
