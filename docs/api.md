# API

O contrato detalhado é publicado pela própria aplicação:

- Swagger UI: <http://localhost:8080/api/v1/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/api/v1/docs>

As rotas usam o prefixo `/api/v1`. O Swagger é a fonte de verdade para payloads, campos obrigatórios, códigos de resposta e todos os endpoints.

## Autenticação

As rotas de cadastro, login, renovação de token e documentação são públicas. Os demais recursos exigem o cabeçalho abaixo com um access token válido:

```http
Authorization: Bearer <access-token>
```

Os fluxos de `POST /api/v1/auth/cadastro`, `POST /api/v1/auth/login` e `POST /api/v1/auth/refresh` estão descritos em [Autenticação](autenticacao.md).

## Recursos

A API agrupa recursos de organização financeira — bancos, contas, categorias, meios de pagamento e itens — e recursos de operação — transações, recorrências, compras parceladas, cartões, faturas, financiamentos, investimentos, previsões e compartilhamentos.

Algumas operações possuem comandos próprios para tornar a intenção explícita, como fechar ou pagar uma fatura, cancelar uma despesa compartilhada, refinanciar uma parcela e estornar um movimento de investimento. Consulte o Swagger antes de integrar esses fluxos, pois eles podem exigir condições de estado.

## Convenções

Coleções paginadas usam offset com `pagina` iniciando em zero e `tamanho` entre 1 e 100. As respostas não expõem entidades JPA e as entradas são validadas na borda.

Falhas de validação, autenticação, autorização, regra de negócio e concorrência retornam `ProblemDetail`. Em especial, regras de estado inválido retornam `422` e conflitos de versão ou lock retornam `409`.

## Compatibilidade

Clientes devem tratar campos adicionais em respostas como compatíveis e não inferir regras a partir da estrutura interna de persistência. Mudanças incompatíveis de contrato devem ser avaliadas e documentadas antes da publicação.
