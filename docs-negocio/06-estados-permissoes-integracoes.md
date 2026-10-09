# Estados, permissões e integrações

[Voltar ao portal](00-README.md) · [Regras](04-regras-negocio.md) · [Pendências](09-pendencias-po.md)

## Estados e transições

Os nomes entre parênteses permitem relacionar o termo funcional ao retorno do sistema. Estado persistido, situação calculada e ausência de comando são diferenciados. Não presumir que um valor declarado no modelo tenha transição pública disponível.

### Obrigação financeira

| Estado atual | Significado / ação possível | Evento | Próximo estado | Regra / impedimento |
|---|---|---|---|---|
| Em aberto (EM_ABERTO) | Dívida ainda não integralmente abatida; admite pagamento. | Processar vencimento anterior à referência | Vencida (VENCIDA) | RN-010; não altera caixa. |
| Em aberto ou vencida | Admite pagamento parcial. | Abatimento menor que pendente | Mesmo estado | RN-010; restante permanece devido. |
| Em aberto ou vencida | Admite quitação. | Abatimento igual ao pendente | Paga (PAGA) | RN-010; principal/juros/desconto separados. |
| Em aberto ou vencida | Pode ser cancelada se sem abatimento. | Cancelar | Cancelada (CANCELADA) | RN-022; pagamento prévio bloqueia. |
| Paga ou parcialmente abatida | Tem pagamento ativo reversível. | Estornar pagamento | Em aberto ou vencida | RN-022; depende do vencimento frente à data operacional. |
| Cancelada | Não aceita novo pagamento. | Tentar pagar | Sem transição | Não há reativação exposta. |

Paga conclui a liquidação; cancelada conclui o abandono autorizado. Não existe estado próprio “parcialmente paga”: o saldo pendente e o valor abatido expressam essa situação. Mudança para vencida depende do processamento, não se deve presumir atualização persistida a cada consulta.

### Fatura

| Estado atual | Significado / ação possível | Evento | Próximo estado | Regra / impedimento |
|---|---|---|---|---|
| Aberta (ABERTA) | Recebe gastos, permite alterar datas/conta e cancelar. | Fechar manualmente ou por processamento de ciclos | Fechada (FECHADA) | RN-024. Crédito suficiente aplicado no fechamento pode levar a paga. |
| Aberta | Ciclo abandonado. | Cancelar | Cancelada (CANCELADA) | RN-024; fechamento já ocorrido impede. |
| Fechada | Não recebe novos gastos; dívida liquidável. | Pagamento parcial | Fechada | RN-009. |
| Fechada | Dívida liquidável. | Pagamento suficiente | Paga (PAGA) | RN-009; excesso conserva crédito. |
| Paga | Pagamento ativo mais recente pode ser revertido se não bloqueado. | Estorno que deixa liquidação insuficiente | Fechada | RN-009; origem de crédito transportado bloqueia. |
| Fechada ou paga | Estorno de pagamento ativo permitido. | Estorno sem necessidade de reabertura | Conforme liquidação remanescente | Conferir pago/crédito/aberto; não garantir sempre fechada. |
| Cancelada | Não recebe gasto/pagamento pelo fluxo normal. | Tentar prosseguir | Sem transição | Sem reativação exposta. |

Paga significa quitação no controle, não autorização bancária. Não há estado parcial próprio.

### Recorrência e ocorrência

| Estado atual | Evento | Próximo estado | Significado / regra |
|---|---|---|---|
| Recorrência ativa | Gerar competência atual/anterior | Recorrência continua ativa; ocorrência pendente | RN-007/RN-023; nenhum caixa. |
| Recorrência ativa | Inativar cadastro | Inativa | Interrompe seleção em geração futura; não elimina ocorrências existentes. |
| Ocorrência pendente (PENDENTE) | Realizar | Realizada (REALIZADA) | Caixa na data operacional, com transação vinculada. |
| Ocorrência realizada | Realizar novamente | Sem transição; erro | Sem segundo débito/crédito. |
| Ocorrência realizada | Estornar sua transação | Ocorrência não é reaberta pelo serviço de transação | PO-007; não prometer nova realização. |

### Importação

| Estado atual | Evento | Próximo estado | Significado / regra |
|---|---|---|---|
| Não existe lote | Ler documento aceito | Pendente de revisão (PENDENTE_REVISAO) | Prévia sem caixa, RN-015. |
| Pendente de revisão | Revisar dados/decisões | Pendente de revisão | Justificativas e versões preservadas. |
| Pendente de revisão | Confirmar selecionados aptos | Confirmada (CONFIRMADA) | Registros criados/associados; RN-015. |
| Confirmada | Confirmar novamente | Confirmada | Mesmo resultado, sem repetição de efeitos. |
| Confirmada | Revisar | Sem transição; erro | Sem comando de reabertura/cancelamento do lote. |

**Linhas:** pendente (PENDENTE) aguarda resolução; ignorada (IGNORADA) está excluída da criação; associada (ASSOCIADA) aponta para fato existente; criada (CRIADA) aponta para novo fato gerado. As decisões de revisão determinam exclusão/associação; a confirmação materializa a criação. Não confundir estado da linha com estado do lote.

### Financiamento e parcelas

| Estado atual | Evento | Próximo estado | Significado / regra |
|---|---|---|---|
| Contrato ativo (ATIVO) | Cancelar sem parcela paga | Cancelado (CANCELADO) | RN-027; abandono sem liquidação anterior. |
| Contrato ativo | Refinanciar pelo fluxo disponível | Finalizado (FINALIZADO) na origem | RN-011; completo cria sucessor ativo, legado não cria. |
| Contrato ativo | Amortizar todo saldo devedor | Finalizado | RN-027; sem parcelas pendentes após quitação. |
| Parcela pendente (PENDENTE) | Processar atraso após vencimento | Atrasada (ATRASADA) | Vencimento deve ser anterior à referência. |
| Parcela pendente/atrasada | Pagar | Paga (PAGA) | Saída vinculada e baixa integral. |
| Parcela paga com vínculo | Estornar pagamento | Pendente ou atrasada | Conforme vencimento, com reversão da saída. |

A última parcela paga ordinariamente não tem finalização automática demonstrada do contrato no serviço de pagamento. Pagamento legado sem vínculo não permite inferir a transação a estornar. Ver PO-009.

### Compartilhamentos

| Objeto / estado atual | Evento | Próximo estado | Ação/limite |
|---|---|---|---|
| Despesa ativa (ATIVA) | Cancelar ou migrar validamente | Cancelada (CANCELADA) | Preserva origem e histórico. |
| Rateio pendente (PENDENTE) | Destinatário aceita | Aceito (ACEITO) | Apenas destinatário interno. |
| Rateio pendente | Destinatário recusa | Recusado (RECUSADO) | Não quita nem cria caixa. |
| Rateio não pago | Cancelar despesa | Cancelado (CANCELADO) | Rateio pago histórico não é cancelado por esse método. |
| Rateio aceito | Marcar pago pela operação exposta | Sem transição; erro | PAGO existe no modelo, mas o serviço atual bloqueia a ação. |
| Divisão ativa (ATIVA) | Inativar pelo criador | Inativa (INATIVA) | Novas associações e reembolsos exigem ativa; não presumir bloqueio uniforme de toda manutenção. |
| Fotografia pendente de revisão (PENDENTE_REVISAO) | Revisar responsabilidades explicitamente | Confirmada (CONFIRMADO) | Valores devem fechar base; já confirmada não é revisável pelo mesmo comando. |
| Vínculo/alocação/reembolso ativo | Cancelamento autorizado | Cancelado logicamente | Registro fica no histórico; não é estorno bancário. |

**Pagamento da despesa na divisão é calculado:** PENDENTE se sem pagamento considerado; PARCIAL se abaixo da base; QUITADO quando a base está coberta. Pode mudar após cancelamento/substituição de alocação. Essas situações não representam aceite do rateio antigo nem acerto integral entre todas as pessoas.

### Outros ciclos

| Objeto | Estados e conclusão |
|---|---|
| Transação | Ativa ou estornada por instante de estorno; repetição de estorno é recusada. |
| Transferência | ATIVA → ESTORNADA pelo agregado; duas pontas revertidas conjuntamente. |
| Compra parcelada | Ativa/cancelada pela data de cancelamento; parcelas inelegíveis ao cancelamento podem continuar. |
| Movimento de investimento | Ativo/estornado; estorno não fabrica um novo aporte ou resgate. |
| Cadastros | Ativo/inativo onde oferecido; arquivamento conserva referências e não elimina valor residual por si só. |
| Solicitação de privacidade | SOLICITADA ao registrar. EM_ANALISE, CONCLUIDA e RECUSADA são declarados, sem operação de transição pública identificada. |
| Usuário | Ativo → inativo; sessões revogadas. Sem reativação pública encontrada no inventário. |

## Perfis e permissões

Não há papel administrativo ou de consulta isolada identificado. Os papéis abaixo são posições na operação. Um titular pode exercer mais de uma delas.

| Funcionalidade | Público | Titular/proprietário | Criador da divisão | Participante não criador |
|---|---|---|---|---|
| Cadastro/login/renovação | Sim, com dados/credencial válidos | Sim | Igual aos demais | Igual aos demais |
| Perfil, exportação e pedido | Não | Apenas próprios | Apenas próprios | Apenas próprios |
| Contas, transações, obrigações, cartão, investimento | Não | Apenas próprios | Não ganha acesso geral a contas alheias | Apenas próprios |
| Banco global | Não | Consultar; não alterar/inativar | Mesma regra | Mesma regra |
| Criar divisão | Não | Sim, se também participante e demais condições válidas | — | Pode criar outra divisão |
| Consultar divisão/resumo/histórico | Não | Exige participação | Sim enquanto participante | Sim enquanto participante atual |
| Alterar participantes/inativar divisão | Não | Não pelo mero acesso autenticado | Sim | Não |
| Associar saída à divisão | Não | Exige participação | Saída própria ou de participante elegível | Somente saída própria |
| Cancelar vínculo e manter alocações | Não | Não pelo mero fato de pagar | Sim | Não |
| Revisar responsabilidade legada | Não | Exige comando autorizado | Sim | Não |
| Registrar reembolso | Não | Exige participação e saída própria | Sim se devedor elegível | Sim se devedor elegível |
| Cancelar reembolso | Não | Não por ser pagador apenas | Sim | Não |
| Manter compartilhamento legado | Não | Criador da despesa | Papel na divisão não basta | Não pela participação isolada |
| Consultar recebidos/responder rateio | Não | Apenas destinatário | Não ganha poder de responder por outro | Apenas próprio rateio |
| Alterar opt-in | Não | Próprio usuário | Próprio usuário | Próprio usuário |

Remover participante pode retirar seu acesso às consultas de divisão, mesmo que seu histórico permaneça na apuração. Necessidade de acesso histórico após saída deve ser validada em PO-006.

## Integrações sob a ótica do negócio

### INT-001 — Interface consumidora do Finisus

**Sistema:** aplicação que utiliza as operações do backend; a documentação existente cita cliente web. **Finalidade:** oferecer ao titular acesso aos registros e consultas.

**Quando é utilizada:** cadastro, entrada de informações, pagamentos registrados e consultas. **Por que é necessária:** o repositório analisado entrega serviços de backend, sem interface gráfica própria.

**Informação enviada:** credenciais na autenticação; dados de operações e identificadores nos fluxos autorizados. **Informação recebida:** registros, indicadores, resultados e erros.

**Impacto no processo e no objetivo final:** torna os recursos utilizáveis por uma pessoa e apresenta a composição financeira. **Indisponibilidade:** impede o uso por esse consumidor; não é evidência de perda dos registros. Retentativas precisam respeitar as regras específicas de repetição de cada operação.

**Limite:** não foram validadas telas, acessibilidade, comportamento offline ou mensagens visuais nesta entrega.

### INT-002 — Documento financeiro em PDF

**Sistema/fonte:** documento fornecido pelo usuário; processamento local de leitura. Há leitores específicos Nubank, fatura Sicredi e leitor genérico, sem garantia de todo layout dessas instituições.

**Finalidade:** obter lançamentos para revisão e reduzir digitação. **Quando:** início da importação. **Por que:** documento é uma fonte de fatos externos.

**Informação enviada:** arquivo e contexto de banco/intenção/destino. **Informação recebida:** tipo detectado, metadados, linhas, incertezas e sugestões de duplicidade.

**Impacto no processo:** só produz efeitos após confirmação; associação pode aproveitar registros existentes. **Impacto no objetivo final:** ampliar a rastreabilidade dos fatos incorporados.

**Indisponibilidade/falha:** arquivo inválido, ilegível, sem texto ou acima de limites é recusado; usuário precisa obter documento compatível ou usar registro manual. Não há fila automática de OCR ou envio a banco identificado. O leitor não conserva o PDF original, mas conserva conteúdo extraído no contexto da importação.

### INT-003 — Rotina interna mensal de recorrências

**Sistema:** agendamento interno do próprio produto; não é integração financeira externa. **Finalidade:** preparar ocorrências do mês.

**Quando:** padrão configurado no primeiro dia do mês às 00:05, fuso America/Sao_Paulo; configurações podem alterar essa agenda. **Por que:** reduzir geração manual repetitiva.

**Informação usada:** usuários ativos e recorrências ativas. **Resultado:** ocorrências pendentes e registro de sucessos/falhas.

**Impacto no processo e objetivo final:** prepara o acompanhamento sem pagar automaticamente. **Indisponibilidade/falha:** geração daquele usuário pode faltar; erro de um usuário é isolado e a rotina continua com os demais. Há geração manual idempotente por competência. Não foi demonstrada política operacional de retentativa ou alerta externo.

## Sistemas externos sem integração de execução identificada

Banco, emissor do cartão e corretora são fontes ou locais de execução da operação real. Cadastro de banco, meio “PIX” ou investimento não cria conexão automática. Não foram identificadas chamadas a provedor de pagamento, Open Finance, serviço de cotação, e-mail ou mensageria que concluam essas operações no inventário examinado.
