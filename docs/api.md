# API

O contrato detalhado é publicado pela própria aplicação:

- Swagger UI: <http://localhost:8080/api/v1/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/api/v1/docs>

As rotas usam o prefixo `/api/v1`. O Swagger é a fonte de verdade para payloads, campos obrigatórios, códigos de resposta e todos os endpoints.

## Autenticação

As rotas de login, renovação de token e health check são públicas. O cadastro exige `USUARIO_CADASTRAR`, Swagger/OpenAPI exige `DOCUMENTACAO_API_LER` e os demais recursos exigem o cabeçalho abaixo com um access token válido:

```http
Authorization: Bearer <access-token>
```

Os fluxos de `POST /api/v1/auth/cadastro`, `POST /api/v1/auth/login` e `POST /api/v1/auth/refresh` estão descritos em [Autenticação](autenticacao.md).

## Perfil e privacidade

`GET /api/v1/usuarios/me/dados` exporta em JSON versionado os dados relacionados ao titular autenticado, incluindo cadastros, fatos financeiros, auditorias, importações e vínculos compartilhados. Credenciais, tokens e hashes de idempotência não integram a resposta.

`POST /api/v1/usuarios/me/solicitacoes-anonimizacao` recebe `motivo` e registra o pedido para análise. Enquanto existir um pedido aberto do titular, repetições reutilizam o mesmo registro. `GET /api/v1/usuarios/me/solicitacoes-privacidade` lista seu acompanhamento. O pedido não apaga dados automaticamente; retenção e anonimização seletiva dependem da política operacional descrita em [Privacidade e ciclo de vida dos dados](privacidade-ciclo-vida.md).

## Recursos

A API agrupa recursos de organização financeira — bancos, contas, categorias, meios de pagamento e itens — e recursos de operação — transações, recorrências, compras parceladas, cartões, faturas, financiamentos, investimentos, previsões e compartilhamentos.

## Inventário dos grupos HTTP

O Swagger é a referência para schemas e para as 150 operações individuais. Esta tabela localiza os controllers e suas dependências de alto nível sem duplicar o contrato gerado.

| Prefixo | Controller | Serviço/caso de uso principal | Tabelas centrais |
|---|---|---|---|
| `/api/v1/auth` | `AuthController` | autenticação/cadastro/refresh | `usuario`, `refresh_token` |
| `/api/v1/usuarios/me` | `PerfilUsuarioController` | `GerenciarPerfilUseCase` | `usuario`, `solicitacao_privacidade` e exportação transversal |
| `/api/v1/bancos`, `/contas` | `BancoController`, `ContaController` | bancos, contas, reconciliação e ajustes | `banco`, `conta`, `ajuste_saldo_conta` |
| `/api/v1/categorias`, `/itens`, `/meios-pagamento` | controllers homônimos | cadastros classificatórios | tabelas homônimas |
| `/api/v1/transacoes` | `TransacaoController` | `TransacaoUseCase` e histórico | `transacao`, `transacao_item`, `transacao_historico` |
| `/api/v1/transferencias` | `TransferenciaContaController` | `TransferenciaContaUseCase` | `transferencia_conta`, `transacao` |
| `/api/v1/cartoes` | `CartaoCreditoController`, `FaturaController` | cartão, ciclo e pagamento de fatura | `cartao_credito`, `fatura`, `pagamento_fatura`, `aplicacao_credito_fatura` |
| `/api/v1/compras-parceladas` | `CompraParceladaController` | `CompraParceladaUseCase` | `compra_parcelada`, `transacao`, `fatura` |
| `/api/v1/recorrencias`, `/previsoes` | controllers homônimos | ocorrências e projeções | `recorrencia`, `recorrencia_geracao`, `previsao_mensal` |
| `/api/v1/obrigacoes-financeiras` | `ObrigacaoFinanceiraController` | obrigações e liquidações | `obrigacao_financeira`, `pagamento_obrigacao` |
| `/api/v1/financiamentos` | controllers de financiamento/parcela | contratos, cronogramas e pagamentos | `financiamento`, parcelas e histórico |
| `/api/v1/investimentos` | três controllers de investimento | cadastro, movimento e posição | `investimento`, `movimento_investimento`, `posicao_investimento` |
| `/api/v1/divisoes-compartilhadas` | `DivisaoCompartilhadaController` | divisão, responsabilidade, pagamento e reembolso | tabelas de divisão, vínculo, responsabilidade, alocação e reembolso |
| `/api/v1/importacoes-financeiras` | `ImportacaoFinanceiraController` | leitura, revisão e confirmação | importação, lançamento e revisão |
| `/api/v1/dashboard` | dois controllers de dashboard | consultas mensais, anuais, agenda e patrimônio | leituras agregadas |

Todos os grupos, exceto login, refresh e health, exigem JWT. Cadastro e desbloqueio exigem, respectivamente, `USUARIO_CADASTRAR` e `USUARIO_DESBLOQUEAR`; Swagger/OpenAPI exige `DOCUMENTACAO_API_LER`; os endpoints Actuator diferentes de health exigem `OBSERVABILIDADE_LER`. Os recursos financeiros continuam autorizados por propriedade e participação.

`POST /api/v1/usuarios/{usuarioId}/desbloquear` remove o bloqueio persistente causado por cinco logins inválidos, zera o contador e revoga sessões anteriores. Retorna `204` e exige `USUARIO_DESBLOQUEAR`.

## Headers e parâmetros transversais

| Elemento | Uso |
|---|---|
| `Authorization: Bearer` | Obrigatório nas rotas protegidas. |
| `Idempotency-Key` | Obrigatório em transferências, ajustes de saldo e pagamento de fatura; deve ser reutilizado apenas com o mesmo payload. |
| `pagina`, `tamanho` | Paginação iniciada em zero; tamanho validado entre 1 e 100. |
| `Content-Type: multipart/form-data` | Início da importação financeira com arquivo PDF ou CSV e metadados. |

## Respostas e erros

| Situação | Status | Código típico |
|---|---:|---|
| Bean Validation/parâmetro inválido | 400 | `error.validacao` |
| Token ausente ou inválido | 401 | `error.auth.unauthorized` |
| Login ou refresh inválido, inclusive conta bloqueada | 401 | `error.auth.invalid` |
| Autenticado sem permissão | 403 | `error.auth.forbidden` |
| Excesso de login/refresh pelo mesmo endereço remoto | 429 | `error.auth.rate-limit` |
| Conflito de versão, lock ou idempotência | 409 | `error.conflito.atualizacao` ou código específico |
| Regra de domínio | 422 | chave de `messages.properties` |

O corpo usa `application/problem+json`, `type=urn:finisus:problem:{code}`, `instance`, `detail` e a propriedade estável `code`. O cliente não deve tomar decisões pela mensagem traduzida.

Em `/api/v1/bancos`, bancos pessoais são administrados somente por seu proprietário. Bancos do sistema aparecem nas consultas autenticadas, mas `PATCH` e `DELETE` são recusados para usuários comuns. O produto não expõe papel ou rota administrativa para alterar esse catálogo global.

Na recorrência, `POST /api/v1/recorrencias/geracoes/{YYYY-MM}` gera ocorrências pendentes sem alterar o saldo. As ocorrências podem ser consultadas em `GET /api/v1/recorrencias/ocorrencias/{YYYY-MM}` e somente `POST /api/v1/recorrencias/ocorrencias/{id}/realizar` cria a transação e movimenta a conta. A resposta da geração mensal passou de transações para ocorrências.

Algumas operações possuem comandos próprios para tornar a intenção explícita, como fechar, pagar ou estornar o pagamento de uma fatura, associar ou remover uma transação de uma divisão, refinanciar uma parcela e estornar um movimento de investimento. As divisões compartilhadas ficam em `/api/v1/divisoes-compartilhadas`: participantes vinculam transações de saída com efeito efetivo no saldo e consultam o resumo por período. `GET /{divisaoId}/participantes/historico` retorna as vigências de participação para um participante autorizado. `GET /{divisaoId}/transacoes/{transacaoId}/alocacoes` mostra o histórico dos pagamentos ancorados em transações reais; a associação inicial cria uma alocação com a base compartilhada sem duplicar caixa. O criador pode substituir a composição ativa em `PUT /{divisaoId}/transacoes/{transacaoId}/alocacoes`, informando uma ou mais transações e valores. A soma pode ser parcial, mas não pode superar a base compartilhada, e o total usado de cada transação entre despesas não pode superar seu valor; a composição anterior é cancelada logicamente. Uma alocação ativa pode ser cancelada individualmente em `DELETE /{divisaoId}/transacoes/{transacaoId}/alocacoes/{alocacaoId}`; o registro permanece no histórico com `canceladaEm` e `canceladaPor`. `GET /{divisaoId}/transacoes/{transacaoId}/pagamento` retorna `baseCompartilhada`, `pago`, `pendente`, o status `PENDENTE`, `PARCIAL` ou `QUITADO` e as alocações ativas válidas. O resumo calcula `pago` pelas alocações ativas, não pelo pagador original da despesa. `percentual` pode ser omitido para todos os participantes, caso em que cada nova associação usa divisão igual; não é aceita uma lista que misture percentuais preenchidos e ausentes. Em `POST /{divisaoId}/transacoes`, `baseCompartilhada` é opcional e limita quanto da transação entra na divisão; quando ausente, usa o valor integral. O campo opcional `responsabilidades` permite substituir a regra do grupo naquela despesa: envie para cada participante somente `percentual`, com soma 100%, ou somente `valorDevido`, com soma igual à base compartilhada. A associação congela a base, os percentuais efetivos e os valores devidos, usando maiores restos e o menor ID de usuário como desempate; alterações posteriores do grupo não reescrevem o histórico. Consulte o Swagger antes de integrar esses fluxos, pois eles podem exigir condições de estado.

Reembolsos são registrados em `POST /{divisaoId}/reembolsos`, com `transacaoId`, `recebedorId` e `valor`, consultados em `GET /{divisaoId}/reembolsos` e cancelados pelo criador em `DELETE /{divisaoId}/reembolsos/{reembolsoId}`. A transação precisa ser uma saída ativa e com efeito em caixa pertencente ao usuário autenticado; o recebedor deve ser outro participante. O valor fica limitado ao menor valor entre o débito acumulado do pagador e o crédito acumulado do recebedor até a data da transação. O reembolso ajusta os saldos desse período sem duplicar a despesa compartilhada. O cancelamento é lógico, preserva instante e usuário responsável no histórico e libera a transação para um novo registro válido.

`POST /api/v1/cartoes/faturas/processar-ciclos` recebe `dataReferencia`, fecha as faturas abertas cuja data de fechamento já foi alcançada e cria a próxima competência quando necessário. A resposta separa `fechadas` e `criadas`. A operação processa competências atrasadas até alcançar uma fatura futura e é idempotente quanto à competência existente; cartões inativos têm faturas vencidas fechadas, mas não recebem uma nova fatura.

`POST /api/v1/cartoes/faturas/{faturaId}/pagar` exige o cabeçalho `Idempotency-Key`. O corpo recebe `dataPagamento` e, opcionalmente, `valor` e `contaId`; sem valor, quita o saldo em aberto, e sem conta usa a conta de pagamento da fatura. Um pagamento inferior mantém a fatura `FECHADA`; ao alcançar o total ela passa para `PAGA`. Excesso é registrado como crédito da fatura. Ao fechar competências posteriores do mesmo cartão, o crédito disponível mais antigo é aplicado automaticamente antes de exigir novo pagamento, sem produzir outra movimentação de caixa. O detalhe separa `valorPago`, `creditoAplicado`, `valorEmAberto` e o eventual `credito`. Repetir a mesma chave e conteúdo não gera novo débito; reutilizar a chave com conteúdo diferente é recusado.

`POST /api/v1/compras-parceladas` aceita `cartaoId` opcional. Sem ele, agenda as parcelas por competência. Com ele, valida o cartão do usuário, cria ou reutiliza uma fatura aberta para cada parcela e considera o dia de fechamento: compras posteriores ao corte entram na competência seguinte. As parcelas permanecem vinculadas à compra para cancelamento e também à respectiva fatura para totalização e pagamento; elas não movimentam o saldo da conta antes do pagamento da fatura. O campo `cartaoId` também é devolvido nas respostas.

## Pesquisa de transações

`GET /api/v1/transacoes` permanece paginado e aceita `pagina` (inicia em zero) e `tamanho` (de 1 a 100). Os filtros opcionais são `mes` no formato `YYYY-MM`, `tipo` (`ENTRADA` ou `SAIDA`) e `categoriaId`. Os filtros são combinados quando informados e retornam somente transações do usuário autenticado, mantendo o contrato `PaginaResponse`.

`PATCH /api/v1/transacoes/{transacaoId}` exige, além dos dados corrigidos, o campo textual `motivo` com até 500 caracteres. Cada correção recebe uma correlação UUID gerada pelo servidor. `GET /api/v1/transacoes/{transacaoId}/historico` expõe `motivo`, `correlacaoId`, `snapshotAnterior` e `snapshotNovo`; esses campos podem ser nulos em eventos que não representam correção.

`PUT /api/v1/transacoes/{transacaoId}/itens` substitui a composição de itens sem alterar valor, conta, data, saldo ou origem da transação. O corpo exige `itens` não vazio e `motivo`; a soma dos valores deve ser exatamente o valor já registrado. A operação aceita transações comuns, gastos diretamente vinculados à fatura e ocorrências de recorrência, mas recusa pagamentos de fatura, parcelas de compra, transferências, liquidações e itens com compartilhamento ativo. O evento `DETALHAMENTO_ITENS` preserva os snapshots anterior e novo no histórico.

Cada linha de item aceita `itemId` opcional, `descricao`, `quantidade` opcional positiva, `valor` e `categoriaId` opcional. Quando `itemId` é informado, o catálogo pertence ao usuário e fornece os valores padrão de descrição e categoria; valores explicitamente enviados ficam congelados na linha. Sem `itemId`, `descricao` é obrigatória e a linha continua válida como snapshot independente do catálogo. Esse formato também é usado nos gastos de cartão e permite que uma importação preserve a descrição original mesmo quando não existe item catalogado.

Categorias possuem hierarquia limitada a cinco níveis. `DELETE /api/v1/categorias/{categoriaId}` realiza inativação lógica e retorna erro de negócio enquanto existir qualquer descendente ativo. Categorias inativas permanecem nas referências históricas, mas são recusadas em novos lançamentos e classificações.

## Transferências entre contas

`POST /api/v1/transferencias` move um valor positivo entre duas contas ativas e distintas do usuário autenticado. O corpo recebe `contaOrigemId`, `contaDestinoId`, `valor`, `data` e `descricao`; o cabeçalho obrigatório `Idempotency-Key`, de até 100 caracteres, identifica a operação. Repetir a mesma chave com o mesmo conteúdo retorna a transferência existente sem alterar novamente os saldos; reutilizá-la com conteúdo diferente é recusado.

Cada transferência cria uma saída na origem e uma entrada no destino, vinculadas ao mesmo agregado. Esses lançamentos ficam disponíveis na pesquisa de transações, mas não compõem receitas, despesas nem linhas operacionais dos dashboards. Eles não podem ser corrigidos ou estornados pelo recurso genérico de transações.

`GET /api/v1/transferencias/{transferenciaId}` consulta a operação e `POST /api/v1/transferencias/{transferenciaId}/estornar` estorna os dois lançamentos e recompõe ambos os saldos atomicamente. Repetir o estorno retorna o estado já estornado sem produzir novo efeito financeiro.

## Reconciliação de saldo

`GET /api/v1/contas/{contaId}/reconciliacao` compara o saldo materializado da conta com o saldo calculado a partir dos movimentos eficazes. A resposta contém `saldoMaterializado`, `saldoCalculado`, `divergencia`, `quantidadeMovimentos` e `conciliado`.

O cálculo ignora transações estornadas e lançamentos que não movimentam saldo no momento do registro, como compras de cartão e parcelas programadas. Transferências, pagamentos de fatura e demais movimentos efetivos permanecem no cálculo. Ajustes patrimoniais anteriores também compõem o saldo calculado e sua quantidade é informada separadamente.

`POST /api/v1/contas/{contaId}/ajustes-saldo` recebe `saldoInformado` e `motivo`, além do cabeçalho obrigatório `Idempotency-Key`. A operação registra saldo anterior, saldo calculado anterior, saldo informado, diferença, autor e data, e então atualiza o saldo materializado. Ela não cria uma transação operacional e, portanto, não produz receita ou despesa no dashboard. Repetir a mesma chave e conteúdo retorna o ajuste existente; reutilizar a chave com dados diferentes é recusado.

`GET /api/v1/contas/{contaId}/ajustes-saldo` lista o histórico de ajustes com paginação por `pagina` e `tamanho`.

## Dashboard financeiro

`GET /api/v1/dashboard/mensal?anoMes=YYYY-MM` retorna receitas, gastos diretos, gastos de faturas, valores pagos e em aberto das faturas, resultados por competência e por caixa, além do agrupamento por categoria. Compras de cartão são agrupadas pelo mês de referência da fatura; o pagamento permanece identificado separadamente. Compras parceladas programadas compõem a competência, mas não o caixa realizado.

`GET /api/v1/dashboard/resumo?mesFinal=YYYY-MM&periodoMeses=1|3|6|12` retorna os totais do intervalo e a composição por origem: `MOVIMENTACAO_DIRETA`, `RECORRENCIA`, `COMPRA_PARCELADA`, `CARTAO`, `PAGAMENTO_FATURA` e `INVESTIMENTO`. `despesas` e `resultadoCompetencia` incluem a compra de cartão no mês da fatura; `pagamentosFaturas` e `resultadoCaixa` usam a data efetiva do pagamento, sem contar a mesma compra duas vezes. Aportes e resgates aparecem como origem patrimonial `INVESTIMENTO`: não aumentam receitas ou despesas operacionais, mas preservam seu efeito no caixa. Compras parceladas programadas continuam visíveis na competência e ficam fora do caixa realizado.

`GET /api/v1/dashboard/anual?ano=YYYY` retorna os 12 meses do ano, inclusive os meses sem lançamentos. Cada mês apresenta receitas, gastos diretos, gastos de fatura, pagamentos de fatura, resultado por competência e resultado de caixa; a resposta também traz os totais anuais.

`GET /api/v1/dashboard/anual/composicao?ano=YYYY` detalha o ano pelas origens técnicas e pelas categorias efetivamente usadas nas transações operacionais. Aportes e resgates não compõem as categorias de receita ou despesa e aparecem na origem patrimonial `INVESTIMENTO`, sem alterar o resultado por competência.

`GET /api/v1/dashboard/balancete` recebe `anoMes` obrigatório e filtros opcionais de `tipo`, `categoriaId` e `contaId`. A resposta possui linhas paginadas e totais separados para gastos diretos, gastos de fatura e pagamentos de fatura. As linhas mantêm aportes e resgates para conciliação; nos totais, eles ficam fora da competência operacional e permanecem refletidos no resultado de caixa.

`GET /api/v1/dashboard/balancete/anual?ano=YYYY` aplica a mesma estrutura paginada e os mesmos filtros opcionais ao ano inteiro. Compras de cartão pertencem ao ano da fatura; pagamentos de fatura pertencem à data efetiva do pagamento.

`GET /api/v1/dashboard/visao-geral?referencia=YYYY-MM-DD&janelaDias=1..90` concentra o saldo atual das contas, resultado operacional mensal, caixa do mês, compromissos, saldo livre, três maiores categorias de gasto e alertas. O valor comprometido considera tanto a janela futura quanto obrigações, faturas, parcelas de financiamento e compras parceladas ainda pendentes que já venceram; esses itens aparecem como alertas `VENCIDO`. `GET /api/v1/dashboard/agenda?inicio=YYYY-MM-DD&fim=YYYY-MM-DD` aceita no máximo 366 dias inclusivos, permanece restrito ao intervalo solicitado e lista obrigações, faturas, parcelas de financiamento, compras parceladas e recorrências ainda previstas, ordenadas pelo vencimento.

`GET /api/v1/dashboard/receitas-gastos?mesFinal=YYYY-MM&periodoMeses=1|3|6|12` entrega a análise operacional por categoria, sem tratar aportes ou resgates como consumo ou renda. `despesasFixasPrevistas` representa recorrências ativas de saída para o intervalo, não uma categoria com esse nome.

`GET /api/v1/dashboard/patrimonio?referencia=YYYY-MM-DD` separa saldo atual das contas, capital líquido aportado, posição de cada investimento, faturas, obrigações, parcelas e compras parceladas restantes. `parcelasFinanciamentoEmAberto` e `dividasECompromissos` mantêm o valor contratual total ainda programado. `principalFinanciamentosEmAberto` informa somente a amortização pendente, `jurosEncargosFinanciamentosFuturos` evidencia o custo futuro e `dividasPrincipais` é a dívida usada no cálculo de `patrimonioLiquido`. Contas inativas ainda entram no patrimônio enquanto conservarem saldo; investimentos inativos permanecem enquanto tiverem capital líquido ou posição registrada. Quando um investimento declara `contaCustodiaId`, sua posição é a fonte patrimonial e o saldo dessa conta não é somado novamente. A data seleciona a última posição manual de investimento aplicável, enquanto saldos de contas e dívidas são os valores atuais obtidos em `saldosEDividasConsultadosEm`. O campo `patrimonioHistoricoCompleto` permanece `false` até existirem snapshots ou reconstrução datada para todos esses componentes; portanto, uma referência passada não deve ser apresentada como fotografia histórica integral.

`GET /api/v1/dashboard/compartilhados?inicio=YYYY-MM-DD&fim=YYYY-MM-DD` aceita no máximo 366 dias inclusivos e agrega as divisões ativas do usuário. `aReceber` soma saldos positivos (`pago - devido`) e `aPagar` soma os negativos em valor absoluto; ambos representam compensação entre participantes, e não saldo de conta ou receita realizada. Cada participante informa `usuarioId`, `nomeExibicao`, `pago`, `devido` e `saldo`.

## Obrigações e posições de investimento

`POST /api/v1/obrigacoes-financeiras` cria uma conta a pagar com descrição, credor, valor, vencimento, conta de pagamento e categoria opcional. `GET /api/v1/obrigacoes-financeiras` é paginado e aceita `status`, `inicio` e `fim`; `GET /api/v1/obrigacoes-financeiras/{id}` consulta uma obrigação. `POST /{id}/pagar` recebe `dataPagamento` e os campos opcionais `valor`, `juros`, `encargos` e `desconto`. Sem `valor`, o pagamento usa o saldo pendente menos o desconto. O abatimento da dívida é `valor + desconto`, enquanto a saída de caixa é `valor + juros + encargos`; nenhum componente pode ser negativo e o abatimento não pode exceder o saldo. A resposta informa `valorPago` e `saldoPendente`.

`GET /api/v1/obrigacoes-financeiras/{id}/pagamentos` consulta o histórico e explicita valor base, juros, encargos, desconto, valor abatido e valor de caixa. `POST /{id}/pagamentos/{pagamentoId}/estornar` estorna somente o pagamento escolhido e recompõe o abatimento correspondente. Uma obrigação parcialmente paga não pode ser cancelada antes dos estornos.

Pagamentos são estornados pelo recurso responsável, nunca pelo endpoint genérico de transações. Para fatura, o estorno do pagamento ativo mais recente só reabre a fatura quando os pagamentos e créditos aplicados restantes não cobrem seu total. Um pagamento cujo crédito já foi consumido por outra competência não pode ser estornado sem antes desfazer essa cadeia. Obrigação e parcela voltam ao estado pendente ou vencido conforme a data operacional.

As respostas de parcelas de financiamento expõem `valor`, `principal`, `juros`, `encargos`, `saldoDevedorInicial` e `saldoDevedorFinal`. Parcelas criadas ou recalculadas possuem a decomposição completa.

As respostas de financiamento expõem `cronogramaVersao`, iniciada em `1` e incrementada quando uma correção de lançamento substitui o cronograma. Versões anteriores podem ser consultadas em `GET /api/v1/financiamentos/{financiamentoId}/cronogramas/{versao}`; a versão atual continua disponível pelo endpoint normal de parcelas.

`POST /api/v1/financiamentos/{financiamentoId}/refinanciar` recebe `parcelaId` e `novoFinanciamento` com os mesmos campos da criação. Antes de remover parcelas pendentes, o cronograma vigente é preservado na versão histórica e a versão do contrato de origem é incrementada. A resposta contém o contrato de origem finalizado, o novo contrato com `financiamentoOrigemId` e a quantidade de parcelas pendentes removidas. O endpoint anterior localizado na parcela permanece disponível para compatibilidade, preserva o cronograma da mesma forma, mas apenas encerra o contrato existente.

`POST /api/v1/financiamentos/{financiamentoId}/amortizar` recebe `valor`, `dataPagamento` e, preferencialmente, `modalidade`: `REDUZIR_PRESTACAO` mantém a quantidade de parcelas pendentes e reduz seus valores; `REDUZIR_PRAZO` escolhe o menor prazo cuja primeira prestação recalculada não ultrapassa a prestação vigente. A operação cria uma saída na conta do financiamento, preserva o cronograma vigente no histórico e recalcula somente as parcelas ainda pendentes. Para compatibilidade, clientes antigos ainda podem omitir `modalidade` e informar `numeroParcelasRestantes`; os dois campos não podem ser combinados. Na quitação, o saldo devedor chega a zero e nenhuma parcela pendente é recriada. A resposta informa a transação criada, os saldos devedores anterior e atual, o novo cronograma e sua quantidade de parcelas restantes.

Na criação e atualização de investimento, `contaOrigemId` é a conta debitada nos aportes e creditada nos resgates. O campo opcional `contaCustodiaId` identifica, sem ambiguidade, uma conta ativa do tipo `APLICACAO` que representa o mesmo ativo; ela deve ser diferente da origem e não pode estar vinculada a outro investimento. `GET /api/v1/investimentos/{investimentoId}/posicoes` lista as posições manuais paginadas, e `POST` registra `valor` e `dataReferencia`. Sem uma posição registrada, o painel usa apenas capital líquido aportado e não declara rentabilidade.

Movimentos de investimento aceitam `APORTE`, `RESGATE`, `RENDIMENTO_REALIZADO` e `TAXA`. Aporte e taxa geram saída na conta de origem; resgate e rendimento efetivamente realizado geram entrada. Somente aporte e resgate alteram o capital líquido aportado apresentado pelo patrimônio. Valorização não é inferida desses movimentos: deve ser informada como posição manual, com valor e data de referência observados.

## Importação de documentos financeiros

`POST /api/v1/importacoes-financeiras` recebe multipart com `bancoId`, `tipoDocumento`, o destino pretendido e a parte `arquivo`. Para `EXTRATO_CONTA`, informe somente `contaId`, que deve ser uma conta ativa do mesmo banco; para `COBRANCA`, informe somente uma conta ativa; para `FATURA_CARTAO`, informe somente `faturaId`. O tipo pretendido e o destino são persistidos desde o início. O formato é identificado pelo conteúdo: a assinatura `%PDF-` encaminha o arquivo aos leitores PDF e os demais conteúdos válidos ao parser CSV. A resposta retorna uma prévia ainda não registrada. Quando o leitor detecta tipo diferente do informado, `divergencias` contém o código `TIPO_DOCUMENTO_DIVERGENTE`, com valores esperado e detectado, para revisão humana. O reenvio do mesmo conteúdo pelo mesmo usuário e banco reaproveita a importação existente somente quando tipo e destino coincidem; outro contexto é recusado. Use `PUT /api/v1/importacoes-financeiras/{id}/revisao` para corrigir/remover linhas e associar categoria ou item. Cada linha exige `justificativa`, pode ser ignorada com `importar=false`, criada com `importar=true` sem vínculo, ou associada informando `transacaoId` para extratos/faturas e `obrigacaoFinanceiraId` para cobranças. Os vínculos são opcionais e mutuamente exclusivos. A transação associada deve estar ativa, pertencer ao usuário e ao destino revisado e ter o mesmo tipo, data e valor da linha. A resposta mantém `conteudoOriginal`, `dataOriginal`, `descricaoOriginal`, `valorOriginal` e `tipoOriginal` separados dos campos revisados e apresenta o histórico `revisoes`, com decisão derivada, motivo da incerteza, justificativa, autor, instante e snapshots anterior/novo. Possíveis duplicidades retornam `nivel` (`EXATA` ou `PROVAVEL`) e `evidencias`; os candidatos possuem mesmo usuário, destino, tipo e valor dentro de uma janela de três dias. Data e descrição iguais classificam a candidata como exata, enquanto diferença em uma delas resulta em provável. Transações, pontas de transferência e obrigações são identificadas pelos respectivos IDs. A semelhança apenas auxilia a revisão e nunca ignora automaticamente duas ocorrências legítimas.

Os layouts CSV reconhecidos são UTF-8, aceitam BOM e usam `;` como delimitador. O layout de fatura exige os cabeçalhos `Data de Compra`, `Descrição` e `Valor (em R$)`; valor positivo representa gasto e valor negativo representa crédito. O extrato Sicredi só é selecionado quando o banco informado corresponde ao código 748 ou ao nome Sicredi e exige `Data`, `Descricao`, `CodTransacao`, `Identificador`, `Tipo`, `Valor` e `Saldo`; `CREDITO` deve ter valor positivo e `DEBITO`, valor negativo. Cabeçalhos ignoram diferenças de maiúsculas, acentos e espaços, mas as colunas obrigatórias devem estar presentes. O parser aceita campos entre aspas e aspas escapadas, limita o documento a 10.000 registros físicos, 64 colunas e 4.096 caracteres por campo. O conteúdo original guardado por linha é limitado a 1.000 caracteres e a descrição normalizada, a 500. Estrutura, codificação, datas e valores inválidos, bem como layouts desconhecidos ou reconhecidos por mais de um leitor, são recusados.

Nos PDFs, há leitores dedicados para fatura Sicredi, fatura e extrato Nubank e extrato C6. O leitor C6 exige que o banco informado corresponda ao C6 e que o conteúdo apresente o período e os lançamentos esperados. Outros PDFs podem ser aceitos pelo leitor genérico quando seguirem seu formato, mas isso não garante compatibilidade com todo documento emitido pela instituição. A Cresol ainda não possui perfil dedicado ou compatibilidade garantida; é necessária uma amostra de cada layout pretendido, sem dados reais nos testes, antes de documentá-lo como suportado.

Além do limite HTTP de 10 MB, a leitura de PDF aceita por padrão até 100 páginas, dois milhões de caracteres extraídos, cinco segundos de processamento cooperativo e 8 MB no cache principal do PDFBox. Ao ultrapassar a memória principal, o PDFBox usa cache temporário fechado junto com o documento; a aplicação não registra conteúdo, nome, descrição ou linhas do documento em logs. Os limites podem ser reduzidos por configuração do ambiente.

Somente `POST /api/v1/importacoes-financeiras/{id}/confirmar` persiste o resultado revisado: extratos geram transações na conta selecionada, cobranças geram obrigações a pagar e faturas geram gastos na fatura existente. Uma linha associada apenas confirma a conciliação e não cria outra movimentação. Ao importar extratos das duas contas de uma transferência própria, associe cada linha à respectiva ponta; ambas permanecem vinculadas ao mesmo `transferenciaId`, sem duplicar débitos ou créditos. A cobrança só produz saída de caixa quando a obrigação é paga. Arquivos inválidos ou não reconhecidos retornam `ProblemDetail` com orientação para reenviar um documento legível. PDF protegido não aceita senha pela API e é recusado. Um PDF válido sem camada de texto retorna o código estável `error.importacao.ocr.nao.suportado`; OCR de documentos digitalizados ainda não é realizado pelo backend.

Cada linha expõe `estado`: `PENDENTE` enquanto aguarda decisão ou criação, `IGNORADA` quando descartada, `ASSOCIADA` quando conciliada com um registro existente e `CRIADA` depois que a confirmação gera o registro financeiro. Confirmar novamente um lote já `CONFIRMADA` devolve o resultado persistido e não repete transações, gastos ou obrigações. A confirmação usa bloqueio pessimista do lote, de modo que requisições concorrentes são serializadas antes da verificação do estado.

Em faturas, linhas `SAIDA` criam gastos e linhas `ENTRADA` criam créditos vinculados à fatura. Créditos reduzem o total líquido e não são convertidos em saída negativa.

## Convenções

Coleções paginadas usam offset com `pagina` iniciando em zero e `tamanho` entre 1 e 100. As respostas não expõem entidades JPA e as entradas são validadas na borda.

Falhas de validação, autenticação, autorização, regra de negócio e concorrência retornam `ProblemDetail`. Em especial, regras de estado inválido retornam `422` e conflitos de versão ou lock retornam `409`. Todas essas respostas incluem `code`, identificador estável como `error.recurso.nao.encontrado`, e `type` no formato `urn:finisus:problem:{code}`. Clientes devem decidir o tratamento pelo `status` e pelo `code`; `detail` é uma mensagem humana traduzível e não constitui identificador de contrato.

## Evolução do contrato

Clientes devem tratar campos adicionais em respostas como compatíveis e não inferir regras a partir da estrutura interna de persistência. Mudanças incompatíveis de contrato devem ser avaliadas e documentadas antes da publicação.
