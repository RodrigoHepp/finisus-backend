# Finisus Backend

API de finanças pessoais em Java 25 e Spring Boot. O pacote-base adotado é `com.financeiro`.

Os recursos financeiros são expostos por controllers próprios; operações compostas, como refinanciamento, pagamento, cancelamento e estorno, mantêm consistência entre as entidades envolvidas. Itens são cadastrados em um catálogo pessoal e vinculados às linhas de transação, preservando o histórico de cada lançamento. Gastos de cartão são agrupados por fatura, que consulta suas transações e itens sem possuir itens diretamente.

## Desenvolvimento local

Use o perfil `dev` somente com uma configuração local não versionada. Copie `src/main/resources/application-dev.example.properties` para `application-dev.properties`, informe as variáveis de banco e JWT e inicie com o perfil `dev`.

## Comandos

```powershell
mvn test
```

## Documentação

- [Swagger UI](http://localhost:8080/api/v1/swagger-ui.html)
- [Arquitetura](docs/arquitetura.md)
- [API](docs/api.md)
- [Autenticação](docs/autenticacao.md)
- [Desenvolvimento](docs/desenvolvimento.md)
- [Testes](docs/testes.md)
- [Regras de negócio](docs/regras-negocio.md)
