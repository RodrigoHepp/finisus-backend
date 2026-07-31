# Testes

Execute a verificação completa com:

```powershell
mvn clean verify
```

## Cobertura atual

A suíte combina testes de domínio, serviços de aplicação, conversão de segurança, paginação e integração HTTP. Os testes de integração inicializam o contexto Spring, usam H2 em memória e executam as migrations Flyway antes das verificações.

Os cenários cobrem autenticação, publicação do OpenAPI, regras de financiamento e parcelas, rateio, fatura e conversão do `sub` validado do JWT para `UsuarioAutenticado`.

## Banco de dados

H2 dá retorno rápido para a suíte local, mas não substitui a validação contra MySQL. O projeto declara Testcontainers para testes de integração que precisarem verificar comportamento específico do banco; esses testes dependem de Docker disponível no ambiente.
