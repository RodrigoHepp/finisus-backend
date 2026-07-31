# API

O contrato detalhado está disponível no [Swagger UI](http://localhost:8080/api/v1/swagger-ui.html). A especificação OpenAPI em JSON é publicada em `http://localhost:8080/api/v1/docs`.

As rotas, exceto autenticação e documentação, exigem um access token JWT no cabeçalho `Authorization: Bearer <token>`. No Swagger UI, obtenha o token em `POST /api/v1/auth/login` e use o botão **Authorize** para informar somente o access token. As respostas de validação e de regra de negócio usam `ProblemDetail`.

As coleções usam paginação por offset com os parâmetros `pagina` (a partir de zero) e `tamanho` (de 1 a 100).

Os recursos de banco, conta, categoria, item, meio de pagamento, transação, fatura e movimento de investimento possuem controllers e tags OpenAPI próprios. As rotas existentes foram preservadas; por exemplo, as faturas continuam em `/api/v1/cartoes/{cartaoId}/faturas` e `/api/v1/cartoes/faturas/**`, e os movimentos em `/api/v1/investimentos/**/movimentos`.

O catálogo de itens é exposto em `/api/v1/itens`. Novas linhas em transações e gastos de cartão recebem `itemId` e `valor`; nome e categoria são copiados do item para manter o histórico do lançamento. Um item inativo não pode ser usado em novos lançamentos. `POST /api/v1/compartilhamentos` aceita `transacaoItemId` opcional para ratear somente uma ocorrência de item; sem esse campo, rateia a transação inteira.

`GET /api/v1/cartoes/faturas/{faturaId}` retorna a fatura, seu total de gastos não estornados e as transações de cartão vinculadas, incluindo os itens e snapshots de cada lançamento. A fatura não possui itens diretamente: o caminho é fatura → transação → item da transação.

Parcelas de financiamento oferecem dois fluxos explícitos: `POST /api/v1/financiamentos/{financiamentoId}/parcelas/{parcelaId}/refinanciamento` encerra o financiamento e remove apenas parcelas não pagas a partir da parcela indicada; `DELETE /api/v1/financiamentos/{financiamentoId}/parcelas/{parcelaId}/erro-de-lancamento` remove uma parcela e recalcula o cronograma somente se não houver parcelas pagas.

`POST /api/v1/recorrencias/geracoes/{anoMes}` aceita somente a competência atual ou passada. Competências futuras são uma previsão e não podem gerar lançamentos nem alterar o saldo da conta.

Operações de ciclo de vida usam comandos explícitos: `POST /api/v1/compartilhamentos/{id}/cancelar`, `GET /api/v1/compartilhamentos/{id}`, `GET /api/v1/compartilhamentos/rateios/recebidos`, `PATCH /api/v1/cartoes/faturas/{id}`, `POST /api/v1/cartoes/faturas/{id}/cancelar`, `POST /api/v1/financiamentos/{id}/cancelar` e `POST /api/v1/investimentos/movimentos/{id}/estornar`. O criador administra a despesa compartilhada; o participante interno consulta apenas os próprios rateios recebidos. Transições inválidas retornam `422`; conflitos de lock ou versão retornam `409`.
