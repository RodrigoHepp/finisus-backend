# Validações e cenários para QA

[Voltar ao portal](00-README.md) · [Requisitos](03-requisitos.md) · [Regras](04-regras-negocio.md)

✅ Condições extraídas de entrada, domínio e serviços. Mensagens literais abaixo foram encontradas no catálogo do backend; sua apresentação visual não foi validada. Quando não há literal transcrito, descreve-se o resultado sem inventar uma mensagem ao usuário.

| ID | Campo ou situação | Condição | Motivo | Comportamento | Mensagem conhecida/limite | Funcionalidade | Regra |
|---|---|---|---|---|---|---|---|
| VAL-001 | Cadastro | Nome/e-mail/senha obrigatórios; nome até 150; senha 8–128 | Impedir cadastro incompleto | Recusar entrada inválida | Mensagem por campo; não presumir texto da interface | FUNC-001 | RN-001 |
| VAL-002 | E-mail | E-mail normalizado já cadastrado | Evitar identidade duplicada | Recusar cadastro/alteração conflitante | Consultar catálogo de mensagens | FUNC-001 | RN-001 |
| VAL-003 | Acesso | Senha/token inválido ou usuário inativo | Restringir dados ao titular | Não autenticar | Mensagem de autenticação conforme contrato | FUNC-001, 025 | RN-001, RN-017 |
| VAL-004 | Propriedade | Recurso não pertence ao titular e não há participação autorizada | Isolamento dos dados | Recusar acesso | Erro de recurso/acesso conforme operação | FUNC-001–025 | RN-001 |
| VAL-005 | Conta | Física com banco; não física sem banco | Consistência do cadastro | Recusar | Conta física não pode ter banco. / Contas não físicas exigem banco. | FUNC-002 | RN-019 |
| VAL-006 | Conta inativa | Novo lançamento usa conta inativa | Preservar arquivamento | Recusar nos fluxos de lançamento que exigem ativa | Esta conta está inativa para novos lançamentos. | FUNC-004, 007, 009–016 | RN-019 |
| VAL-007 | Saldo | Operação subtrai mais que saldo disponível | Modelo não admite saldo negativo | Recusar débito ou reversão incompatível | Erro de valor negativo; não há promessa de texto específico de saldo insuficiente | FUNC-004, 007, 009–016 | RN-019 |
| VAL-008 | Categoria | Ciclo, pai inativo ou mais de cinco níveis | Hierarquia coerente | Recusar criação/movimentação | Erro específico da categoria | FUNC-003 | RN-006 |
| VAL-009 | Categoria | Há descendente ativo ao inativar pai | Preservar dependências | Recusar até inativar descendentes | Erro de descendente ativo | FUNC-003 | RN-006 |
| VAL-010 | Classificação | Categoria/item inativo em novo uso | Não usar referência arquivada | Recusar novo uso | Erro de categoria/item inativo | FUNC-003–004, 006, 011, 022 | RN-006, RN-020 |
| VAL-011 | Itens | Soma diferente do total | Explicar integralmente transação | Recusar | Erro de soma dos itens | FUNC-004, 006, 011 | RN-020 |
| VAL-012 | Linha livre | Sem catálogo e sem descrição; quantidade informada não positiva | Identificar composição | Recusar | Erro da composição da linha | FUNC-004, 006 | RN-020 |
| VAL-013 | Correção/detalhamento | Motivo ausente ou vazio | Manter justificativa | Recusar | Erro de motivo/dados obrigatórios | FUNC-005–006 | RN-005 |
| VAL-014 | Origem da transação | Origem vinculada impede comando genérico | Manter coerência com pagamento/origem | Recusar e exigir fluxo compatível | Erro de origem imutável | FUNC-005–006 | RN-021 |
| VAL-015 | Compartilhamento de item | Itens com compartilhamento ativo são substituídos | Preservar responsabilidade histórica | Recusar detalhamento | Erro de compartilhamento ativo | FUNC-006, 018 | RN-021 |
| VAL-016 | Transferência | Mesma conta; conta alheia/inativa; valor não positivo | Garantir duas pontas próprias válidas | Recusar | Erro de transferência/recurso | FUNC-007 | RN-003 |
| VAL-017 | Chave de repetição | Chave usada com outro conteúdo | Evitar reutilização ambígua | Recusar transferência, ajuste ou pagamento de fatura conflitante | Erro de idempotência conflitante | FUNC-007–008, 012 | RN-003, RN-004, RN-009 |
| VAL-018 | Ajuste | Saldo informado negativo ou motivo/chave ausentes | Registrar correção explicável | Recusar | Erro de ajuste inválido | FUNC-008 | RN-004 |
| VAL-019 | Obrigação | Base + desconto maior que pendente; abatimento ou caixa zero | Separar quitação e desembolso válidos | Recusar | Erro de pagamento inválido | FUNC-009 | RN-010 |
| VAL-020 | Cancelamento de obrigação | Existe valor já abatido | Não apagar liquidação ativa | Recusar até estornar pagamentos | Erro de cancelamento inválido | FUNC-009 | RN-022 |
| VAL-021 | Geração mensal | Competência futura | Separar previsão de ocorrência gerada | Recusar geração | Erro de período futuro | FUNC-010 | RN-023 |
| VAL-022 | Realização | Ocorrência já realizada | Evitar segunda baixa | Recusar sem novo efeito | A ocorrência da recorrência já foi realizada. | FUNC-010 | RN-007 |
| VAL-023 | Cartão/fatura | Cartão inativo ou fatura não aberta ao lançar gasto | Respeitar ciclo | Recusar | Erro de cartão inativo/fatura fechada | FUNC-011, 013 | RN-024 |
| VAL-024 | Limite | Gasto/parcela mais total verificado da fatura supera limite | Controlar limite configurado | Recusar | Erro de limite excedido | FUNC-011, 013 | RN-024 |
| VAL-025 | Pagar fatura | Estado diferente de fechada ou saldo aberto não positivo | Liquidar dívida existente | Recusar novo pagamento | Erro de transição/pagamento | FUNC-012 | RN-009 |
| VAL-026 | Estornar fatura | Crédito da origem já aplicado em outra competência | Preservar encadeamento do crédito | Recusar estorno isolado | Erro do crédito utilizado | FUNC-012 | RN-009 |
| VAL-027 | Compra | Quantidade de parcelas menor que um | Definir distribuição válida | Recusar | Erro de quantidade inválida | FUNC-013 | RN-025 |
| VAL-028 | Financiamento | Recálculo por erro com paga ou exclusão da única parcela | Preservar pagamentos e cronograma válido | Recusar | Erro de recálculo | FUNC-014–015 | RN-011 |
| VAL-029 | Amortização | Valor excede saldo ou modalidade combinada com quantidade legada | Reduzir dívida sem condições contraditórias | Recusar | Erro de amortização | FUNC-015 | RN-027 |
| VAL-030 | Custódia | Não é aplicação ativa própria/distinta ou já vinculada | Evitar referência inválida/dupla representação | Recusar | Erro de custódia | FUNC-016 | RN-026 |
| VAL-031 | Investimento | Inativo para novo movimento/posição; estorno repetido | Respeitar ciclo | Recusar | Erro de investimento inativo/movimento já estornado | FUNC-016–017 | RN-012 |
| VAL-032 | Rateio | Novo externo, sem opt-in ou responsabilidades não fecham base | Garantir participante e acordo válido | Recusar | Erro de externo/opt-in/total | FUNC-018 | RN-028 |
| VAL-033 | Rateio pago | Solicitação de marcar pago sem transação | Impedir quitação sem caixa comprovado | Sempre recusar no fluxo atual | Não é permitido marcar um rateio como pago sem uma transação financeira vinculada. | FUNC-019 | RN-029 |
| VAL-034 | Divisão | Criador fora do grupo; percentuais incompletos ou diferentes de 100% | Definir responsabilidade integral | Recusar | Erro de participantes/responsabilidades | FUNC-020 | RN-030 |
| VAL-035 | Base/alocação | Base excede saída; pagamentos excedem base ou transação disponível | Não usar valor além do real | Recusar | Erro de base/alocação | FUNC-020 | RN-030 |
| VAL-036 | Reembolso | Sem dívida/crédito suficiente ou saída própria elegível | Comprovar compensação | Recusar | Erro de reembolso/transação/saldo | FUNC-021 | RN-031 |
| VAL-037 | Migração | Rateio pago/recusado/cancelado ou mapeamento incompleto | Preservar origem e identidade | Recusar | Erro de migração/mapeamento | FUNC-021 | RN-029 |
| VAL-038 | PDF | Inválido, vazio ou acima de 10 MB | Limitar e reconhecer entrada | Recusar antes de criar efeitos | Envie um arquivo PDF válido, não vazio e de até 10 MB. | FUNC-022 | RN-032 |
| VAL-039 | PDF sem texto | Exige OCR | Evitar prévia sem interpretação sustentada | Recusar leitura | Este PDF não possui uma camada de texto legível. OCR ainda não é suportado; envie o PDF original com texto selecionável. | FUNC-022 | RN-032 |
| VAL-040 | Destino da importação | Extrato com conta de outro banco ou conta/fatura incompatível | Aplicar documento no contexto correto | Recusar | A conta de destino deve pertencer ao banco informado para o extrato. | FUNC-022 | RN-032 |
| VAL-041 | Revisão | Justificativa ausente; selecionados incompletos; nenhuma linha selecionada | Exigir decisão verificável | Recusar revisão/confirmação | Informe a justificativa da decisão humana para cada linha revisada. / Selecione ao menos um lançamento para importar. | FUNC-022 | RN-015 |
| VAL-042 | Associação | Registro alheio, inativo ou sem correspondência exigida de tipo/data/valor | Não conciliar fatos diferentes | Recusar associação | Erro de revisão inválida | FUNC-022 | RN-015 |
| VAL-043 | Períodos | Resumo fora de 1/3/6/12; agenda acima de 366 dias inclusivos | Delimitar consulta | Recusar | Erro de período | FUNC-023 | RN-033 |
| VAL-044 | Previsão | Horizonte fora de 1–12 | Delimitar recálculo | Recusar | Erro de período da previsão | FUNC-024 | RN-034 |
| VAL-045 | Privacidade | Motivo vazio ou acima de 500 caracteres | Registrar solicitação compreensível | Recusar | Erro de solicitação inválida | FUNC-025 | RN-035 |

## Cenários de aceite por funcionalidade

CEN-001 a CEN-025 reproduzem os critérios de RF-001 a RF-025 com dados concretos. A pré-condição comum é usuário autorizado e referências válidas, exceto no cenário que testa sua ausência. Validar também ausência de efeito parcial na recusa; não executar operações sobre dados pessoais reais para homologar exemplos.

### CEN-001 — Acesso e perfil

Dado um operador autenticado com `USUARIO_CADASTRAR`, e-mail ainda não cadastrado e senha de 8 a 128 caracteres, quando houver cadastro válido, então criar usuário; quando e-mail já estiver em uso, recusar. Usuário inativo não autentica.

### CEN-002 — Bancos e contas

Dada conta física, quando houver banco informado, então recusar; conta não física sem banco também é recusada. Conta nova inicia com saldo zero.

### CEN-003 — Categorias, meios de pagamento e catálogo

Dada hierarquia de cinco níveis, quando se tentar incluir sexto nível, então recusar. Categoria com descendente ativo não pode ser inativada.

### CEN-004 — Registrar e pesquisar transações

Dada conta com R$ 500, quando registrar saída direta de R$ 100 válida, então saldo passa a R$ 400 e transação fica consultável; conta de outro titular não é aceita.

### CEN-005 — Corrigir e estornar transações

Dada saída manual elegível de R$ 100, quando corrigir para R$ 80 com motivo, então recompor R$ 20 e preservar antes/depois. Sem motivo, recusar.

### CEN-006 — Detalhar itens e consultar histórico

Dada transação de R$ 100, quando detalhar em R$ 60 e R$ 40 com motivo, então preservar saldo e total; composição de R$ 99 é recusada.

### CEN-007 — Transferências próprias

Dadas contas com R$ 500 e R$ 100, quando transferir R$ 200, então ficam R$ 300 e R$ 300; repetição com mesmo identificador e conteúdo não repete efeito.

### CEN-008 — Reconciliação e ajuste de saldo

Dada conta com saldo zero, quando ajustar para R$ 1.000 com motivo/chave, então saldo e histórico refletem o ajuste e não surge receita operacional.

### CEN-009 — Obrigações e pagamentos

Dada obrigação de R$ 100, quando pagar base de R$ 80, desconto de R$ 20, juros de R$ 5 e encargos de R$ 2, então abater R$ 100, debitar R$ 87 e marcar paga.

### CEN-010 — Recorrências e ocorrências

Dada recorrência ativa, quando gerar a mesma competência duas vezes, então existir uma ocorrência sem caixa; realizar uma vez movimenta saldo, e repetir realização retorna erro sem novo movimento.

### CEN-011 — Cartões e gastos de fatura

Dada fatura aberta e cartão ativo, quando registrar gasto de R$ 150 dentro do limite, então fatura recebe o gasto e saldo da conta não é debitado.

### CEN-012 — Fechar, pagar e estornar faturas

Dada fatura fechada de R$ 600, quando pagar R$ 200, então restam R$ 400 e o estado continua fechado; pagar mais R$ 450 deixa paga e gera crédito de R$ 50.

### CEN-013 — Compras parceladas

Dada compra de R$ 100 em três parcelas, quando criada, então distribuir R$ 33,33, R$ 33,33 e R$ 33,34, sem débito inicial.

### CEN-014 — Financiamentos e parcelas

Dada parcela pendente válida, quando pagar, então criar saída vinculada e marcar paga; quando estornar pagamento vinculado, então recompor conta e reabrir conforme vencimento.

### CEN-015 — Amortizar, refinanciar e corrigir cronograma

Dado cronograma com parcelas pagas e pendentes, quando amortizar validamente, então preservar pagas e versão anterior, e recalcular pendentes; correção por erro com parcela paga é bloqueada.

### CEN-016 — Investimentos e movimentos

Dado investimento ativo, quando aportar R$ 100, então reduzir conta de origem em R$ 100; quando registrar rendimento realizado de R$ 10, então aumentar conta em R$ 10 sem classificá-lo como aporte.

### CEN-017 — Posições de investimento

Dado investimento com aporte de R$ 100, quando registrar posição de R$ 110, então posição fica disponível sem entrada automática de R$ 10.

### CEN-018 — Compartilhamento por transação ou item

Dado item de R$ 100 com base compartilhada de R$ 40, quando responsabilidades fecharem R$ 40, então conservar R$ 60 individuais; base de R$ 101 é recusada.

### CEN-019 — Consentimento e resposta a rateio

Dado rateio pendente do usuário, quando aceitar, então mudar para aceito sem caixa; chamada de marcar pago deve ser recusada no comportamento atual.

### CEN-020 — Divisões, responsabilidades e alocações

Dada despesa de R$ 200 dividida igualmente entre dois usuários, paga por um deles, então devido é R$ 100 por pessoa e saldos são +R$ 100 e −R$ 100; mudar grupo não altera essa responsabilidade.

### CEN-021 — Reembolsos e migração de compartilhamento

Dado pagador devedor de R$ 100 e recebedor credor de R$ 100, quando vincular saída própria de R$ 100 como reembolso elegível, então registrar compensação sem outro débito; R$ 101 é recusado.

### CEN-022 — Importação e revisão de PDF

Dado PDF de extrato com linha já registrada, quando associá-la ao registro correspondente e confirmar, então não criar outro movimento; confirmar o lote novamente devolve o resultado existente.

### CEN-023 — Painéis, agenda e patrimônio

Dado ano sem movimentos, quando consultar resumo anual, então retornar doze meses; período de resumo de dois meses deve ser recusado. Não converter PO-001 a PO-004 em critérios aprovados de produto.

### CEN-024 — Previsão mensal de fluxo

Dado horizonte de zero ou treze meses, quando recalcular previsão, então recusar; horizonte válido começa no mês operacional e não modifica saldo de conta.

### CEN-025 — Exportação, pedido de anonimização e desativação

Dado titular autenticado, quando exportar, então entregar dados sem credenciais; quando pedir anonimização novamente com pedido aberto, então reutilizá-lo; desativação invalida acesso sem apagar dados.

## Cenários excepcionais e de fronteira

### CEN-026 — Repetição conflitante

**Dado que** uma transferência de R$ 50 foi registrada com identificador X, **quando** o mesmo identificador for enviado com R$ 60, **então** recusar sem outra movimentação. Relacionado a RF-007 e RN-003.

### CEN-027 — Dia inexistente no mês

**Dada** recorrência no dia 31, **quando** gerar competência de fevereiro, **então** usar seu último dia e deixar ocorrência pendente, sem alterar saldo. Relacionado a RF-010 e RN-023.

### CEN-028 — Cancelamento seletivo de compra

**Dada** compra com parcelas em fatura aberta e fechada, **quando** cancelar, **então** estornar somente as vinculadas à aberta. Sem cartão, somente parcelas com data posterior à operacional são elegíveis. Relacionado a RF-013 e RN-025.

### CEN-029 — Histórico de participantes

**Dada** despesa confirmada com responsabilidades de 50%/50%, **quando** alterar grupo para 60%/40%, **então** a despesa anterior conserva sua composição; nova despesa usa o acordo aplicável à sua associação. Relacionado a RF-020 e RN-013.

### CEN-030 — Centavos na divisão

**Dada** base de R$ 100 para três participantes iguais, **quando** materializar responsabilidades, **então** totalizar exatamente R$ 100, distribuindo residual por maiores restos e desempate pelo identificador do usuário. Não depender da ordem de envio. Relacionado a RF-020 e RN-030.

### CEN-031 — Desativação não é anonimização

**Dado** titular com transações e participações, **quando** desativar acesso, **então** invalidar suas sessões e conservar identidade/histórico. Pedido de anonimização é operação distinta e não demonstra remoção executada. Relacionado a RF-025 e RN-017.

### CEN-032 — Limites do PDF

**Dado** PDF com texto mas acima do limite configurado de páginas ou caracteres, **quando** importar, **então** recusar leitura sem movimentos financeiros. Documento composto por imagem deve retornar a limitação de OCR. Relacionado a RF-022 e RN-032.

### CEN-033 — Saldo insuficiente e reversão

**Dada** conta com R$ 10, **quando** registrar saída de R$ 20, **então** recusar sem persistir saldo negativo. **Dada** entrada antiga cujo dinheiro já foi consumido, **quando** estorná-la resultaria em saldo negativo, **então** a mesma restrição monetária pode impedir a reversão. Validar mensagem e orientação de negócio em PO-005. Relacionado a RN-019.

### CEN-034 — Consulta sem movimento

**Dado** período sem transações, **quando** consultar ano, **então** apresentar os doze meses; consultar não cria receitas, despesas nem fechamento mensal. Relacionado a RF-023.

## Cenários de investigação, não critérios aprovados

Os roteiros de PO-001 a PO-004, PO-007, PO-009 e PO-010 distinguem comportamento encontrado e decisão esperada. QA deve reproduzi-los e registrar a resposta antes de o PO estabelecer o comportamento-alvo. Eles não autorizam correção de código nesta entrega.
