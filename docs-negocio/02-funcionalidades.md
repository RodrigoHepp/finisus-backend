# Catálogo de funcionalidades

[Voltar ao portal](00-README.md) · [Requisitos](03-requisitos.md) · [Regras](04-regras-negocio.md) · [Fontes](08-rastreabilidade.md)

Cada FUNC agrupa um conjunto coeso de operações, incluindo consultas e manutenção indicadas no fluxo. A matriz técnica inventaria as operações HTTP individualmente. ✅ Fluxos descritos foram identificados estaticamente; o valor para o negócio é uma interpretação funcional, sem afirmar motivação histórica do produto.

## FUNC-001 — Acesso e perfil

**Objetivo e valor para o negócio:** Permitir que uma pessoa mantenha um controle próprio e volte a acessá-lo.

**Atores envolvidos:** Pessoa no cadastro; titular nas demais operações.

**Quando é utilizada:** No primeiro acesso, na renovação da sessão e na atualização de nome/e-mail.

**Pré-condições:** Para cadastro, e-mail ainda não utilizado e dados válidos; para login, usuário ativo e senha correta; para perfil, sessão do próprio titular.

**Entradas:** Cadastro com nome, e-mail e senha; acesso com e-mail e senha; perfil com nome/e-mail.

**Fluxo principal:** Cadastrar → verificar e-mail único → autenticar → abrir sessão → consultar ou atualizar perfil.

**Fluxos alternativos:** Renovar acesso com credencial de renovação válida; consultar perfil sem alterá-lo.

**Exceções:** E-mail repetido, senha incorreta, usuário inativo ou credencial inválida impedem a operação.

**Resultado esperado:** Usuário cadastrado ou sessão válida; perfil atualizado sem alterar a titularidade.

**Impacto no objetivo final:** Permitir que uma pessoa mantenha um controle próprio e volte a acessá-lo. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-001, RN-017, RN-018. **Requisito:** RF-001.

**Permissões:** Cadastro restrito a usuário autenticado com `USUARIO_CADASTRAR`; perfil restrito ao próprio titular.

**Integrações envolvidas:** INT-001.

**Observações:** Cadastro não comprova recuperação de senha, confirmação de e-mail ou perfil administrativo.

## FUNC-002 — Bancos e contas

**Objetivo e valor para o negócio:** Organizar onde o dinheiro é mantido e a qual instituição cada conta pertence.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Na preparação do controle ou abertura de uma nova conta.

**Pré-condições:** Usuário autenticado; banco próprio ou global acessível quando o tipo de conta o exige.

**Entradas:** Banco pessoal: nome e código. Conta: nome, tipo e banco quando não física.

**Fluxo principal:** Consultar bancos próprios e globais → cadastrar banco se necessário → criar conta → consultar, atualizar ou inativar.

**Fluxos alternativos:** Conta física dispensa e proíbe banco; bancos globais podem ser usados por diferentes titulares.

**Exceções:** Banco de terceiro é inacessível; alteração de banco global é recusada; tipo de conta incompatível com banco é inválido.

**Resultado esperado:** Conta própria criada ativa com saldo zero; inativação preserva histórico.

**Impacto no objetivo final:** Organizar onde o dinheiro é mantido e a qual instituição cada conta pertence. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-001, RN-019, RN-026. **Requisito:** RF-002.

**Permissões:** Titular altera seus cadastros; banco global é somente leitura.

**Integrações envolvidas:** INT-001.

**Observações:** Saldo de partida usa ajuste justificado, sem fabricar receita. Conta inativa com saldo pode continuar no patrimônio.

## FUNC-003 — Categorias, meios de pagamento e catálogo

**Objetivo e valor para o negócio:** Organizar classificações reutilizáveis e permitir análise compreensível dos registros.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Antes de classificar registros ou ao reorganizar o controle.

**Pré-condições:** Usuário autenticado; categoria pai própria e ativa quando informada; descendentes inativos antes da inativação de pai.

**Entradas:** Categoria: nome e pai opcional; meio: nome; item: nome e categoria padrão opcional.

**Fluxo principal:** Cadastrar → consultar → usar nos lançamentos → atualizar ou inativar preservando referências anteriores.

**Fluxos alternativos:** Linha de transação pode ter descrição própria sem item de catálogo.

**Exceções:** Categoria com ciclo, sexto nível, pai inativo ou descendente ativo na inativação é recusada.

**Resultado esperado:** Referências próprias disponíveis e classificações históricas preservadas.

**Impacto no objetivo final:** Organizar classificações reutilizáveis e permitir análise compreensível dos registros. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-001, RN-006, RN-020. **Requisito:** RF-003.

**Permissões:** Somente titular dos cadastros.

**Integrações envolvidas:** INT-001.

**Observações:** Nome de categoria não transforma saída em investimento ou transferência. Não presumir estoque.

## FUNC-004 — Registrar e pesquisar transações

**Objetivo e valor para o negócio:** Representar receita ou despesa realizada e localizar os fatos que explicam o saldo.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Quando acontece uma entrada ou saída direta; não para substituir pagamento vinculado de outro módulo.

**Pré-condições:** Conta própria ativa, saldo suficiente para saída e referências próprias utilizáveis; itens, se fornecidos, devem fechar o total.

**Entradas:** Tipo, valor, data, descrição, conta; categoria, meio e itens quando usados. Consulta admite filtros e paginação.

**Fluxo principal:** Informar dados → validar conta/referências → registrar transação → atualizar saldo → consultar registro e histórico.

**Fluxos alternativos:** Pesquisar por critérios oferecidos no contrato; registrar sem itens ou com linhas livres válidas.

**Exceções:** Conta inativa ou alheia, valor inválido, soma de itens divergente ou saldo resultante negativo impedem registro.

**Resultado esperado:** Entrada aumenta saldo; saída reduz saldo; origem e histórico ficam disponíveis.

**Impacto no objetivo final:** Representar receita ou despesa realizada e localizar os fatos que explicam o saldo. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-001, RN-002, RN-019, RN-020. **Requisito:** RF-004.

**Permissões:** Titular da conta e referências.

**Integrações envolvidas:** INT-001.

**Observações:** Registrar saída direta de pagamento já lançado pela obrigação ou fatura duplicaria o fato; usar o fluxo de origem.

## FUNC-005 — Corrigir e estornar transações

**Objetivo e valor para o negócio:** Retificar fatos elegíveis sem perder o registro anterior.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Ao identificar erro em transação já registrada.

**Pré-condições:** Transação própria não estornada, origem compatível com o comando e ausência dos bloqueios de compartilhamento; motivo para correção.

**Entradas:** Transação, novos dados completos e motivo na correção; identificação da transação no estorno.

**Fluxo principal:** Consultar → validar origem e estado → corrigir revertendo efeito anterior e aplicando novo, ou estornar → consultar histórico.

**Fluxos alternativos:** Se o lançamento pertence a transferência, pagamento de fatura, obrigação ou parcela, usar o comando do módulo responsável.

**Exceções:** Correção de lançamento estornado ou de origem imutável é bloqueada; compartilhamento legado ativo bloqueia alteração; novo estorno do mesmo lançamento é recusado.

**Resultado esperado:** Saldos recompostos conforme efeito original e trilha de alteração mantida.

**Impacto no objetivo final:** Retificar fatos elegíveis sem perder o registro anterior. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-002, RN-005, RN-021. **Requisito:** RF-005.

**Permissões:** Titular; restrições específicas do vínculo permanecem.

**Integrações envolvidas:** INT-001.

**Observações:** A proteção de divisões difere do compartilhamento legado; ver PO-010. Estorno de ocorrência não reabre automaticamente sua realização; ver PO-007.

## FUNC-006 — Detalhar itens e consultar histórico

**Objetivo e valor para o negócio:** Explicar a composição de uma transação sem alterar seu valor financeiro.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Quando se deseja separar uma compra em itens ou investigar uma correção.

**Pré-condições:** Transação própria não estornada, origem que permita detalhamento, itens sem compartilhamento ativo, nova composição fechando o valor e motivo.

**Entradas:** Transação, lista não vazia de itens e motivo; cada linha com valor e descrição própria ou catálogo; quantidade positiva se informada.

**Fluxo principal:** Consultar → fornecer composição → validar soma e referências → substituir itens → registrar motivo e fotografias antes/depois.

**Fluxos alternativos:** Permite detalhar gasto de cartão e transação de recorrência; categoria pode vir do catálogo.

**Exceções:** Itens compartilhados ativamente não podem ser substituídos; pagamento vinculado, transferência e parcela de compra têm restrições.

**Resultado esperado:** Itens alterados, mantendo valor, data, conta e saldo da transação.

**Impacto no objetivo final:** Explicar a composição de uma transação sem alterar seu valor financeiro. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-005, RN-020, RN-021. **Requisito:** RF-006.

**Permissões:** Titular da transação e referências.

**Integrações envolvidas:** INT-001.

**Observações:** Quantidade é informação da linha: seu valor informado compõe a soma; não presumir multiplicação automática por preço unitário.

## FUNC-007 — Transferências próprias

**Objetivo e valor para o negócio:** Representar mudança de localização do dinheiro sem criar renda ou consumo.

**Atores envolvidos:** Titular de ambas as contas.

**Quando é utilizada:** Após transferência real entre contas próprias.

**Pré-condições:** Duas contas próprias distintas e ativas; saldo suficiente na origem; valor positivo e identificador de solicitação.

**Entradas:** Origem, destino, valor positivo, data, descrição e identificador da solicitação.

**Fluxo principal:** Validar contas distintas e ativas → debitar origem e creditar destino conjuntamente → guardar vínculo → consultar.

**Fluxos alternativos:** Repetição com mesmo identificador e conteúdo retorna a operação existente; estorno desfaz ambas as pontas.

**Exceções:** Contas iguais, conta de terceiro, saldo insuficiente ou reutilização do identificador com outro conteúdo são recusados.

**Resultado esperado:** Duas pontas vinculadas, sem aumento do total financeiro pela transferência em si.

**Impacto no objetivo final:** Representar mudança de localização do dinheiro sem criar renda ou consumo. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-003, RN-019. **Requisito:** RF-007.

**Permissões:** Titular de origem e destino.

**Integrações envolvidas:** INT-001.

**Observações:** Não executa transferência bancária; não serve como transferência entre dois usuários.

## FUNC-008 — Reconciliação e ajuste de saldo

**Objetivo e valor para o negócio:** Explicar divergências internas e registrar saldo informado com justificativa.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Na preparação do saldo de partida ou conferência posterior.

**Pré-condições:** Conta própria existente; para ajuste, saldo informado não negativo, motivo e identificador de solicitação.

**Entradas:** Conta; para ajuste, saldo informado não negativo, motivo e identificador da solicitação.

**Fluxo principal:** Consultar saldo registrado e calculado → avaliar diferença → se apropriado, informar ajuste → consultar histórico.

**Fluxos alternativos:** Repetição idêntica de ajuste é reaproveitada; consulta não exige efetuar ajuste.

**Exceções:** Conta alheia, dados inválidos ou identificador reutilizado com conteúdo diferente são recusados.

**Resultado esperado:** Saldo ajustado com trilha anterior e fundamento do cálculo; sem receita/despesa operacional.

**Impacto no objetivo final:** Explicar divergências internas e registrar saldo informado com justificativa. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-004, RN-019. **Requisito:** RF-008.

**Permissões:** Titular da conta; não presumir exigência de conta ativa no ajuste, pois esse fluxo não a aplica.

**Integrações envolvidas:** INT-001.

**Observações:** Reconciliação compara registros internos. Comparação com extrato externo depende da informação do usuário.

## FUNC-009 — Obrigações e pagamentos

**Objetivo e valor para o negócio:** Controlar contas a pagar e distinguir dívida, abatimento e dinheiro efetivamente desembolsado.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Quando surge cobrança com vencimento, antes ou no momento da liquidação.

**Pré-condições:** Conta própria ativa; obrigação em aberto ou vencida para pagar; abatimento/caixa positivos e saldo disponível para o desembolso.

**Entradas:** Descrição, credor, valor, vencimento, conta de pagamento e categoria opcional; pagamento com data, valor base opcional, juros, encargos e desconto opcionais.

**Fluxo principal:** Cadastrar obrigação → acompanhar → pagar parcial ou integralmente → conferir saldo pendente e pagamentos.

**Fluxos alternativos:** Estornar pagamento individual; comando legado estorna todos os ativos; cancelar somente sem valor abatido.

**Exceções:** Abatimento acima do pendente, caixa nulo, estado incompatível ou saldo insuficiente impedem pagamento.

**Resultado esperado:** Obrigação permanece em aberto/vencida enquanto parcial e fica paga quando integralmente abatida.

**Impacto no objetivo final:** Controlar contas a pagar e distinguir dívida, abatimento e dinheiro efetivamente desembolsado. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-010, RN-019, RN-022. **Requisito:** RF-009.

**Permissões:** Titular da obrigação e conta.

**Integrações envolvidas:** INT-001; INT-002 quando originada por cobrança importada..

**Observações:** Pagamento de R$ 80 com desconto de R$ 20 abate R$ 100; juros e encargos aumentam caixa, sem abater principal adicional. Ver PO-001 para o painel.

## FUNC-010 — Recorrências e ocorrências

**Objetivo e valor para o negócio:** Preparar expectativas mensais repetidas e registrar sua realização explícita.

**Atores envolvidos:** Titular; rotina mensal.

**Quando é utilizada:** Ao organizar receita ou despesa repetida e em cada competência.

**Pré-condições:** Recorrência própria; conta ativa para criação/realização; geração de mês atual/anterior; ocorrência pendente para realização.

**Entradas:** Nome, tipo, valor esperado, dia do mês, conta e referências opcionais; competência para geração; ocorrência para realização.

**Fluxo principal:** Cadastrar → gerar mês atual ou anterior → consultar ocorrência pendente → realizar → criar movimento na data operacional.

**Fluxos alternativos:** Dia 31 é limitado ao último dia do mês; geração repetida não cria nova ocorrência; conta inativa é pulada na geração.

**Exceções:** Mês futuro é recusado na geração; realizar ocorrência já realizada retorna erro sem novo caixa.

**Resultado esperado:** Uma ocorrência por recorrência/mês; caixa só aparece após realização.

**Impacto no objetivo final:** Preparar expectativas mensais repetidas e registrar sua realização explícita. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-007, RN-019, RN-023. **Requisito:** RF-010.

**Permissões:** Titular; rotina processa usuários ativos.

**Integrações envolvidas:** INT-001, INT-003.

**Observações:** A realização não recebe data retroativa no comando atual. Pendência de agenda e reabertura após estorno em PO-003 e PO-007.

## FUNC-011 — Cartões e gastos de fatura

**Objetivo e valor para o negócio:** Organizar compras e créditos de cartão por competência sem debitar a conta no registro da compra.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Ao cadastrar cartão e registrar gastos de uma fatura aberta.

**Pré-condições:** Cartão próprio ativo; fatura aberta e limite suficiente para novo gasto; conta e classificações acessíveis.

**Entradas:** Cartão: nome, limite e dias de fechamento/vencimento; fatura: cartão, competência, datas e conta; gasto: valor, data, descrição, conta, categoria/itens opcionais.

**Fluxo principal:** Cadastrar cartão → criar ou consultar fatura → lançar gasto em fatura aberta → conferir total líquido.

**Fluxos alternativos:** Importação pode produzir crédito de fatura, reduzindo seu total sem entrada bancária.

**Exceções:** Cartão inativo, fatura não aberta, competência duplicada ou limite excedido recusam o novo gasto.

**Resultado esperado:** Gastos vinculados ao mês da fatura; saldo da conta ainda não reduzido.

**Impacto no objetivo final:** Organizar compras e créditos de cartão por competência sem debitar a conta no registro da compra. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-008, RN-020, RN-024. **Requisito:** RF-011.

**Permissões:** Titular do cartão e referências.

**Integrações envolvidas:** INT-001; INT-002 para crédito/documento importado..

**Observações:** O limite verificado é o total da fatura na operação, não uma garantia de limite bancário global entre todas as competências.

## FUNC-012 — Fechar, pagar e estornar faturas

**Objetivo e valor para o negócio:** Liquidar o compromisso de cartão sem duplicar a despesa e conservar créditos e pagamentos.

**Atores envolvidos:** Titular.

**Quando é utilizada:** No fechamento de ciclo e após pagamento externo da fatura.

**Pré-condições:** Fatura própria aberta para fechar; fechada com saldo pendente para pagar; conta ativa com saldo suficiente e identificador de solicitação.

**Entradas:** Fatura, data de pagamento, valor opcional, conta opcional e identificador da solicitação; data de referência para processar ciclos.

**Fluxo principal:** Fechar → aplicar crédito anterior disponível → registrar pagamento positivo → reduzir saldo da conta → apurar saldo aberto e excesso.

**Fluxos alternativos:** Pagamento parcial mantém fatura fechada; excesso vira crédito; ciclos atrasados avançam competências; estorno usa pagamento ativo mais recente.

**Exceções:** Pagamento sem saldo aberto, fatura em estado incompatível ou crédito já transportado no estorno são bloqueados.

**Resultado esperado:** Fatura paga ao atingir liquidação suficiente; caixa e crédito separados.

**Impacto no objetivo final:** Liquidar o compromisso de cartão sem duplicar a despesa e conservar créditos e pagamentos. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-008, RN-009, RN-024. **Requisito:** RF-012.

**Permissões:** Titular do cartão e conta de pagamento.

**Integrações envolvidas:** INT-001.

**Observações:** Não há comando público de estornar qualquer pagamento escolhido por ID neste fluxo; cancelamento/edição da fatura só enquanto aberta.

## FUNC-013 — Compras parceladas

**Objetivo e valor para o negócio:** Distribuir uma compra em compromissos mensais preservando seu valor total.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Ao registrar uma compra dividida em parcelas.

**Pré-condições:** Conta própria ativa; número positivo de parcelas; se houver cartão, ativo e com faturas de destino abertas e dentro do limite.

**Entradas:** Descrição, valor total, número de parcelas, data da compra, conta, categoria opcional e cartão opcional.

**Fluxo principal:** Criar compra → distribuir valores → ajustar centavos na última parcela → vincular a faturas abertas quando houver cartão.

**Fluxos alternativos:** Sem cartão, gera previsão legada; ao cancelar, estorna parcelas elegíveis conforme fatura/data.

**Exceções:** Número menor que um, cartão inativo, fatura fechada ou limite excedido impedem criação.

**Resultado esperado:** Parcelas programadas consultáveis; sua criação não reduz saldo da conta.

**Impacto no objetivo final:** Distribuir uma compra em compromissos mensais preservando seu valor total. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-008, RN-025. **Requisito:** RF-013.

**Permissões:** Titular da compra, conta e cartão quando usado.

**Integrações envolvidas:** INT-001.

**Observações:** Com cartão, cancelamento alcança parcelas de faturas abertas; sem cartão, somente datas futuras. Liquidação sem cartão requer definição em PO-008.

## FUNC-014 — Financiamentos e parcelas

**Objetivo e valor para o negócio:** Acompanhar dívida contratada, composição de parcelas e pagamentos realizados.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Ao registrar contrato e pagar suas parcelas.

**Pré-condições:** Contrato próprio e condições válidas; parcela elegível e conta com saldo para pagamento; vínculo financeiro para estorno.

**Entradas:** Descrição, principal, taxa mensal, quantidade de parcelas, início e conta; parcela e data no pagamento.

**Fluxo principal:** Cadastrar → gerar cronograma → consultar parcelas → registrar pagamento integral → vincular saída → acompanhar atraso.

**Fluxos alternativos:** Estorno de pagamento vinculado reabre parcela pendente ou atrasada; histórico de cronograma pode ser consultado.

**Exceções:** Dados inválidos, recurso alheio, estado incompatível ou saldo insuficiente impedem a operação.

**Resultado esperado:** Parcelas pagas e pendentes distinguíveis; principal separado de juros quando composição existe.

**Impacto no objetivo final:** Acompanhar dívida contratada, composição de parcelas e pagamentos realizados. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-011, RN-019, RN-027. **Requisito:** RF-014.

**Permissões:** Titular do contrato e conta.

**Integrações envolvidas:** INT-001.

**Observações:** Não presumir crédito automático do principal na conta ao cadastrar. A última parcela paga não demonstra finalização automática do contrato; ver PO-009.

## FUNC-015 — Amortizar, refinanciar e corrigir cronograma

**Objetivo e valor para o negócio:** Representar renegociação e redução extraordinária de dívida com memória das condições anteriores.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Após amortização, renegociação ou identificação de parcela lançada por erro.

**Pré-condições:** Contrato próprio elegível; cronograma com composição para amortizar; saldo disponível; sem parcelas pagas para correção por erro/cancelamento.

**Entradas:** Contrato; amortização com valor, data e modalidade; refinanciamento com parcela de referência e novo contrato; correção com parcela.

**Fluxo principal:** Preservar cronograma anterior → validar situação → amortizar e recalcular pendentes, ou criar sucessor, ou excluir erro e recalcular.

**Fluxos alternativos:** Reduzir prestação mantém quantidade; reduzir prazo busca prazo compatível com prestação vigente; quitação por amortização finaliza.

**Exceções:** Amortização maior que saldo, combinação indevida de modalidade e quantidade, ausência de composição, correção com parcela paga ou remoção da única parcela são recusadas.

**Resultado esperado:** Novo cronograma/versionamento; refinanciamento completo liga origem ao sucessor; parcelas pagas preservadas.

**Impacto no objetivo final:** Representar renegociação e redução extraordinária de dívida com memória das condições anteriores. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-011, RN-027. **Requisito:** RF-015.

**Permissões:** Titular do contrato.

**Integrações envolvidas:** INT-001.

**Observações:** Fluxo legado de refinanciamento apenas encerra origem e remove pendentes a partir da referência; não equivale ao fluxo completo.

## FUNC-016 — Investimentos e movimentos

**Objetivo e valor para o negócio:** Separar capital investido, resgate, rendimento recebido e taxa paga.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Ao cadastrar investimento e registrar seus eventos reais.

**Pré-condições:** Investimento próprio ativo para novo movimento, conta de origem ativa e saldo para saídas; custódia opcional válida.

**Entradas:** Nome, tipo, conta de origem e custódia opcional; movimento com tipo, valor, data e investimento.

**Fluxo principal:** Cadastrar → registrar movimento → alterar caixa da origem conforme tipo → consultar → estornar movimento se necessário.

**Fluxos alternativos:** Aporte/taxa são saídas; resgate/rendimento realizado são entradas; arquivamento mantém valor residual no patrimônio.

**Exceções:** Investimento inativo impede novos movimentos; custódia incompatível/duplicada e estorno repetido são recusados.

**Resultado esperado:** Eventos financeiros vinculados; aporte/resgate não viram consumo/renda operacional.

**Impacto no objetivo final:** Separar capital investido, resgate, rendimento recebido e taxa paga. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-012, RN-019, RN-026. **Requisito:** RF-016.

**Permissões:** Titular do investimento e contas.

**Integrações envolvidas:** INT-001.

**Observações:** Não executa ordens na corretora nem calcula rendimento implícito.

## FUNC-017 — Posições de investimento

**Objetivo e valor para o negócio:** Informar valor observado numa data e tornar explícita a diferença entre capital e posição.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Quando consulta o valor do investimento em fonte externa.

**Pré-condições:** Investimento próprio ativo para nova posição; valor e data de referência válidos.

**Entradas:** Investimento ativo, valor observado e data de referência.

**Fluxo principal:** Informar posição → guardar data/valor → consultar posições → painel usa última até a referência.

**Fluxos alternativos:** Sem posição, painel usa capital líquido aportado e informa ausência de posição.

**Exceções:** Investimento alheio ou inativo e dados inválidos impedem novo registro.

**Resultado esperado:** Valor observado rastreável, sem fabricar movimento de rendimento.

**Impacto no objetivo final:** Informar valor observado numa data e tornar explícita a diferença entre capital e posição. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-012, RN-026. **Requisito:** RF-017.

**Permissões:** Titular do investimento.

**Integrações envolvidas:** INT-001.

**Observações:** Patrimônio combina posições datadas com saldos/dívidas atuais; não é fotografia histórica integral.

## FUNC-018 — Compartilhamento por transação ou item

**Objetivo e valor para o negócio:** Distribuir responsabilidade sobre todo o gasto ou parte de um item no modelo de rateio.

**Atores envolvidos:** Criador titular da transação e participantes internos.

**Quando é utilizada:** Ao atribuir uma despesa individual a outras pessoas cadastradas.

**Pré-condições:** Transação própria elegível, alvo ainda não compartilhado, participantes internos com opt-in e responsabilidades fechando a base.

**Entradas:** Transação ou item; tipo de rateio; participantes; valores ou percentuais; base de item quando parcial.

**Fluxo principal:** Definir alvo/base → validar consentimento e total → criar despesa/rateios → consultar → cancelar quando necessário.

**Fluxos alternativos:** Base de item pode ser integral, quantidade, percentual ou valor explícito; restante fica individual.

**Exceções:** Novo participante externo, ausência de opt-in, total divergente, alvo já compartilhado ou conflito com divisão são recusados.

**Resultado esperado:** Responsabilidades registradas; sem pagamento bancário automático.

**Impacto no objetivo final:** Distribuir responsabilidade sobre todo o gasto ou parte de um item no modelo de rateio. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-013, RN-028. **Requisito:** RF-018.

**Permissões:** Criador mantém despesa; destinatário responde ao próprio rateio.

**Integrações envolvidas:** INT-001.

**Observações:** Este modelo mantém uso e histórico, mas sua marcação direta de pagamento está bloqueada.

## FUNC-019 — Consentimento e resposta a rateio

**Objetivo e valor para o negócio:** Permitir que a pessoa controle recebimento de rateios e manifeste aceitação ou recusa.

**Atores envolvidos:** Titular da configuração; destinatário do rateio.

**Quando é utilizada:** Antes de receber compartilhamento e ao examinar rateio recebido.

**Pré-condições:** Sessão própria para consentimento; destinatário interno do rateio e estado pendente para aceitar/recusar.

**Entradas:** Aceita compartilhamento; rateio e decisão de aceitar/recusar.

**Fluxo principal:** Consultar/alterar consentimento → consultar recebidos → responder rateio pendente.

**Fluxos alternativos:** Recusar é alternativa a aceitar; resposta não movimenta caixa.

**Exceções:** Responder rateio de outro usuário ou transição inválida é recusado; marcar pago é sempre bloqueado pela aplicação atual.

**Resultado esperado:** Consentimento e resposta registrados; sem quitação fictícia.

**Impacto no objetivo final:** Permitir que a pessoa controle recebimento de rateios e manifeste aceitação ou recusa. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-028, RN-029. **Requisito:** RF-019.

**Permissões:** Somente titular/destinatário.

**Integrações envolvidas:** INT-001.

**Observações:** Opt-in do rateio não deve ser presumido como requisito equivalente da criação de divisão; ver PO-006.

## FUNC-020 — Divisões, responsabilidades e alocações

**Objetivo e valor para o negócio:** Agrupar despesas entre usuários e comparar o devido com pagamentos financeiros reais.

**Atores envolvidos:** Criador e participantes de divisão.

**Quando é utilizada:** Ao organizar contas de um grupo e associar despesas pagas.

**Pré-condições:** Grupo com criador incluído e participantes ativos; divisão ativa para associar; saída real elegível; permissão de criador para manter alocações.

**Entradas:** Nome, participantes ativos, percentuais opcionais; transação/base/responsabilidades; lista de transações e valores para alocação.

**Fluxo principal:** Criar grupo → associar saída elegível → congelar responsabilidades → criar alocação inicial → consultar resumo → substituir/cancelar alocações quando autorizado.

**Fluxos alternativos:** Sem percentuais divide igualmente; pode definir responsabilidades por despesa; vínculos históricos pendentes admitem revisão explícita.

**Exceções:** Criador ausente, percentuais incompletos, transação sem caixa, base excessiva, transação já vinculada ou alocação excessiva são recusados.

**Resultado esperado:** Devido/pago/saldo e situação pendente, parcial ou quitada da despesa disponíveis.

**Impacto no objetivo final:** Agrupar despesas entre usuários e comparar o devido com pagamentos financeiros reais. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-013, RN-014, RN-030. **Requisito:** RF-020.

**Permissões:** Participante associa saída própria; criador pode associar saída de participante e administrar composição/alocações.

**Integrações envolvidas:** INT-001.

**Observações:** Mudança do grupo não reescreve responsabilidades antigas. Quitação da despesa não significa que todos os participantes já compensaram entre si.

## FUNC-021 — Reembolsos e migração de compartilhamento

**Objetivo e valor para o negócio:** Registrar acertos reais entre participantes e transferir responsabilidades legadas para divisões quando permitido.

**Atores envolvidos:** Pagador participante; criador nos cancelamentos/migração.

**Quando é utilizada:** Ao compensar saldo de divisão ou converter compartilhamento legado elegível.

**Pré-condições:** Para reembolso, divisão ativa, saída própria eficaz e débito/crédito compatíveis; para migração, legado ativo elegível e divisão de destino autorizada.

**Entradas:** Divisão, saída real própria, recebedor, valor; migração com divisão destino e mapeamentos de externos.

**Fluxo principal:** Reembolso: validar dívida/crédito → vincular saída → atualizar compensação. Migração: validar destino/responsáveis → cancelar legado → associar divisão → preservar origem.

**Fluxos alternativos:** Criador pode cancelar referência do reembolso conservando histórico; mapeamento associa participantes externos antigos a usuários ativos.

**Exceções:** Reembolso acima do débito/crédito, saída inválida/reutilizada, migração com rateio pago/recusado/cancelado ou mapeamento incompleto são recusados.

**Resultado esperado:** Acerto rastreável ou responsabilidades migradas, sem nova saída financeira pela associação.

**Impacto no objetivo final:** Registrar acertos reais entre participantes e transferir responsabilidades legadas para divisões quando permitido. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-014, RN-029, RN-031. **Requisito:** RF-021.

**Permissões:** Reembolso pelo dono da saída; cancelamento pelo criador; migração exige criador do destino e acesso à despesa.

**Integrações envolvidas:** INT-001.

**Observações:** Registrar reembolso não cria automaticamente uma entrada na conta do recebedor; ver PO-011.

## FUNC-022 — Importação e revisão de PDF

**Objetivo e valor para o negócio:** Reduzir digitação com conferência humana antes de produzir efeitos financeiros.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Ao obter extrato, cobrança ou fatura em PDF legível.

**Pré-condições:** PDF com texto dentro dos limites, banco acessível e destino compatível; confirmação exige selecionados completos e sem pendência de revisão.

**Entradas:** PDF, banco, tipo pretendido, conta ou fatura compatível; revisão com dados por linha e justificativa.

**Fluxo principal:** Enviar → ler → apresentar prévia/incertezas/duplicidades → revisar → confirmar → consultar vínculos criados ou associados.

**Fluxos alternativos:** Ignorar linha; associar registro existente; crédito de fatura reduz documento; reenvio idêntico reaproveita contexto.

**Exceções:** PDF sem texto, limite excedido, destino incompatível, dados incertos não confirmados ou nenhuma linha selecionada bloqueiam avanço.

**Resultado esperado:** Extrato gera transação; cobrança gera obrigação; fatura gera gasto/crédito; associação não duplica caixa.

**Impacto no objetivo final:** Reduzir digitação com conferência humana antes de produzir efeitos financeiros. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-015, RN-016, RN-032. **Requisito:** RF-022.

**Permissões:** Titular do lote, destino e registros associados.

**Integrações envolvidas:** INT-001, INT-002.

**Observações:** Reconhecimento não garante suporte a todos os layouts bancários. Nome do arquivo não determina o tipo.

## FUNC-023 — Painéis, agenda e patrimônio

**Objetivo e valor para o negócio:** Permitir análise por período e visão da posição financeira com composição dos indicadores.

**Atores envolvidos:** Titular e participante em consolidações compartilhadas.

**Quando é utilizada:** Na conferência cotidiana e análise de períodos.

**Pré-condições:** Usuário autenticado; período/formato dentro do limite da consulta; participação nas divisões consultadas.

**Entradas:** Mês, ano, período, datas, filtros e paginação conforme consulta.

**Fluxo principal:** Consultar mensal/período/anual/balancete → conferir origens → consultar agenda, visão geral, patrimônio e compartilhados.

**Fluxos alternativos:** Anual apresenta os doze meses; períodos de resumo são 1, 3, 6 ou 12 meses; saldo compartilhado fica separado de conta.

**Exceções:** Datas/períodos inválidos e acesso indevido são recusados; inconsistências de apuração exigem leitura de PO-001 a PO-004.

**Resultado esperado:** Indicadores de caixa, competência, compromissos e patrimônio com suas composições.

**Impacto no objetivo final:** Permitir análise por período e visão da posição financeira com composição dos indicadores. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-002, RN-008, RN-012, RN-033. **Requisito:** RF-023.

**Permissões:** Dados próprios e divisões acessíveis.

**Integrações envolvidas:** INT-001.

**Observações:** Não representa fechamento contábil nem patrimônio histórico integral. Não usar saldo livre como garantia de disponibilidade bancária.

## FUNC-024 — Previsão mensal de fluxo

**Objetivo e valor para o negócio:** Reunir expectativas de entradas e saídas por mês/categoria segundo o cálculo disponível.

**Atores envolvidos:** Titular.

**Quando é utilizada:** Ao recalcular horizonte de acompanhamento de 1 a 12 meses.

**Pré-condições:** Usuário autenticado; horizonte de 1 a 12 meses para recálculo; consulta de resultados depende de projeção armazenada.

**Entradas:** Quantidade de meses; mês para consulta.

**Fluxo principal:** Recalcular a partir do mês operacional → reunir recorrências ativas, transações conhecidas não recorrentes e parcelas → substituir projeções → consultar.

**Fluxos alternativos:** Consulta lê projeção previamente armazenada; novo recálculo atualiza horizonte.

**Exceções:** Horizonte fora de 1 a 12 é recusado.

**Resultado esperado:** Projeção por mês/categoria disponível; não movimenta contas.

**Impacto no objetivo final:** Reunir expectativas de entradas e saídas por mês/categoria segundo o cálculo disponível. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-034. **Requisito:** RF-024.

**Permissões:** Titular.

**Integrações envolvidas:** INT-001.

**Observações:** A apuração não é idêntica ao dashboard de caixa e pode combinar previsto/realizado; PO-004 deve definir interpretação desejada.

## FUNC-025 — Exportação, pedido de anonimização e desativação

**Objetivo e valor para o negócio:** Dar ao titular acesso aos seus dados e registrar intenção de encerrar tratamento/acesso conforme capacidades atuais.

**Atores envolvidos:** Titular autenticado.

**Quando é utilizada:** Ao solicitar cópia de dados, anonimização ou desativação da conta de usuário.

**Pré-condições:** Titular autenticado; motivo válido para pedido; credenciais ainda utilizáveis antes de desativar.

**Entradas:** Para pedido, motivo não vazio de até 500 caracteres; demais operações usam identidade autenticada.

**Fluxo principal:** Exportar dados próprios ou registrar pedido → acompanhar solicitações; se desativar, revogar sessões e preservar fatos.

**Fluxos alternativos:** Pedido aberto é reaproveitado; exportação omite credenciais e hashes técnicos indicados.

**Exceções:** Acesso não autenticado ou motivo inválido é recusado; não há execução pública da anonimização.

**Resultado esperado:** Cópia disponível, pedido solicitado ou usuário inativo, conforme operação escolhida.

**Impacto no objetivo final:** Dar ao titular acesso aos seus dados e registrar intenção de encerrar tratamento/acesso conforme capacidades atuais. O resultado integra o acompanhamento descrito na jornada principal.

**Regras relacionadas:** RN-017, RN-018, RN-035. **Requisito:** RF-025.

**Permissões:** Somente titular.

**Integrações envolvidas:** INT-001.

**Observações:** São três ações diferentes. Pedido não desativa automaticamente a conta; desativação não anonimiza. Atendimento posterior depende de definição operacional.
