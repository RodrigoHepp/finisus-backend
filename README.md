# Finisus Backend

API para gerenciamento de finanças pessoais. O backend concentra cadastro e autenticação de usuários, contas, transações, cartões, faturas, investimentos, financiamentos, recorrências e compartilhamento de despesas.

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

- Swagger UI: <http://localhost:8080/api/v1/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/api/v1/docs>
- Health check: <http://localhost:8080/actuator/health>

## Comandos úteis

```powershell
mvn clean verify
mvn "-Dspring-boot.run.profiles=dev" spring-boot:run
```

## Estrutura do código

O código-fonte usa o pacote-base `com.finisus`. O domínio contém modelos e invariantes; a aplicação define casos de uso e portas; adapters expõem HTTP e persistência; a infraestrutura reúne configuração, segurança, observabilidade, tempo e agendamento.

## Licença

Copyright © 2026 Rodrigo Joel Hepp.

O Finisus é disponibilizado sob a [GNU Affero General Public License v3.0 ou posterior](LICENSE). A AGPL permite uso comercial, alteração e redistribuição, desde que suas obrigações sejam cumpridas.

Quem precisar usar, modificar ou distribuir o Finisus sem cumprir a AGPL poderá solicitar uma licença comercial alternativa. Consulte [Licenciamento comercial](COMMERCIAL-LICENSE.md). Contribuições aceitas pelo projeto seguem o [acordo de contribuição](CLA.md).

## Documentação

- [Arquitetura](docs/arquitetura.md)
- [API](docs/api.md)
- [Autenticação](docs/autenticacao.md)
- [Desenvolvimento](docs/desenvolvimento.md)
- [Regras de negócio](docs/regras-negocio.md)
- [Testes](docs/testes.md)
