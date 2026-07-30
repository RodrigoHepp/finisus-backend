# API

O contrato detalhado está disponível no Swagger UI em `/api/v1/swagger-ui.html`. A especificação OpenAPI é publicada em `/api/v1/docs`.

As rotas, exceto autenticação e documentação, exigem um access token JWT no cabeçalho `Authorization: Bearer <token>`. As respostas de validação e de regra de negócio usam `ProblemDetail`.

As coleções usam paginação por offset com os parâmetros `pagina` (a partir de zero) e `tamanho` (de 1 a 100).

`POST /api/v1/recorrencias/geracoes/{anoMes}` aceita somente a competência atual ou passada. Competências futuras são uma previsão e não podem gerar lançamentos nem alterar o saldo da conta.
