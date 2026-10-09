# Pontos para validação do Product Owner

[Voltar ao portal](00-README.md) · [Fontes e rastreabilidade](08-rastreabilidade.md)

Os achados abaixo resultam de análise estática. Não houve reprodução executada nem alteração de comportamento. “Inconsistência” indica diferença concreta nas fontes ou fórmulas; a decisão do comportamento desejado pertence ao PO. Os roteiros tornam cada ponto verificável para QA e desenvolvimento.

## PO-001 — Valor integral de obrigação parcialmente paga nos painéis

**Classificação:** ⚠️ Inconsistência funcional identificada estaticamente.

**Questão:** A agenda e a dívida patrimonial devem usar o saldo pendente após pagamentos/descontos?

**Contexto:** RF-009 e RF-023 já distinguem total e saldo pendente.

**Comportamento atualmente identificado:** PainelFinanceiroService.consultarAgenda, consultarAgendaComVencidos e consultarPatrimonio usam o valor original da obrigação. O adapter reconstrói valor e valorPago separadamente, sem substituir o valor original pelo saldo.

**O que precisa ser confirmado:** Qual indicador deve representar principal original e qual deve representar remanescente.

**Cenário de validação e impacto:** Criar obrigação de R$ 100, pagar R$ 40 e consultar agenda/patrimônio. O caminho lido usa R$ 100 onde o pendente é R$ 60; pode superestimar comprometimento/dívida e reduzir saldo livre.

**Origem para investigação:** PainelFinanceiroService.java; ObrigacaoFinanceiraPersistenceAdapter.java; ObrigacaoFinanceira.java. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-002 — Compra parcelada também incluída em fatura

**Classificação:** ⚠️ Possível dupla contagem sustentada pelos filtros atuais.

**Questão:** A agenda e o patrimônio devem excluir a parcela já representada pela fatura?

**Contexto:** Uma parcela pode ter simultaneamente vínculo de compra e fatura.

**Comportamento atualmente identificado:** PainelFinanceiroService soma faturas e, separadamente, transações com compraParceladaId, sem excluir as que possuem fatura. A consulta de transações por período retorna os lançamentos do usuário.

**O que precisa ser confirmado:** Regra única de representação do compromisso, inclusive após pagamento/cancelamento da fatura.

**Cenário de validação e impacto:** Criar compra em cartão de R$ 300 em três parcelas; consultar janela que contenha parcela e vencimento da mesma fatura. Investigar soma dupla de R$ 100 e persistência de parcela programada após quitação da fatura.

**Origem para investigação:** PainelFinanceiroService.java; CompraParceladaService.java; TransacaoPersistenceAdapter.java. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-003 — Ocorrência pendente deixa de aparecer como previsão na agenda

**Classificação:** ⚠️ Inconsistência de cobertura do compromisso.

**Questão:** Uma ocorrência gerada e ainda não realizada deve permanecer na agenda?

**Contexto:** Gerar ocorrência não paga nem movimenta caixa.

**Comportamento atualmente identificado:** A agenda adiciona recorrências somente quando não existe geração daquela competência; não há inclusão explícita das ocorrências pendentes no método consultado.

**O que precisa ser confirmado:** Como a agenda deve representar previsto, gerado pendente e realizado sem duplicidade.

**Cenário de validação e impacto:** Cadastrar saída mensal, consultar agenda, gerar competência e consultar de novo antes da realização. O caminho atual remove a previsão ao encontrar a chave gerada.

**Origem para investigação:** PainelFinanceiroService.java; RecorrenciaService.java. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-004 — Previsão e visão geral usam composições diferentes

**Classificação:** ⚠️ Inconsistência semântica a validar.

**Questão:** O que significa o indicador de resultado na visão geral e qual universo deve compor a previsão?

**Contexto:** RF-023 separa caixa/competência; RF-024 agrega registros conhecidos e expectativa.

**Comportamento atualmente identificado:** Visão geral soma a variação de capital de investimento ao resultado de competência do resumo. Previsão soma recorrências ativas, transações não recorrentes e parcelas, sem a mesma separação de transferências/pagamentos de fatura usada no dashboard.

**O que precisa ser confirmado:** Se a diferença é intencional; nomes, fórmulas e exemplos aprovados para cada indicador.

**Cenário de validação e impacto:** Comparar resumo/visão geral num mês com apenas aporte; recalcular previsão num mês com compra de cartão e pagamento de fatura, transferência ou parcela já paga. Verificar dupla contagem e classificação, sem chamar projeção de caixa realizado.

**Origem para investigação:** PainelFinanceiroService.java; PrevisaoFluxoCaixaService.java; DashboardFinanceiroPersistenceAdapter.java. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-005 — Saldo negativo e reversões bloqueadas

**Classificação:** ❓ Decisão de produto não justificada nos materiais.

**Questão:** O produto pretende permitir cheque especial, saldo devedor ou reversão que deixe saldo negativo?

**Contexto:** Conta usa valor monetário não negativo.

**Comportamento atualmente identificado:** Subtrair acima do saldo gera erro de valor negativo. Isso pode impedir registrar despesa real ou estornar entrada já consumida; não foi encontrada regra distinta por tipo de conta.

**O que precisa ser confirmado:** Política desejada, orientação ao usuário e forma de registrar dívida sem falsificar saldo.

**Cenário de validação e impacto:** Conta com R$ 10: tentar saída de R$ 20. Em outro caso, consumir entrada anterior e tentar estorná-la.

**Origem para investigação:** ValorMonetario.java; Conta.java; TransacaoService.java. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-006 — Consentimento, saída de participante e dois modelos de compartilhamento

**Classificação:** ❓ Política de colaboração incompleta.

**Questão:** Qual deve ser o consentimento para entrar numa divisão e qual acesso deve permanecer após sair?

**Contexto:** Rateio legado exige opt-in; divisão verifica usuários ativos e participação.

**Comportamento atualmente identificado:** DivisaoCompartilhadaService não usa a configuração de opt-in do rateio. Consulta exige participação atual; composição histórica de despesas pode incluir ex-participantes.

**O que precisa ser confirmado:** Modelo prioritário do produto, aceite para divisão, acesso após saída e manutenção em divisão inativa.

**Cenário de validação e impacto:** Comparar usuário sem opt-in nos dois modelos; remover participante com histórico e tentar consultar como ele. Não presumir que a retenção do histórico garante seu acesso.

**Origem para investigação:** DivisaoCompartilhadaService.java; DespesaCompartilhadaService.java; ConfiguracaoCompartilhamentoService.java. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-007 — Realização recorrente após estorno

**Classificação:** ⚠️ Fluxo incompleto e divergência de linguagem documental.

**Questão:** Como reabrir ocorrência realizada quando sua transação foi estornada?

**Contexto:** Ocorrência tem somente pendente e realizada; pagamento usa data operacional.

**Comportamento atualmente identificado:** TransacaoService estorna a transação sem atualizar ocorrência. RecorrenciaService.realizar rejeita estado diferente de pendente. Documento técnico chama a baixa de idempotente, mas repetição retorna erro.

**O que precisa ser confirmado:** Reabertura, preservação do vínculo, possibilidade de refazer baixa e significado de repetição segura.

**Cenário de validação e impacto:** Gerar, realizar, estornar transação e tentar realizar de novo. Conferir estado e vínculo; não assumir sucesso idempotente no segundo comando.

**Origem para investigação:** RecorrenciaService.java; TransacaoService.java; docs/regras-negocio.md. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-008 — Liquidação de compra parcelada sem cartão

**Classificação:** ❓ Lacuna funcional.

**Questão:** Como marcar cada parcela sem cartão como paga sem duplicar gasto ou compromisso?

**Contexto:** Compra sem cartão gera transação programada que não altera saldo.

**Comportamento atualmente identificado:** Controller oferece criação, consulta e cancelamento da compra, sem baixa individual. Cancelamento sem cartão alcança apenas parcelas futuras.

**O que precisa ser confirmado:** Se esse fluxo legado deve continuar e qual é o vínculo correto com liquidação real.

**Cenário de validação e impacto:** Criar compra sem cartão, atingir vencimento e procurar operação de baixa no inventário. Registrar saída manual por si só não prova encerramento do compromisso original.

**Origem para investigação:** CompraParceladaController.java; CompraParceladaService.java; Transacao.java. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-009 — Conclusão ordinária do financiamento e atrasos

**Classificação:** ❓ Transições operacionais não demonstradas.

**Questão:** Pagar a última parcela deve finalizar contrato? Como e quando se processam atrasos de parcelas e obrigações?

**Contexto:** Há serviços para atrasos e estados de contrato; rotina agendada localizada é a de recorrências.

**Comportamento atualmente identificado:** Pagamento de parcela salva parcela paga e saída, sem finalizar financiamento no método. Finalização existe em refinanciamento e amortização quitante. Não foi identificado agendamento dedicado dos atrasos no inventário de infraestrutura.

**O que precisa ser confirmado:** Evento terminal, necessidade de atualização automática de atraso e comportamento de legado sem vínculo financeiro.

**Cenário de validação e impacto:** Pagar todas as parcelas e consultar contrato; comparar data vencida com estado persistido antes/depois de processamento. Validar em aplicação antes de concluir alcance.

**Origem para investigação:** ParcelaFinanciamentoService.java; FinanciamentoService.java; ObrigacaoFinanceiraService.java. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-010 — Alterar transação que participa de divisão

**Classificação:** ⚠️ Proteções diferentes entre compartilhamentos.

**Questão:** Correção/estorno deve ser bloqueado em toda transação alocada ou deve haver reavaliação explícita da divisão?

**Contexto:** O modelo legado bloqueia alterações com compartilhamento ativo; divisão utiliza pagamentos reais e snapshots.

**Comportamento atualmente identificado:** TransacaoService verifica despesa compartilhada legada e suas linhas; não consulta diretamente alocações/vínculos de divisão. Serviços de divisão filtram pagamentos válidos, mas responsabilidade histórica pode permanecer.

**O que precisa ser confirmado:** Comportamento de edição de valor/data/estorno e como comunicar diferença de responsabilidade/pagamento.

**Cenário de validação e impacto:** Associar saída de R$ 200 a divisão, corrigir seu valor ou estornar e consultar resumo/alocações. Reproduzir alcance real antes de propor solução.

**Origem para investigação:** TransacaoService.java; DivisaoCompartilhadaService.java. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-011 — Reembolso e recebimento do credor

**Classificação:** ❓ Limite do processo.

**Questão:** O recebedor deve registrar entrada separada, confirmar recebimento ou ter associação entre contas?

**Contexto:** Reembolso exige saída real do pagador.

**Comportamento atualmente identificado:** RegistrarReembolso salva referência e ajusta saldo compartilhado; não cria entrada na conta do recebedor. Cancelar referência também não estorna a saída.

**O que precisa ser confirmado:** Como representar recebimento sem duplicar operação, classificação dos reembolsos nos painéis e confirmação entre pessoas.

**Cenário de validação e impacto:** Executar exemplo de Ana/Bruno e conferir ambas as contas e o resumo do grupo; não apresentar saldo compartilhado zerado como prova de recebimento bancário.

**Origem para investigação:** DivisaoCompartilhadaService.java. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-012 — Privacidade depois do pedido/desativação

**Classificação:** ❓ Fluxo operacional sem conclusão disponível.

**Questão:** Quem analisa e conclui anonimização, em qual prazo e por qual canal após desativação?

**Contexto:** Estados de análise/conclusão existem no modelo; não há papel administrativo encontrado.

**Comportamento atualmente identificado:** Pedido é registrado/reutilizado, mas não executa anonimização. Desativação impede continuar consultando pelo acesso anterior e conserva identidade.

**O que precisa ser confirmado:** Responsável, canal alternativo, política aprovada, autorização e transições; não presumir prazo ou obrigação jurídica específica.

**Cenário de validação e impacto:** Registrar pedido, desativar e verificar restrição de acesso; obter decisão operacional antes de descrever atendimento completo.

**Origem para investigação:** PerfilUsuarioService.java; SolicitacaoPrivacidade.java; docs/privacidade-ciclo-vida.md. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-013 — Objetivo comercial, qualidade e experiência de uso

**Classificação:** ❓ Requisitos não identificados.

**Questão:** Quem é o público prioritário e como se mede sucesso, qualidade e confiabilidade do produto?

**Contexto:** O domínio sustenta finanças pessoais, mas não define metas de negócio ou operação.

**Comportamento atualmente identificado:** Não foram encontradas metas comprovadas de disponibilidade, recuperação, desempenho, retenção comercial ou homologação de todas as telas nesta análise.

**O que precisa ser confirmado:** Personas prioritárias, KPIs, SLOs, suporte, backup/recuperação, acessibilidade e critérios de liberação.

**Cenário de validação e impacto:** Workshop com PO, operação e QA; definir medidas observáveis sem converter inferências deste portal em metas aprovadas.

**Origem para investigação:** README.md; docs/testes.md; inventário do checkout. Caminhos completos na [matriz](08-rastreabilidade.md).

## PO-014 — Divergências na documentação técnica anterior

**Classificação:** ⚠️ Inconsistência documental.

**Questão:** Qual redação técnica deve ser consolidada após validar os fluxos atuais?

**Contexto:** O portal funcional usa o código atual quando a narrativa técnica generaliza.

**Comportamento atualmente identificado:** Há trecho dizendo que cada linha referencia catálogo embora linha livre seja aceita; estorno de fatura é descrito como sempre voltando a fechada, apesar de pagamentos/créditos remanescentes; pagamento de rateio aparece na apresentação mas serviço o bloqueia.

**O que precisa ser confirmado:** Atualização desses documentos em entrega própria ou junto à próxima evolução, mantendo as qualificações deste portal.

**Cenário de validação e impacto:** Comparar documentos com RN-009, RN-020 e RN-029; não usar resumo técnico antigo para sobrepor comportamento comprovado.

**Origem para investigação:** docs/regras-negocio.md; README.md; FaturaService.java; RateioDespesaService.java. Caminhos completos na [matriz](08-rastreabilidade.md).

## Regras inferidas

Nenhuma inferência foi promovida a regra obrigatória de comportamento. São inferências a motivação de reduzir controles dispersos, o valor de negócio atribuído às funcionalidades e a interpretação de conclusão do acompanhamento mensal. Todas precisam de confirmação do PO, especialmente PO-013.

## Comportamentos aparentemente inconsistentes

Priorizar PO-001, PO-002 e PO-003 porque podem alterar a percepção de compromisso e saldo livre. PO-004 trata de significados distintos de indicadores. PO-007 e PO-010 tratam de coerência entre operação financeira e estado/vínculo. PO-014 é divergência entre narrativa existente e implementação.

## Lacunas de requisitos

PO-005 (saldo devedor), PO-006 (consentimento/acesso histórico), PO-008 (baixa sem cartão), PO-009 (conclusão/atrasos), PO-011 (recebimento de reembolso), PO-012 (atendimento de privacidade) e PO-013 (objetivos e qualidade).

Não presumir requisitos de aviso externo, orçamento, metas financeiras, recuperação de senha, multimoeda ou fechamento contábil pelo fato de serem comuns em outros produtos.

## Funcionalidades que merecem documentação adicional

Após decisões do PO: fórmulas aprovadas dos indicadores; exemplos completos de cada modalidade de financiamento e arredondamento; catálogo homologado de layouts bancários; jornada real nas telas; procedimento de atendimento de privacidade e recuperação operacional.

Esses assuntos estão documentados aqui até o limite do comportamento comprovado. Documentação adicional depende de decisão, material externo ou execução de homologação.

## Pontos cujo objetivo de negócio não pôde ser identificado

O motivo de manter simultaneamente os dois modelos de compartilhamento, a previsão sem baixa da compra parcelada sem cartão, o encerramento legado de refinanciamento sem sucessor e a diferença de resultado de competência da visão geral precisam de justificativa explícita. As capacidades existem; não se inventa sua razão histórica.

## Sugestões de assuntos a discutir com o time

1. Definir um exemplo financeiro de referência atravessando obrigação parcial, cartão parcelado, recorrência e investimento; aprovar a fórmula de cada indicador.
2. Escolher o modelo de compartilhamento prioritário e o plano de convivência/migração.
3. Definir eventos terminais e reversões de ocorrência, financiamento e compra sem cartão.
4. Separar no vocabulário do produto registro de pagamento, pagamento bancário e confirmação de recebimento.
5. Aprovar responsabilidades de privacidade e continuidade operacional.
6. Homologar jornadas com frontend e MySQL real antes de afirmar prontidão operacional.

## Ordem sugerida para validação

Primeiro valores exibidos e coerência financeira (PO-001 a PO-004, PO-010); depois ciclos de liquidação (PO-005, PO-007 a PO-009, PO-011); em seguida colaboração, privacidade e objetivos de qualidade (PO-006, PO-012, PO-013), consolidando a narrativa técnica em PO-014. Essa ordem é uma recomendação de análise, não prioridade de roadmap aprovada.
