# Contexto do Frontend — Finisus

Este arquivo descreve o contrato e os fluxos atualmente expostos pelo backend Finisus. Use-o como contexto para implementar o frontend; o OpenAPI publicado pela aplicação é a fonte final para schemas e exemplos atualizados.

## Produto e escopo

Finisus é uma aplicação de finanças pessoais. Cada pessoa autenticada administra seus próprios dados: bancos, contas, categorias, meios de pagamento, itens, transações, compras parceladas, cartões e faturas, financiamentos, investimentos, recorrências, previsões e compartilhamentos.

Não há conceito de administrador no contrato HTTP. Os recursos protegidos sempre são resolvidos para o usuário do token; o frontend **não envia `usuarioId`** nas requisições comuns.

## Conexão com a API

- URL local da API: `http://localhost:8080`
- Prefixo de todas as rotas de negócio: `/api/v1`
- OpenAPI JSON: `http://localhost:8080/api/v1/docs`
- Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`
- Health check público: `GET /actuator/health`
- Protocolo: JSON UTF-8

No frontend, mantenha a origem em configuração de ambiente, por exemplo `API_URL=http://localhost:8080/api/v1`, sem fixá-la nos componentes.

## Autenticação e sessão

As únicas rotas públicas são `/api/v1/auth/**`, documentação e health check. Todas as demais exigem:

```http
Authorization: Bearer <accessToken>
```

Fluxo esperado:

1. Cadastro: `POST /auth/cadastro` com `{ "nome", "email", "senha" }`; retorna `201` e o usuário criado.
2. Login: `POST /auth/login` com `{ "email", "senha" }`; retorna tokens.
3. Em toda chamada protegida, envie o `accessToken` no cabeçalho `Authorization`.
4. Antes de expirar — ou após receber `401` — envie o `refreshToken` em `POST /auth/refresh` para receber um novo par de tokens.
5. Se a renovação falhar, limpe a sessão local e redirecione para login.

Resposta de login e refresh:

```json
{
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "expiraEm": "2026-08-06T18:00:00Z"
}
```

O frontend deve tratar `expiraEm` como instante UTC. Não inclua o token em URL, query string ou logs. O backend é stateless e CORS aceita, por configuração, os métodos `GET`, `POST`, `PUT`, `PATCH`, `DELETE` e `OPTIONS`; a origem liberada deve ser ajustada no ambiente do backend quando necessário.

## Convenções de dados

- IDs são números inteiros positivos (`Long` no backend).
- Valores monetários e percentuais são números JSON (`BigDecimal` no backend). Preserve-os como decimal no estado da interface; formate apenas na apresentação usando `pt-BR` e BRL. Não faça cálculos financeiros com ponto flutuante comum.
- Datas (`LocalDate`) usam `YYYY-MM-DD`, por exemplo `2026-08-06`.
- Data/hora (`LocalDateTime`) não possui offset; instantes (`Instant`, como `expiraEm`) vêm em UTC com `Z`.
- Enums são strings em maiúsculas, por exemplo `"SAIDA"`.
- Campos ausentes não devem ser inventados no cliente. IDs de relações são enviados como `contaId`, `categoriaId`, `bancoId` etc.
- Recursos desativados normalmente retornam `204 No Content` no `DELETE`; não há exclusão física a ser presumida pela interface.

### Paginação

Listagens paginadas aceitam `pagina` (zero baseada, padrão `0`) e `tamanho` (padrão `20`, intervalo `1..100`). A resposta tem sempre este formato:

```ts
type Pagina<T> = {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
};
```

Use `totalPaginas` e `pagina` para os controles de navegação. Evite solicitar tamanhos acima de 100.

### Erros

O backend retorna erros no formato RFC 7807 (`ProblemDetail`). O frontend deve priorizar `detail` para a mensagem ao usuário e manter `status` para comportamento técnico.

```ts
type ApiProblem = {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
};
```

- `400`: payload ou parâmetro inválido; mostre `detail` próximo ao formulário quando possível.
- `401`: token ausente, inválido ou expirado; tente refresh uma vez e, se falhar, faça logout.
- `403`: usuário não pode acessar o recurso; não tente contornar no cliente.
- `404`: recurso não encontrado ou não disponível para o usuário.
- `409`: conflito de concorrência/lock; informe e ofereça recarregar os dados.
- `422`: regra de negócio ou transição de estado inválida; mostre `detail` e preserve o formulário.

## Catálogos, contas e transações

Comece as telas financeiras carregando os catálogos de bancos, categorias, contas e meios de pagamento. Os recursos abaixo seguem o padrão de listar paginado, criar, buscar por ID, atualizar parcialmente com `PATCH` e inativar com `DELETE`.

| Recurso | Base | Campos principais de criação/edição | Retorno relevante |
| --- | --- | --- | --- |
| Bancos | `/bancos` | `nome`, `codigo` | `id`, `nome`, `codigo`, `sistema` |
| Contas | `/contas` | `nome`, `tipo`, `bancoId` opcional | `id`, `nome`, `tipo`, `bancoId`, `saldo`, `ativo` |
| Categorias | `/categorias` | `nome`, `categoriaPaiId` opcional | `id`, `nome`, `categoriaPaiId`, `ativo` |
| Meios de pagamento | `/meios-pagamento` | `nome` | `id`, `nome`, `ativo` |
| Itens de catálogo | `/itens` | `nome`, `categoriaPadraoId` opcional | `id`, `nome`, `categoriaPadraoId`, `ativo` |

Rotas CRUD: `GET` e `POST` na base; `GET`, `PATCH` e `DELETE` em `/{id}`. As respostas de `GET` na coleção são `Pagina<T>`.

Enums úteis:

```ts
type TipoConta = 'FISICO' | 'CORRENTE' | 'POUPANCA' | 'APLICACAO';
type TipoTransacao = 'ENTRADA' | 'SAIDA';
```

### Transações

Base: `/transacoes`.

| Método e rota | Uso |
| --- | --- |
| `GET /transacoes?pagina&tamanho` | lista transações do usuário |
| `POST /transacoes` | cria uma entrada ou saída |
| `GET /transacoes/{transacaoId}` | detalhe |
| `PATCH /transacoes/{transacaoId}` | altera a transação |
| `POST /transacoes/{transacaoId}/estorno` | estorna, preservando histórico |
| `GET /transacoes/{transacaoId}/historico?pagina&tamanho` | consulta auditoria da transação |

Payload de criar/editar:

```ts
type TransacaoInput = {
  tipo: TipoTransacao;
  valor: number; // maior que zero
  data: string; // YYYY-MM-DD
  descricao: string;
  contaId: number;
  categoriaId?: number;
  meioPagamentoId?: number;
  itens?: Array<{
    itemId: number;
    valor: number;
  }>;
};

type Transacao = {
  id: number;
  tipo: TipoTransacao;
  valor: number;
  data: string;
  descricao: string | null;
  contaId: number;
  categoriaId: number | null;
  meioPagamentoId: number | null;
  itens: Array<{
    id: number;
    itemId: number;
    descricao: string;
    valor: number;
    categoriaId: number | null;
  }>;
};
```

`SAIDA` representa gasto e `ENTRADA` representa recebimento. Para a UX de estorno, confirme a ação: ela não é um `DELETE`. O histórico retorna registros com `campoAlterado`, `valorAnterior`, `valorNovo`, `alteradoPor` e instante de alteração.

### Compras parceladas e recorrências

| Recurso | Rotas e ações |
| --- | --- |
| Compras parceladas | `GET/POST /compras-parceladas`, `GET /compras-parceladas/{compraId}`, `POST /compras-parceladas/{compraId}/cancelar` |
| Recorrências | `GET/POST /recorrencias`, `GET/PATCH/DELETE /recorrencias/{recorrenciaId}`, `POST /recorrencias/geracoes/{anoMes}` |
| Previsões | `POST /previsoes/recalcular`, `GET /previsoes/{anoMes}` |

Compra parcelada exige `descricao`, `valorTotal`, `numeroParcelas`, `dataCompra`, `categoriaId` e `contaId`. Recorrência usa `nome`, `tipo`, `valorEsperado`, `diaDoMes`, `categoriaId` opcional, `contaId` e `meioPagamentoId` opcional. `anoMes` é uma string `YYYY-MM`.

## Cartões e faturas

Cartões usam base `/cartoes` e o CRUD padrão: `GET/POST /cartoes`, `GET/PATCH/DELETE /cartoes/{cartaoId}`. O payload contém `nome`, `limite`, `diaFechamento` e `diaVencimento`; a resposta também informa `ativo`.

Faturas possuem comandos de ciclo de vida, não CRUD genérico:

| Método e rota | Uso |
| --- | --- |
| `GET /cartoes/{cartaoId}/faturas?pagina&tamanho` | lista faturas de um cartão |
| `POST /cartoes/faturas` | cria fatura (`cartaoId`, `anoMes`, `dataFechamento`, `dataVencimento`, `contaPagamentoId`) |
| `GET /cartoes/faturas/{faturaId}` | detalhe, incluindo gastos |
| `POST /cartoes/faturas/{faturaId}/gastos` | adiciona gasto (`valor`, `data`, `descricao`, `contaId`, `categoriaId` opcional e `itens` opcionais) |
| `POST /cartoes/faturas/{faturaId}/fechar` | fecha a fatura |
| `POST /cartoes/faturas/{faturaId}/pagar` | marca como paga (`dataPagamento`) |
| `PATCH /cartoes/faturas/{faturaId}` | altera fechamento, vencimento e conta de pagamento |
| `POST /cartoes/faturas/{faturaId}/cancelar` | cancela a fatura |

```ts
type StatusFatura = 'ABERTA' | 'FECHADA' | 'PAGA' | 'CANCELADA';
```

Modele botões e formulários conforme o status retornado, e deixe o backend validar a transição. Não permita que a interface represente uma fatura cancelada como pagável.

## Financiamentos e investimentos

### Financiamentos

- `GET/POST /financiamentos` e `GET /financiamentos/{financiamentoId}`.
- `POST /financiamentos/{financiamentoId}/cancelar`.
- `GET /financiamentos/{financiamentoId}/parcelas?pagina&tamanho`.
- `POST /financiamentos/{financiamentoId}/parcelas/{parcelaId}/pagar` com `{ "dataPagamento": "YYYY-MM-DD" }`.
- `POST /financiamentos/{financiamentoId}/parcelas/{parcelaId}/refinanciamento`.
- `DELETE /financiamentos/{financiamentoId}/parcelas/{parcelaId}/erro-de-lancamento`.

```ts
type StatusFinanciamento = 'ATIVO' | 'FINALIZADO' | 'CANCELADO';
type StatusParcelaFinanciamento = 'PENDENTE' | 'PAGA' | 'ATRASADA';
```

### Investimentos

- `GET/POST /investimentos`, `GET/PATCH/DELETE /investimentos/{investimentoId}`.
- `GET /investimentos/{investimentoId}/movimentos?pagina&tamanho`.
- `POST /investimentos/movimentos`.
- `POST /investimentos/movimentos/{movimentoId}/estornar`.

```ts
type TipoInvestimento = 'RENDA_FIXA' | 'RENDA_VARIAVEL' | 'FUNDO' | 'CRIPTO' | 'OUTRO';
type TipoMovimentoInvestimento = 'APORTE' | 'RESGATE';
```

O investimento recebe `nome`, `tipo` e `contaOrigemId`; movimentos recebem `investimentoId`, `tipo`, `valor` e `data`. Estorno também é uma ação explícita, não uma remoção.

## Perfil e compartilhamentos

### Perfil

Base: `/usuarios/me`.

- `GET /usuarios/me`: carrega o perfil autenticado.
- `PATCH /usuarios/me`: atualiza `{ nome, email }`.
- `DELETE /usuarios/me`: desativa o próprio usuário.

### Compartilhamento por despesa/rateio

Este módulo cria uma despesa compartilhada a partir de uma transação já existente — ou de um item dela. Antes de exibir seus fluxos, consulte o opt-in:

- `GET /compartilhamentos/opt-in`
- `PUT /compartilhamentos/opt-in` com `{ "aceita": true | false }`

Rotas principais:

| Método e rota | Uso |
| --- | --- |
| `GET/POST /compartilhamentos` | lista/cria despesas compartilhadas |
| `GET /compartilhamentos/{despesaId}` | detalhe resumido |
| `POST /compartilhamentos/{despesaId}/cancelar` | cancela a despesa compartilhada |
| `GET /compartilhamentos/{despesaId}/rateios?pagina&tamanho` | rateios da despesa |
| `POST /compartilhamentos/rateios/{rateioId}/resposta` | responde `{ "aceita": true | false }` |
| `POST /compartilhamentos/rateios/{rateioId}/pagar` | marca rateio como pago |
| `GET /compartilhamentos/rateios/recebidos?pagina&tamanho` | rateios pendentes/recebidos pelo usuário |

Criação de despesa compartilhada:

```ts
type TipoRateio = 'VALOR_FIXO' | 'PERCENTUAL';
type CriarDespesaCompartilhada = {
  transacaoId: number;
  transacaoItemId?: number;
  tipoRateio: TipoRateio;
  participantes: Array<{
    usuarioId?: number;     // participante interno
    nomeExterno?: string;   // participante externo
    emailExterno?: string;
    valorFixo?: number;     // use para VALOR_FIXO
    percentual?: number;    // use para PERCENTUAL
  }>;
};
```

```ts
type StatusRateio = 'PENDENTE' | 'ACEITO' | 'RECUSADO' | 'PAGO' | 'CANCELADO';
type TipoParticipante = 'INTERNO' | 'EXTERNO';
```

Use ações distintas para aceitar/recusar e para marcar pago. Um participante externo não é uma conta local autenticável; ele pode ser apresentado como nome/email informado no rateio.

### Divisões compartilhadas por transações

Este é um conceito diferente do rateio acima: agrupa contas compartilhadas recorrentes (por exemplo, água, luz, seguro ou supermercado) e calcula quem pagou mais ou menos usando as **transações reais** vinculadas. O pagador é derivado do usuário que registrou cada transação, não de um campo manual de pagamento.

| Método e rota | Uso |
| --- | --- |
| `GET/POST /divisoes-compartilhadas` | lista/cria grupos |
| `GET /divisoes-compartilhadas/{divisaoId}` | detalhe do grupo |
| `PATCH /divisoes-compartilhadas/{divisaoId}/participantes` | substitui participantes/percentuais |
| `DELETE /divisoes-compartilhadas/{divisaoId}` | inativa o grupo |
| `POST /divisoes-compartilhadas/{divisaoId}/transacoes` | vincula `{ "transacaoId": number }` |
| `DELETE /divisoes-compartilhadas/{divisaoId}/transacoes/{transacaoId}` | remove vínculo |
| `GET /divisoes-compartilhadas/{divisaoId}/resumo?inicio=YYYY-MM-DD&fim=YYYY-MM-DD` | saldo por participante |

```ts
type ParticipanteDivisao = { usuarioId: number; percentual: number };
type DivisaoInput = {
  nome: string;
  participantes: ParticipanteDivisao[];
};
type Divisao = {
  id: number;
  nome: string;
  criadorId: number;
  status: 'ATIVA' | 'INATIVA';
  participantes: ParticipanteDivisao[];
};
type ResumoDivisao = {
  divisaoId: number;
  divisao: string;
  total: number;
  participantes: Array<{
    usuarioId: number;
    percentual: number;
    pago: number;
    devido: number;
    saldo: number; // pago - devido; positivo indica crédito
  }>;
};
```

Regras importantes para a interface:

- Criador deve ser participante e todos os participantes são usuários ativos com opt-in de compartilhamento.
- Os percentuais devem totalizar exatamente 100; mostre a soma durante a edição.
- Uma transação só pode estar em uma divisão.
- O resumo considera somente transações de saída, ativas e não estornadas, dentro do período.
- Em divisão desigual, os centavos remanescentes são atribuídos ao último participante para conservar o total.
- Mostre claramente `pago`, `devido` e `saldo`; não assuma divisão 50/50 nem status de casal.

## Organização sugerida do frontend

1. Uma camada única de cliente HTTP: URL de ambiente, JSON, `Authorization`, renovação coordenada do token e conversão de `ProblemDetail`.
2. Uma área pública restrita ao login e uma área autenticada com guarda de rota; o cadastro de usuários exige a permissão `USUARIO_CADASTRAR`.
3. Carregue catálogos antes de formulários de transação, fatura, recorrência e compra parcelada.
4. Mantenha telas de detalhe para recursos que expõem ações de estado: transações, faturas, financiamento, investimento, rateios e divisões compartilhadas.
5. Recarregue dados após comandos com efeito financeiro; em `409`, descarte o estado potencialmente desatualizado e consulte novamente.

## Limites deste contexto

Este arquivo registra o contrato observado no código atual do backend. Para gerar tipos completos, conferir campos opcionais e validar payloads, consuma `GET /api/v1/docs` no backend em execução. Não deduza endpoints a partir das entidades JPA ou das migrations.
