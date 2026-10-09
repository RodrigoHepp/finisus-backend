# Requisitos funcionais e não funcionais

[Voltar ao portal](00-README.md) · [Cenários](05-validacoes-cenarios.md) · [Pendências](09-pendencias-po.md)

Catálogo extraído do comportamento existente. Critérios são verificáveis, mas não foram executados nesta entrega. Quando o comportamento é questionável, o respectivo PO registra a decisão pendente; não se presume aprovação de produto.

## RF-001 — Acesso e perfil

**Descrição:** disponibilizar o conjunto funcional de acesso e perfil.

**Objetivo:** Permitir que uma pessoa mantenha um controle próprio e volte a acessá-lo.

**Ator:** Pessoa no cadastro; titular nas demais operações.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-001; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Cadastrar → verificar e-mail único → autenticar → abrir sessão → consultar ou atualizar perfil.

**Resultado:** Usuário cadastrado ou sessão válida; perfil atualizado sem alterar a titularidade.

**Funcionalidade relacionada:** FUNC-001. **Regras relacionadas:** RN-001, RN-017, RN-018.

**Critério de aceite:** Dado um operador autenticado com `USUARIO_CADASTRAR`, e-mail ainda não cadastrado e senha de 8 a 128 caracteres, quando houver cadastro válido, então criar usuário; quando e-mail já estiver em uso, recusar. Usuário inativo não autentica.

## RF-002 — Bancos e contas

**Descrição:** disponibilizar o conjunto funcional de bancos e contas.

**Objetivo:** Organizar onde o dinheiro é mantido e a qual instituição cada conta pertence.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-002; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Consultar bancos próprios e globais → cadastrar banco se necessário → criar conta → consultar, atualizar ou inativar.

**Resultado:** Conta própria criada ativa com saldo zero; inativação preserva histórico.

**Funcionalidade relacionada:** FUNC-002. **Regras relacionadas:** RN-001, RN-019, RN-026.

**Critério de aceite:** Dada conta física, quando houver banco informado, então recusar; conta não física sem banco também é recusada. Conta nova inicia com saldo zero.

## RF-003 — Categorias, meios de pagamento e catálogo

**Descrição:** disponibilizar o conjunto funcional de categorias, meios de pagamento e catálogo.

**Objetivo:** Organizar classificações reutilizáveis e permitir análise compreensível dos registros.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-003; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Cadastrar → consultar → usar nos lançamentos → atualizar ou inativar preservando referências anteriores.

**Resultado:** Referências próprias disponíveis e classificações históricas preservadas.

**Funcionalidade relacionada:** FUNC-003. **Regras relacionadas:** RN-001, RN-006, RN-020.

**Critério de aceite:** Dada hierarquia de cinco níveis, quando se tentar incluir sexto nível, então recusar. Categoria com descendente ativo não pode ser inativada.

## RF-004 — Registrar e pesquisar transações

**Descrição:** disponibilizar o conjunto funcional de registrar e pesquisar transações.

**Objetivo:** Representar receita ou despesa realizada e localizar os fatos que explicam o saldo.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-004; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Informar dados → validar conta/referências → registrar transação → atualizar saldo → consultar registro e histórico.

**Resultado:** Entrada aumenta saldo; saída reduz saldo; origem e histórico ficam disponíveis.

**Funcionalidade relacionada:** FUNC-004. **Regras relacionadas:** RN-001, RN-002, RN-019, RN-020.

**Critério de aceite:** Dada conta com R$ 500, quando registrar saída direta de R$ 100 válida, então saldo passa a R$ 400 e transação fica consultável; conta de outro titular não é aceita.

## RF-005 — Corrigir e estornar transações

**Descrição:** disponibilizar o conjunto funcional de corrigir e estornar transações.

**Objetivo:** Retificar fatos elegíveis sem perder o registro anterior.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-005; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Consultar → validar origem e estado → corrigir revertendo efeito anterior e aplicando novo, ou estornar → consultar histórico.

**Resultado:** Saldos recompostos conforme efeito original e trilha de alteração mantida.

**Funcionalidade relacionada:** FUNC-005. **Regras relacionadas:** RN-002, RN-005, RN-021.

**Critério de aceite:** Dada saída manual elegível de R$ 100, quando corrigir para R$ 80 com motivo, então recompor R$ 20 e preservar antes/depois. Sem motivo, recusar.

## RF-006 — Detalhar itens e consultar histórico

**Descrição:** disponibilizar o conjunto funcional de detalhar itens e consultar histórico.

**Objetivo:** Explicar a composição de uma transação sem alterar seu valor financeiro.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-006; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Consultar → fornecer composição → validar soma e referências → substituir itens → registrar motivo e fotografias antes/depois.

**Resultado:** Itens alterados, mantendo valor, data, conta e saldo da transação.

**Funcionalidade relacionada:** FUNC-006. **Regras relacionadas:** RN-005, RN-020, RN-021.

**Critério de aceite:** Dada transação de R$ 100, quando detalhar em R$ 60 e R$ 40 com motivo, então preservar saldo e total; composição de R$ 99 é recusada.

## RF-007 — Transferências próprias

**Descrição:** disponibilizar o conjunto funcional de transferências próprias.

**Objetivo:** Representar mudança de localização do dinheiro sem criar renda ou consumo.

**Ator:** Titular de ambas as contas.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-007; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Validar contas distintas e ativas → debitar origem e creditar destino conjuntamente → guardar vínculo → consultar.

**Resultado:** Duas pontas vinculadas, sem aumento do total financeiro pela transferência em si.

**Funcionalidade relacionada:** FUNC-007. **Regras relacionadas:** RN-003, RN-019.

**Critério de aceite:** Dadas contas com R$ 500 e R$ 100, quando transferir R$ 200, então ficam R$ 300 e R$ 300; repetição com mesmo identificador e conteúdo não repete efeito.

## RF-008 — Reconciliação e ajuste de saldo

**Descrição:** disponibilizar o conjunto funcional de reconciliação e ajuste de saldo.

**Objetivo:** Explicar divergências internas e registrar saldo informado com justificativa.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-008; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Consultar saldo registrado e calculado → avaliar diferença → se apropriado, informar ajuste → consultar histórico.

**Resultado:** Saldo ajustado com trilha anterior e fundamento do cálculo; sem receita/despesa operacional.

**Funcionalidade relacionada:** FUNC-008. **Regras relacionadas:** RN-004, RN-019.

**Critério de aceite:** Dada conta com saldo zero, quando ajustar para R$ 1.000 com motivo/chave, então saldo e histórico refletem o ajuste e não surge receita operacional.

## RF-009 — Obrigações e pagamentos

**Descrição:** disponibilizar o conjunto funcional de obrigações e pagamentos.

**Objetivo:** Controlar contas a pagar e distinguir dívida, abatimento e dinheiro efetivamente desembolsado.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-009; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Cadastrar obrigação → acompanhar → pagar parcial ou integralmente → conferir saldo pendente e pagamentos.

**Resultado:** Obrigação permanece em aberto/vencida enquanto parcial e fica paga quando integralmente abatida.

**Funcionalidade relacionada:** FUNC-009. **Regras relacionadas:** RN-010, RN-019, RN-022.

**Critério de aceite:** Dada obrigação de R$ 100, quando pagar base de R$ 80, desconto de R$ 20, juros de R$ 5 e encargos de R$ 2, então abater R$ 100, debitar R$ 87 e marcar paga.

## RF-010 — Recorrências e ocorrências

**Descrição:** disponibilizar o conjunto funcional de recorrências e ocorrências.

**Objetivo:** Preparar expectativas mensais repetidas e registrar sua realização explícita.

**Ator:** Titular; rotina mensal.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-010; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Cadastrar → gerar mês atual ou anterior → consultar ocorrência pendente → realizar → criar movimento na data operacional.

**Resultado:** Uma ocorrência por recorrência/mês; caixa só aparece após realização.

**Funcionalidade relacionada:** FUNC-010. **Regras relacionadas:** RN-007, RN-019, RN-023.

**Critério de aceite:** Dada recorrência ativa, quando gerar a mesma competência duas vezes, então existir uma ocorrência sem caixa; realizar uma vez movimenta saldo, e repetir realização retorna erro sem novo movimento.

## RF-011 — Cartões e gastos de fatura

**Descrição:** disponibilizar o conjunto funcional de cartões e gastos de fatura.

**Objetivo:** Organizar compras e créditos de cartão por competência sem debitar a conta no registro da compra.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-011; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Cadastrar cartão → criar ou consultar fatura → lançar gasto em fatura aberta → conferir total líquido.

**Resultado:** Gastos vinculados ao mês da fatura; saldo da conta ainda não reduzido.

**Funcionalidade relacionada:** FUNC-011. **Regras relacionadas:** RN-008, RN-020, RN-024.

**Critério de aceite:** Dada fatura aberta e cartão ativo, quando registrar gasto de R$ 150 dentro do limite, então fatura recebe o gasto e saldo da conta não é debitado.

## RF-012 — Fechar, pagar e estornar faturas

**Descrição:** disponibilizar o conjunto funcional de fechar, pagar e estornar faturas.

**Objetivo:** Liquidar o compromisso de cartão sem duplicar a despesa e conservar créditos e pagamentos.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-012; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Fechar → aplicar crédito anterior disponível → registrar pagamento positivo → reduzir saldo da conta → apurar saldo aberto e excesso.

**Resultado:** Fatura paga ao atingir liquidação suficiente; caixa e crédito separados.

**Funcionalidade relacionada:** FUNC-012. **Regras relacionadas:** RN-008, RN-009, RN-024.

**Critério de aceite:** Dada fatura fechada de R$ 600, quando pagar R$ 200, então restam R$ 400 e o estado continua fechado; pagar mais R$ 450 deixa paga e gera crédito de R$ 50.

## RF-013 — Compras parceladas

**Descrição:** disponibilizar o conjunto funcional de compras parceladas.

**Objetivo:** Distribuir uma compra em compromissos mensais preservando seu valor total.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-013; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Criar compra → distribuir valores → ajustar centavos na última parcela → vincular a faturas abertas quando houver cartão.

**Resultado:** Parcelas programadas consultáveis; sua criação não reduz saldo da conta.

**Funcionalidade relacionada:** FUNC-013. **Regras relacionadas:** RN-008, RN-025.

**Critério de aceite:** Dada compra de R$ 100 em três parcelas, quando criada, então distribuir R$ 33,33, R$ 33,33 e R$ 33,34, sem débito inicial.

## RF-014 — Financiamentos e parcelas

**Descrição:** disponibilizar o conjunto funcional de financiamentos e parcelas.

**Objetivo:** Acompanhar dívida contratada, composição de parcelas e pagamentos realizados.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-014; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Cadastrar → gerar cronograma → consultar parcelas → registrar pagamento integral → vincular saída → acompanhar atraso.

**Resultado:** Parcelas pagas e pendentes distinguíveis; principal separado de juros quando composição existe.

**Funcionalidade relacionada:** FUNC-014. **Regras relacionadas:** RN-011, RN-019, RN-027.

**Critério de aceite:** Dada parcela pendente válida, quando pagar, então criar saída vinculada e marcar paga; quando estornar pagamento vinculado, então recompor conta e reabrir conforme vencimento.

## RF-015 — Amortizar, refinanciar e corrigir cronograma

**Descrição:** disponibilizar o conjunto funcional de amortizar, refinanciar e corrigir cronograma.

**Objetivo:** Representar renegociação e redução extraordinária de dívida com memória das condições anteriores.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-015; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Preservar cronograma anterior → validar situação → amortizar e recalcular pendentes, ou criar sucessor, ou excluir erro e recalcular.

**Resultado:** Novo cronograma/versionamento; refinanciamento completo liga origem ao sucessor; parcelas pagas preservadas.

**Funcionalidade relacionada:** FUNC-015. **Regras relacionadas:** RN-011, RN-027.

**Critério de aceite:** Dado cronograma com parcelas pagas e pendentes, quando amortizar validamente, então preservar pagas e versão anterior, e recalcular pendentes; correção por erro com parcela paga é bloqueada.

## RF-016 — Investimentos e movimentos

**Descrição:** disponibilizar o conjunto funcional de investimentos e movimentos.

**Objetivo:** Separar capital investido, resgate, rendimento recebido e taxa paga.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-016; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Cadastrar → registrar movimento → alterar caixa da origem conforme tipo → consultar → estornar movimento se necessário.

**Resultado:** Eventos financeiros vinculados; aporte/resgate não viram consumo/renda operacional.

**Funcionalidade relacionada:** FUNC-016. **Regras relacionadas:** RN-012, RN-019, RN-026.

**Critério de aceite:** Dado investimento ativo, quando aportar R$ 100, então reduzir conta de origem em R$ 100; quando registrar rendimento realizado de R$ 10, então aumentar conta em R$ 10 sem classificá-lo como aporte.

## RF-017 — Posições de investimento

**Descrição:** disponibilizar o conjunto funcional de posições de investimento.

**Objetivo:** Informar valor observado numa data e tornar explícita a diferença entre capital e posição.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-017; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Informar posição → guardar data/valor → consultar posições → painel usa última até a referência.

**Resultado:** Valor observado rastreável, sem fabricar movimento de rendimento.

**Funcionalidade relacionada:** FUNC-017. **Regras relacionadas:** RN-012, RN-026.

**Critério de aceite:** Dado investimento com aporte de R$ 100, quando registrar posição de R$ 110, então posição fica disponível sem entrada automática de R$ 10.

## RF-018 — Compartilhamento por transação ou item

**Descrição:** disponibilizar o conjunto funcional de compartilhamento por transação ou item.

**Objetivo:** Distribuir responsabilidade sobre todo o gasto ou parte de um item no modelo de rateio.

**Ator:** Criador titular da transação e participantes internos.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-018; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Definir alvo/base → validar consentimento e total → criar despesa/rateios → consultar → cancelar quando necessário.

**Resultado:** Responsabilidades registradas; sem pagamento bancário automático.

**Funcionalidade relacionada:** FUNC-018. **Regras relacionadas:** RN-013, RN-028.

**Critério de aceite:** Dado item de R$ 100 com base compartilhada de R$ 40, quando responsabilidades fecharem R$ 40, então conservar R$ 60 individuais; base de R$ 101 é recusada.

## RF-019 — Consentimento e resposta a rateio

**Descrição:** disponibilizar o conjunto funcional de consentimento e resposta a rateio.

**Objetivo:** Permitir que a pessoa controle recebimento de rateios e manifeste aceitação ou recusa.

**Ator:** Titular da configuração; destinatário do rateio.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-019; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Consultar/alterar consentimento → consultar recebidos → responder rateio pendente.

**Resultado:** Consentimento e resposta registrados; sem quitação fictícia.

**Funcionalidade relacionada:** FUNC-019. **Regras relacionadas:** RN-028, RN-029.

**Critério de aceite:** Dado rateio pendente do usuário, quando aceitar, então mudar para aceito sem caixa; chamada de marcar pago deve ser recusada no comportamento atual.

## RF-020 — Divisões, responsabilidades e alocações

**Descrição:** disponibilizar o conjunto funcional de divisões, responsabilidades e alocações.

**Objetivo:** Agrupar despesas entre usuários e comparar o devido com pagamentos financeiros reais.

**Ator:** Criador e participantes de divisão.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-020; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Criar grupo → associar saída elegível → congelar responsabilidades → criar alocação inicial → consultar resumo → substituir/cancelar alocações quando autorizado.

**Resultado:** Devido/pago/saldo e situação pendente, parcial ou quitada da despesa disponíveis.

**Funcionalidade relacionada:** FUNC-020. **Regras relacionadas:** RN-013, RN-014, RN-030.

**Critério de aceite:** Dada despesa de R$ 200 dividida igualmente entre dois usuários, paga por um deles, então devido é R$ 100 por pessoa e saldos são +R$ 100 e −R$ 100; mudar grupo não altera essa responsabilidade.

## RF-021 — Reembolsos e migração de compartilhamento

**Descrição:** disponibilizar o conjunto funcional de reembolsos e migração de compartilhamento.

**Objetivo:** Registrar acertos reais entre participantes e transferir responsabilidades legadas para divisões quando permitido.

**Ator:** Pagador participante; criador nos cancelamentos/migração.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-021; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Reembolso: validar dívida/crédito → vincular saída → atualizar compensação. Migração: validar destino/responsáveis → cancelar legado → associar divisão → preservar origem.

**Resultado:** Acerto rastreável ou responsabilidades migradas, sem nova saída financeira pela associação.

**Funcionalidade relacionada:** FUNC-021. **Regras relacionadas:** RN-014, RN-029, RN-031.

**Critério de aceite:** Dado pagador devedor de R$ 100 e recebedor credor de R$ 100, quando vincular saída própria de R$ 100 como reembolso elegível, então registrar compensação sem outro débito; R$ 101 é recusado.

## RF-022 — Importação e revisão de PDF

**Descrição:** disponibilizar o conjunto funcional de importação e revisão de pdf.

**Objetivo:** Reduzir digitação com conferência humana antes de produzir efeitos financeiros.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-022; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Enviar → ler → apresentar prévia/incertezas/duplicidades → revisar → confirmar → consultar vínculos criados ou associados.

**Resultado:** Extrato gera transação; cobrança gera obrigação; fatura gera gasto/crédito; associação não duplica caixa.

**Funcionalidade relacionada:** FUNC-022. **Regras relacionadas:** RN-015, RN-016, RN-032.

**Critério de aceite:** Dado PDF de extrato com linha já registrada, quando associá-la ao registro correspondente e confirmar, então não criar outro movimento; confirmar o lote novamente devolve o resultado existente.

## RF-023 — Painéis, agenda e patrimônio

**Descrição:** disponibilizar o conjunto funcional de painéis, agenda e patrimônio.

**Objetivo:** Permitir análise por período e visão da posição financeira com composição dos indicadores.

**Ator:** Titular e participante em consolidações compartilhadas.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-023; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Consultar mensal/período/anual/balancete → conferir origens → consultar agenda, visão geral, patrimônio e compartilhados.

**Resultado:** Indicadores de caixa, competência, compromissos e patrimônio com suas composições.

**Funcionalidade relacionada:** FUNC-023. **Regras relacionadas:** RN-002, RN-008, RN-012, RN-033.

**Critério de aceite:** Dado ano sem movimentos, quando consultar resumo anual, então retornar doze meses; período de resumo de dois meses deve ser recusado. Não converter PO-001 a PO-004 em critérios aprovados de produto.

## RF-024 — Previsão mensal de fluxo

**Descrição:** disponibilizar o conjunto funcional de previsão mensal de fluxo.

**Objetivo:** Reunir expectativas de entradas e saídas por mês/categoria segundo o cálculo disponível.

**Ator:** Titular.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-024; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Recalcular a partir do mês operacional → reunir recorrências ativas, transações conhecidas não recorrentes e parcelas → substituir projeções → consultar.

**Resultado:** Projeção por mês/categoria disponível; não movimenta contas.

**Funcionalidade relacionada:** FUNC-024. **Regras relacionadas:** RN-034.

**Critério de aceite:** Dado horizonte de zero ou treze meses, quando recalcular previsão, então recusar; horizonte válido começa no mês operacional e não modifica saldo de conta.

## RF-025 — Exportação, pedido de anonimização e desativação

**Descrição:** disponibilizar o conjunto funcional de exportação, pedido de anonimização e desativação.

**Objetivo:** Dar ao titular acesso aos seus dados e registrar intenção de encerrar tratamento/acesso conforme capacidades atuais.

**Ator:** Titular autenticado.

**Pré-condições:** as condições de acesso, referências e estado descritas em FUNC-025; exceção de acesso público apenas onde indicada.

**Comportamento esperado:** Exportar dados próprios ou registrar pedido → acompanhar solicitações; se desativar, revogar sessões e preservar fatos.

**Resultado:** Cópia disponível, pedido solicitado ou usuário inativo, conforme operação escolhida.

**Funcionalidade relacionada:** FUNC-025. **Regras relacionadas:** RN-017, RN-018, RN-035.

**Critério de aceite:** Dado titular autenticado, quando exportar, então entregar dados sem credenciais; quando pedir anonimização novamente com pedido aberto, então reutilizá-lo; desativação invalida acesso sem apagar dados.

## Requisitos não funcionais identificados

Os controles abaixo são verificáveis no código/configuração. Não significam certificação, disponibilidade medida ou conformidade jurídica.

| ID | Categoria | Comportamento/controle identificado | Critério verificável | Fonte |
|---|---|---|---|---|
| RNF-001 | Segurança | Operações protegidas exigem autenticação e escopo de titular/participante. | Sem sessão válida, impedir acesso; identificador alheio não autoriza leitura própria. | Segurança, serviços e teste de ownership na matriz. |
| RNF-002 | Sessão | Desativação altera validade das sessões; renovação consome credencial anterior e emite novo par. | Após desativar, token anterior deixa de autenticar; renovação inválida é recusada. | AutenticacaoService, PerfilUsuarioService e validação de sessão. |
| RNF-003 | Privacidade | Exportação evita credenciais e limita seções ao titular. | Verificar ausência de senha e tokens e isolamento de dados próprios. | Exportador e PrivacidadeIntegrationTest. |
| RNF-004 | Rastreabilidade | Correções, revisões de importação, alocações e mudanças de cronograma preservam registros históricos. | Consultar motivo/autoria/estado anterior e novo onde o fluxo os registra. | Serviços, testes e migrations da matriz. |
| RNF-005 | Integridade | Operações financeiras compostas são delimitadas transacionalmente; há proteção de concorrência em operações selecionadas. | Em falha intermediária, não persistir meia transferência ou pagamento sem vínculo; validar concorrência em MySQL real. | Serviços transacionais e testes específicos; execução não feita aqui. |
| RNF-006 | Repetição segura | Transferências, ajustes, pagamentos de fatura e importações têm proteção específica contra repetição. | Mesma chave/conteúdo não repete efeito; chave conflitante é recusada onde aplicável. | Não estender a toda operação financeira. |
| RNF-007 | Uso de recursos | Leitura de PDF tem limites de tamanho, páginas, texto, tempo e memória principal. | Rejeitar PDF sem texto ou acima dos limites; defaults: 100 páginas, 2.000.000 caracteres, limite de processamento configurado em 5 s. | LeitorDocumentoFinanceiroPdfAdapter e mensagens. Limite de processamento não é SLA. |
| RNF-008 | Interoperabilidade | Operações expostas por API com contratos e erros estruturados. | Consumidor consegue distinguir falha de autenticação, validação, recurso e conflito conforme contrato. | Controllers, OpenAPI e ApiExceptionHandler. |
| RNF-009 | Observabilidade | Existem correlação, métricas financeiras e registro do resultado da execução mensal. | Execução registra sucesso/falha por resultado; falha de um usuário não interrompe todos os seguintes. | RecorrenciaScheduler e observabilidade. |
| RNF-010 | Compatibilidade de origem web | Acesso pelo navegador usa lista configurada de origens exatas, sem curinga. | Configuração vazia/com curinga falha; origem não permitida não recebe autorização CORS. | SecurityConfig. |

### Possível requisito a validar

❓ Não há meta comprovada de tempo de resposta geral, volume simultâneo, disponibilidade, recuperação após desastre, perda de dados tolerável, acessibilidade de telas, suporte de navegadores ou política de retenção. Discutir em PO-013; não atribuir números arbitrários.

❓ A existência de testes automatizados não prova aprovação da suíte atual nem execução em MySQL. A validação desta entrega é documental.
