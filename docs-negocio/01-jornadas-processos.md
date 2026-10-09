# Jornadas e processos de negócio

[Voltar ao portal](00-README.md) · [Funcionalidades](02-funcionalidades.md) · [Estados](06-estados-permissoes-integracoes.md)

✅ Operações identificadas no backend. Sequências narrativas e exemplos são composições funcionais dessas operações, não telas homologadas. JORNADA-001 está definida no portal.

## JORNADA-002 — Usar cartão e liquidar fatura

**Objetivo do usuário:** acompanhar uma compra no período correto e conferir seu pagamento posterior.

**Ponto de entrada:** cartão próprio cadastrado. **Pré-condições:** cartão ativo, fatura aberta para novos gastos, conta ativa e saldo suficiente ao pagar. **Participantes:** titular.

**Etapas:** informar gasto ou compra parcelada → conferir competência → acompanhar fatura → fechar → registrar pagamento parcial ou integral → conferir saldo aberto/crédito → consultar caixa e competência.

**Decisões:** compra única ou parcelada; pagamento integral ou parcial; usar crédito anterior quando aplicado no fechamento.

**Regras importantes:** RN-008, RN-009, RN-024, RN-025. **Possíveis impedimentos:** limite da fatura excedido, fatura fechada para inclusão, pagamento sem identificador, saldo insuficiente.

**Integrações:** INT-001; INT-002 se houver importação. Não há pagamento bancário automático.

**Resultado esperado:** fatura e pagamentos vinculados; despesa e saída de caixa separadas. **Objetivo final atingido:** explicar o gasto e o desembolso sem somar duas despesas pelo mesmo fato.

**Exemplo:** gasto de R$ 600 não reduz saldo no lançamento. Fatura fechada recebe R$ 200: permanece fechada com R$ 400 em aberto. Recebe mais R$ 450: fica paga e conserva R$ 50 de crédito. As saídas totalizam R$ 650. A aplicação futura do crédito não gera nova saída. O estorno de origem com crédito já transportado é impedido.

## JORNADA-003 — Importar e conciliar documento

**Objetivo do usuário:** aproveitar informação de um documento sem cadastrar o mesmo fato duas vezes.

**Ponto de entrada:** PDF de extrato, cobrança ou fatura obtido fora do sistema. **Pré-condições:** banco acessível, PDF com texto, destino compatível, autenticação. **Participantes:** titular.

**Etapas:** escolher intenção/destino → enviar → conferir tipo detectado e linhas → corrigir incertezas com justificativa → decidir criar, ignorar ou associar → confirmar → consultar registros resultantes.

**Decisões:** candidata a duplicidade é o mesmo fato ou ocorrência legítima diferente; linha deve ser descartada; tipo detectado corresponde ao documento esperado.

**Regras importantes:** RN-015, RN-016, RN-032. **Possíveis impedimentos:** arquivo inválido, OCR necessário, limite de leitura, referência alheia, linha incompleta ou lote sem selecionados.

**Integrações:** INT-001 e INT-002. **Resultado esperado:** lote confirmado com cada linha selecionada vinculada ao resultado. **Objetivo final atingido:** documento incorporado de forma explicável, com decisão humana preservada.

**Exemplo:** extrato contém uma saída de R$ 100 já registrada. O usuário confere data/tipo/valor e associa a transação correspondente. Confirmação conserva o vínculo e não debita outros R$ 100. Se a linha representar outra compra realmente distinta, pode optar por criar; a sugestão não decide por ele.

## JORNADA-004 — Dividir uma despesa e compensar participantes

**Objetivo do usuário:** saber quanto cada pessoa pagou além ou aquém de sua responsabilidade.

**Ponto de entrada:** criação de divisão ou associação de despesa a uma existente. **Pré-condições:** participantes cadastrados e ativos, criador incluído, transação de saída válida com efeito em caixa. **Participantes:** criador, pagadores, devedores e recebedores.

**Etapas:** definir grupo → associar saída/base → congelar responsabilidades → verificar alocações → consultar saldos → registrar saída real de compensação fora do grupo de responsabilidades → vinculá-la como reembolso elegível.

**Decisões:** divisão igual ou percentual; compartilhar tudo ou parte; aceitar alocação inicial ou redistribuir pagamentos reais.

**Regras importantes:** RN-013, RN-014, RN-030, RN-031. **Possíveis impedimentos:** base excessiva, pagamento de terceiro fora do grupo, valor já alocado, reembolso acima do débito/crédito.

**Integrações:** INT-001. A transferência externa entre pessoas não é executada pelo Finisus.

**Resultado esperado:** valor devido, pago e saldo de compensação por participante. **Objetivo final atingido:** demonstrar responsabilidades e acertos com referência financeira, sem inventar recebimento.

**Exemplo:** Ana registra saída de R$ 200 para despesa de Ana e Bruno, dividida igualmente. A base é R$ 200; cada um deve R$ 100. A alocação inicial mostra Ana pagando R$ 200: saldo de Ana +R$ 100, de Bruno −R$ 100. A despesa está quitada, mas o acerto entre pessoas está pendente. Bruno registra saída própria de R$ 100 e reembolso elegível a Ana: compensações se ajustam sem novo débito. A conta de Ana não recebe entrada automática por esse comando.

## JORNADA-005 — Acompanhar financiamento e reduzir dívida

**Objetivo do usuário:** controlar prestações e consequências de amortização ou renegociação.

**Ponto de entrada:** informações de contrato externo. **Pré-condições:** conta própria ativa, principal e taxa válidos, quantidade e datas válidas. **Participantes:** titular.

**Etapas:** cadastrar → consultar cronograma/composição → pagar parcelas → conferir dívida → amortizar ou refinanciar se cabível → consultar versão anterior e atual.

**Decisões:** reduzir prazo ou prestação; refinanciamento completo ou encerramento legado; correção de erro ainda sem pagamentos.

**Regras importantes:** RN-011, RN-027. **Possíveis impedimentos:** saldo insuficiente, amortização excessiva, cronograma legado sem composição, recálculo com parcela paga.

**Integrações:** INT-001; negociação bancária ocorre fora do sistema.

**Resultado esperado:** pagamentos e novo cronograma preservando memória anterior. **Objetivo final atingido:** explicar a dívida remanescente e as mudanças do contrato. A finalização após pagamento ordinário da última parcela precisa de validação (PO-009).

## JORNADA-006 — Acompanhar capital investido e posição

**Objetivo do usuário:** distinguir dinheiro aplicado de rendimento efetivamente informado.

**Ponto de entrada:** investimento próprio cadastrado. **Pré-condições:** conta de origem ativa; custódia opcional válida; investimento ativo para novo registro. **Participantes:** titular.

**Etapas:** registrar aporte → acompanhar movimentos → informar posição datada → registrar taxa/rendimento/resgate quando ocorrerem → consultar patrimônio.

**Decisões:** capital movimentado ou valor observado; há custódia representando o mesmo ativo; é necessário estornar um evento.

**Regras importantes:** RN-012, RN-026. **Possíveis impedimentos:** conta inválida, falta de saldo para saída, investimento inativo, custódia duplicada, estorno repetido.

**Integrações:** INT-001; dados de instituição financeira são fornecidos pelo usuário.

**Resultado esperado:** capital, posição e caixa identificados separadamente. **Objetivo final atingido:** representar o ativo sem duplicar a conta de custódia e sem inferir rentabilidade.

**Exemplo:** aporte de R$ 1.000 reduz saldo da origem em R$ 1.000. Uma posição posterior de R$ 1.050 altera o valor observado no acompanhamento patrimonial; não cria automaticamente R$ 50 de entrada na conta.

## Processos completos

### PROC-001 — Preparar, registrar e conciliar caixa

**Objetivo:** manter contas e movimentos explicáveis. **Início:** cadastro ou novo fato financeiro. **Participantes:** titular.

1. Criar referências e conta; conferir saldo zero inicial.
2. Registrar saldo de partida por ajuste justificado quando necessário.
3. Registrar entrada/saída ou transferência própria.
4. Conferir saldo e histórico.
5. Se houver erro, corrigir/estornar pelo fluxo autorizado.
6. Consultar reconciliação; se divergente, investigar antes de ajustar.

**Decisões/caminhos:** receita, despesa, transferência ou ajuste; não usar categoria para simular origem. **Regras:** RN-001 a RN-006, RN-019 a RN-021. **Integração:** INT-001. **Estados:** conta ativa/inativa; transação ativa/estornada; transferência ativa/estornada. **Fim/resultado:** fato correto e conciliável ou impedimento explicado. **Contribuição:** base do acompanhamento de caixa.

### PROC-002 — Prever e realizar compromisso

**Objetivo:** separar dívida/expectativa de pagamento. **Início:** cobrança ou recorrência. **Participantes:** titular e rotina mensal quando aplicável.

1. Cadastrar obrigação com credor/vencimento, ou recorrência mensal.
2. Na recorrência, gerar ocorrência única por competência.
3. Acompanhar o compromisso; obrigação pode vencer.
4. Registrar pagamento da obrigação ou realizar ocorrência.
5. Conferir transação vinculada; no pagamento parcial, continuar acompanhamento.
6. Estornar pagamento da obrigação pelo seu recurso quando necessário.

**Decisões/caminhos:** pagamento parcial/integral, juros/encargos/desconto, cancelamento sem pagamentos. **Regras:** RN-007, RN-010, RN-022, RN-023. **Integrações:** INT-001, INT-003. **Estados:** obrigação em aberto/vencida/paga/cancelada; ocorrência pendente/realizada. **Fim:** obrigação paga/cancelada ou ocorrência realizada. **Contribuição:** obrigações conhecidas sem antecipar caixa. Pendências de agenda e estorno da ocorrência estão registradas separadamente.

### PROC-003 — Compra, competência e liquidação do cartão

**Objetivo:** controlar o ciclo do cartão. **Início:** gasto ou compra parcelada. **Participante:** titular.

1. Preparar cartão/fatura.
2. Registrar gasto ou distribuir parcelas nas competências.
3. Fechar fatura e aplicar créditos disponíveis.
4. Pagar com identificação da solicitação.
5. Conferir total, pago, aberto e crédito.
6. Se necessário, estornar pagamento mais recente permitido.

**Decisões/caminhos:** compra sem cartão segue previsão legada; pagamento parcial mantém aberta a dívida, não a fatura para novos gastos. **Regras:** RN-008, RN-009, RN-024, RN-025. **Integrações:** INT-001/INT-002. **Estados:** aberta → fechada → paga; cancelamento só aberta. **Fim:** fatura liquidada ou cancelada segundo regras. **Contribuição:** distinguir consumo e pagamento.

### PROC-004 — Contrato e evolução da dívida

**Objetivo:** acompanhar financiamento. **Início:** registro do contrato externo. **Participante:** titular.

1. Registrar condições e gerar cronograma.
2. Consultar e pagar parcelas.
3. Registrar atrasos quando o processamento correspondente ocorrer.
4. Para renegociar, preservar versão anterior.
5. Amortizar pendentes, refinanciar com sucessor ou corrigir erro permitido.
6. Consultar composição e versões.

**Decisões/caminhos:** redução de prazo/prestação; quitação; cancelamento sem parcela paga. **Regras:** RN-011/RN-027. **Integração:** INT-001. **Estados:** contrato ativo/finalizado/cancelado; parcelas pendentes/pagas/atrasadas. **Fim:** cronograma atualizado ou finalização explícita pelo fluxo disponível. **Contribuição:** dívida explicável; não prometer finalização automática na última parcela.

### PROC-005 — Movimentar e observar investimento

**Objetivo:** controlar ativo e liquidez. **Início:** aporte ou observação de posição. **Participante:** titular.

1. Cadastrar investimento e origem.
2. Vincular custódia opcional.
3. Registrar tipo correto de movimento.
4. Informar posição quando conhecida.
5. Consultar patrimônio e, se necessário, estornar movimento pelo investimento.

**Decisões/caminhos:** capital, rendimento, taxa ou apenas observação. **Regras:** RN-012/RN-026. **Integração:** INT-001. **Estados:** cadastro ativo/inativo; movimento ativo/estornado. **Fim:** evento/posição registrado, sem “concluir” investimento automaticamente. **Contribuição:** separar valor observado de caixa e capital.

### PROC-006 — Responsabilizar, pagar e compensar

**Objetivo:** demonstrar obrigações entre pessoas. **Início:** despesa conjunta. **Participantes:** criador, pagadores e demais participantes.

1. Escolher rateio por alvo ou divisão; não usar ambos no mesmo alvo.
2. No legado, exigir consentimento e fechar total das responsabilidades.
3. Na divisão, congelar base e responsabilidade por despesa.
4. Referenciar pagamentos reais por alocações.
5. Consultar situação da despesa e saldos dos participantes.
6. Registrar reembolso elegível ou migrar legado elegível.
7. Cancelar referências quando necessário, preservando histórico.

**Decisões/caminhos:** base integral/parcial, valores/percentuais, migração de externos antigos. **Regras:** RN-013/RN-014/RN-028 a RN-031. **Integração:** INT-001. **Estados:** divisão ativa/inativa; pagamento pendente/parcial/quitado; vínculo confirmado/pendente de revisão/cancelado conforme representação. **Fim:** acertos demonstrados; saldo de compensação zero indica acerto no intervalo considerado. **Contribuição:** confiança no histórico compartilhado, sem caixa paralelo.

### PROC-007 — Incorporar documento financeiro

**Objetivo:** converter documento em registros revisados. **Início:** envio do PDF. **Participante:** titular.

1. Validar intenção e destino.
2. Identificar conteúdo e recuperar lote repetido quando aplicável.
3. Extrair linhas, incertezas e candidatas.
4. Revisar com justificativa.
5. Confirmar somente quando selecionados estiverem aptos.
6. Criar/associar registros conforme o tipo e preservar vínculos.

**Decisões/caminhos:** criar, ignorar, associar transação ou obrigação. **Regras:** RN-015/RN-016/RN-032. **Integração:** INT-002. **Estados:** pendente de revisão → confirmada; linhas pendentes/ignoradas/associadas/criadas. **Fim:** lote confirmado; repetição não cria efeitos adicionais. **Contribuição:** menos digitação e decisão rastreável.

### PROC-008 — Consultar indicadores e projeções

**Objetivo:** entender situação e próximos compromissos. **Início:** seleção do período. **Participante:** titular.

1. Consultar resumo e balancete.
2. Separar competência e caixa.
3. Consultar agenda/visão geral e composição patrimonial.
4. Conferir compartilhados.
5. Recalcular projeção se necessário.
6. Investigar valores com base na composição e nas limitações.

**Decisões/caminhos:** histórico analítico, posição atual ou projeção; os resultados não são intercambiáveis. **Regras:** RN-002/RN-012/RN-033/RN-034. **Integração:** INT-001. **Estados:** consultas não encerram períodos; projeção fica armazenada após recálculo. **Fim:** informação disponibilizada. **Contribuição:** decisão informada, sujeita às inconsistências catalogadas.

### PROC-009 — Exercitar capacidades de privacidade

**Objetivo:** obter cópia dos dados e registrar pedido ou encerramento de acesso. **Início:** ação do titular autenticado. **Participante:** titular; responsável pelo atendimento ainda não definido.

1. Exportar dados, quando desejado.
2. Registrar pedido com motivo.
3. Acompanhar o próprio pedido enquanto houver acesso.
4. Desativar acesso somente se essa for a ação escolhida.

**Decisões/caminhos:** exportar, pedir anonimização e desativar são ações independentes. **Regras:** RN-017/RN-018/RN-035. **Integração:** INT-001. **Estados:** pedido solicitado e usuário ativo/inativo; etapas posteriores do pedido não têm comando público identificado. **Fim:** exportação/pedido/desativação concluído; anonimização efetiva não demonstrada. **Contribuição:** transparência sobre o alcance real do controle do titular.
