# Privacidade e ciclo de vida dos dados

Este documento registra o comportamento técnico existente. Ele não define bases legais nem prazos jurídicos de retenção: essas decisões dependem do controlador, da finalidade real do tratamento e das obrigações aplicáveis à operação.

## Inventário técnico

| Categoria | Exemplos | Vínculo e ciclo de vida atuais |
|---|---|---|
| Identidade e acesso | nome, e-mail, hash de senha, estado da conta, versão da sessão e refresh tokens | O perfil permanece identificado. Refresh tokens podem ser invalidados e expiram conforme a configuração de autenticação. Senhas e tokens não são devolvidos pelas APIs de perfil. |
| Organização financeira | bancos pessoais, contas, categorias, meios de pagamento e itens | Pertencem ao usuário. Parte dos cadastros usa inativação para preservar referências históricas. |
| Movimentação e planejamento | transações, itens, transferências, ajustes, recorrências, previsões, cartões, faturas, compras, financiamentos, obrigações, investimentos e posições | Registros financeiros mantêm referências e eventos necessários à conciliação. Estornos, cancelamentos e correções preservam histórico em vez de apagar fatos financeiros. |
| Compartilhamento | despesas, rateios, divisões, participantes, responsabilidades, alocações e reembolsos | Pode referenciar mais de um usuário. Histórico, vigências e responsabilidades congeladas não podem ser removidos isoladamente sem alterar o demonstrativo dos demais participantes. |
| Importação | metadados do lote, conteúdo e revisão das linhas importadas, associações e hashes de idempotência | O conteúdo extraído é persistido para revisão e auditoria do lote. O arquivo PDF não é armazenado pelo leitor nem registrado em logs. |
| Auditoria técnica | históricos, motivos, snapshots, correlações, autoria e instantes | Preservado para rastreabilidade das correções e operações financeiras. Logs e métricas não devem receber conteúdo financeiro nem identificadores pessoais como labels. |

## Desativação disponível hoje

`DELETE /api/v1/usuarios/me` executa desativação lógica. A operação:

- marca o usuário como inativo;
- incrementa a versão de sessão, invalidando access tokens emitidos com a versão anterior;
- invalida todos os refresh tokens do usuário;
- preserva nome, e-mail, hash de senha, dados financeiros, trilhas de auditoria e histórico compartilhado.

Portanto, essa rota não representa exclusão definitiva nem anonimização. O comportamento é intencional enquanto não existir uma política aprovada capaz de separar dados removíveis de registros cuja alteração afetaria integridade financeira ou direitos de outros participantes.

## Direitos técnicos disponíveis

- `GET /api/v1/usuarios/me/dados` produz um JSON versionado e autenticado com o perfil, cadastros, movimentações, planejamentos, históricos financeiros, importações e participações compartilhadas relacionadas ao titular;
- a exportação omite hash de senha, refresh tokens e hashes técnicos de idempotência;
- `POST /api/v1/usuarios/me/solicitacoes-anonimizacao` registra uma solicitação auditável. Uma nova chamada enquanto houver solicitação `SOLICITADA` ou `EM_ANALISE` devolve o mesmo registro, sem criar duplicidade;
- `GET /api/v1/usuarios/me/solicitacoes-privacidade` permite acompanhar as solicitações do próprio titular;
- a tabela `solicitacao_privacidade` preserva motivo, estado, datas, observação de conclusão e versão concorrente da solicitação.

O registro de solicitação não executa anonimização automaticamente. Essa separação impede que uma chamada destrutiva elimine dados necessários a obrigações financeiras ou ao histórico de outros participantes antes da análise aplicável.

## Decisões operacionais ainda necessárias

- anonimização seletiva compatível com autoria, rateios e histórico compartilhado;
- prazos automáticos de retenção e descarte por categoria;
- registro da base legal, finalidade, controlador, operadores e canal de atendimento.

Antes de automatizar essas decisões, a operação do produto deve aprovar a matriz de finalidade, base legal, retenção e exceções por categoria. A anonimização deverá manter valores, responsabilidades e eventos necessários ao demonstrativo compartilhado, substituindo somente identificadores cuja remoção seja permitida. Até essa aprovação, pedidos permanecem registrados para análise e não há descarte automático.
