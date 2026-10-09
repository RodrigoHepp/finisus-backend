# Regras de negócio

[Voltar ao portal](00-README.md) · [Rastreabilidade](08-rastreabilidade.md)

RN-001 a RN-018 mantêm os temas do catálogo técnico anterior, com qualificações do comportamento atual. O motivo abaixo é uma interpretação funcional (💡), pois não foram encontradas atas de decisão de produto para cada regra. A origem detalhada, com links verificáveis, está na matriz. Não se atribui um requisito jurídico a nenhuma regra.

## RN-001 — Acesso por titularidade

**Regra:** Dados financeiros próprios exigem titular autenticado; acesso compartilhado depende da operação e participação.

**Motivo da regra:** 💡 preservar a coerência do processo de acesso por titularidade e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Leituras e alterações protegidas.

**Resultado:** Recurso alheio não fica disponível pelo conhecimento de seu identificador.

**Exceções e limites:** Participação compartilhada e bancos globais de leitura têm escopos próprios.

**Funcionalidades relacionadas:** FUNC-001–025. **Requisitos relacionados:** RF-001–025. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Identidade, proprietário, participante.

**Origem:** SecurityConfig; ResourceOwnershipValidator; serviços; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-002 — Caixa e competência separados

**Regra:** Somente registros com efeito em saldo movimentam conta. Gastos de fatura e parcelas programadas não debitam no registro.

**Motivo da regra:** 💡 preservar a coerência do processo de caixa e competência separados e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Registro e análise financeira.

**Resultado:** Saldo muda na realização; análises distinguem o período econômico.

**Exceções e limites:** Consultas de previsão/agenda têm fórmulas próprias e inconsistências, não equivalem ao caixa.

**Funcionalidades relacionadas:** FUNC-004–017, 023. **Requisitos relacionados:** RF-004–017, 023. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Tipo, origem, data, fatura, compra e valor.

**Origem:** Transacao.impactaSaldoDaConta; serviços de transação/fatura; DashboardFinanceiroPersistenceAdapter; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-003 — Transferência própria íntegra

**Regra:** Origem e destino devem ser contas próprias, distintas e ativas; débito e crédito são registrados conjuntamente. Mesmo identificador/conteúdo não repete efeito.

**Motivo da regra:** 💡 preservar a coerência do processo de transferência própria íntegra e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Transferência e seu estorno.

**Resultado:** Duas pontas vinculadas; reversão conjunta; operação fora de receita/despesa operacional.

**Exceções e limites:** Mesmo identificador com conteúdo diferente é conflito; não se aplica a transferências entre pessoas.

**Funcionalidades relacionadas:** FUNC-007. **Requisitos relacionados:** RF-007. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Contas, valor, data, descrição, chave.

**Origem:** TransferenciaContaService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-004 — Ajuste auditável de saldo

**Regra:** Saldo informado, motivo e identificador são necessários. Registrar ajuste não cria receita/despesa; repetição idêntica é reaproveitada.

**Motivo da regra:** 💡 preservar a coerência do processo de ajuste auditável de saldo e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Ajuste após consulta ou saldo de partida.

**Resultado:** Saldo atualizado e histórico com valores anteriores/calculados.

**Exceções e limites:** A consulta não corrige sozinha; ajuste não exige conta ativa no serviço atual.

**Funcionalidades relacionadas:** FUNC-008. **Requisitos relacionados:** RF-008. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Conta, saldo informado/anterior/calculado, motivo.

**Origem:** AjusteSaldoContaService; ReconciliacaoSaldoContaService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-005 — Correção com motivo e memória

**Regra:** Correção e detalhamento exigem motivo; guardam autoria, instante e fotografias antes/depois.

**Motivo da regra:** 💡 preservar a coerência do processo de correção com motivo e memória e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Transação elegível para correção/detalhamento.

**Resultado:** É possível identificar o conteúdo alterado e seu responsável.

**Exceções e limites:** Origem/estado podem impedir a operação; consultar histórico não altera dados.

**Funcionalidades relacionadas:** FUNC-005–006. **Requisitos relacionados:** RF-005–006. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Motivo, transação, itens, autor.

**Origem:** TransacaoService; ConsultarHistoricoTransacaoService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-006 — Hierarquia de categorias

**Regra:** Até cinco níveis; sem ciclos; pai deve estar ativo. Inativar pai exige não haver descendentes ativos.

**Motivo da regra:** 💡 preservar a coerência do processo de hierarquia de categorias e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Criar, mover e inativar categoria; selecionar referência.

**Resultado:** Estrutura válida e histórico preservado.

**Exceções e limites:** Categoria inativa pode continuar no histórico; não pode ser nova classificação.

**Funcionalidades relacionadas:** FUNC-003. **Requisitos relacionados:** RF-003. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Categoria, pai, descendentes, ativo.

**Origem:** CategoriaService; CategoriaAtivaValidator; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-007 — Recorrência antes de caixa

**Regra:** Geração cria ocorrência pendente única no mês, sem alterar saldo. Realização explícita gera a movimentação.

**Motivo da regra:** 💡 preservar a coerência do processo de recorrência antes de caixa e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Geração e baixa da ocorrência.

**Resultado:** Previsão separada do fato realizado.

**Exceções e limites:** Nova realização retorna erro; não é retorno idêntico bem-sucedido. Conta inativa é pulada na geração.

**Funcionalidades relacionadas:** FUNC-010. **Requisitos relacionados:** RF-010. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Recorrência, competência, ocorrência, conta.

**Origem:** RecorrenciaService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-008 — Compra no cartão e competência

**Regra:** Gasto pertence ao mês de referência da fatura; pagamento pertence ao caixa na data do pagamento.

**Motivo da regra:** 💡 preservar a coerência do processo de compra no cartão e competência e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Lançamento e consultas por período.

**Resultado:** Compra e quitação podem estar em períodos diferentes sem duplicar consumo nos dashboards próprios.

**Exceções e limites:** Compra parcelada sem cartão é previsão legada; indicadores de agenda/patrimônio exigem PO-002.

**Funcionalidades relacionadas:** FUNC-011–013, 023. **Requisitos relacionados:** RF-011–013, 023. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Compra, fatura, competência, pagamento.

**Origem:** FaturaService; CompraParceladaService; DashboardFinanceiroPersistenceAdapter; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-009 — Pagamentos e créditos de fatura

**Regra:** Fatura fechada admite pagamentos positivos parciais e excesso. Cada solicitação identificada debita uma vez; excesso vira crédito para competências posteriores.

**Motivo da regra:** 💡 preservar a coerência do processo de pagamentos e créditos de fatura e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Pagamento, fechamento e estorno.

**Resultado:** Parcial mantém fechada; suficiente paga; crédito aplicado não cria caixa novo.

**Exceções e limites:** Estorno atua no pagamento ativo mais recente; crédito transportado impede estorno isolado da origem.

**Funcionalidades relacionadas:** FUNC-012. **Requisitos relacionados:** RF-012. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Total, pago, aberto, crédito, conta, chave.

**Origem:** FaturaService; PagamentoFatura; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-010 — Obrigação não é pagamento

**Regra:** Criar ou vencer obrigação não altera conta. Abatimento é valor base mais desconto; caixa é valor base mais juros e encargos.

**Motivo da regra:** 💡 preservar a coerência do processo de obrigação não é pagamento e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Criação, pagamento e estorno de obrigação.

**Resultado:** Saldo pendente reduz pelo abatimento; conta reduz pelo desembolso.

**Exceções e limites:** Abatimento não ultrapassa pendente; caixa e abatimento precisam ser positivos; desconto integral sem desembolso não é aceito.

**Funcionalidades relacionadas:** FUNC-009. **Requisitos relacionados:** RF-009. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Valor base, desconto, juros, encargos, saldo.

**Origem:** ObrigacaoFinanceiraService; ObrigacaoFinanceira; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-011 — Memória do financiamento

**Regra:** Preservar cronograma antes de amortização, refinanciamento ou correção; novas condições não reescrevem parcelas pagas.

**Motivo da regra:** 💡 preservar a coerência do processo de memória do financiamento e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Mudança das condições do contrato.

**Resultado:** Versão histórica consultável e cronograma corrente atualizado.

**Exceções e limites:** Correção por erro é bloqueada havendo parcela paga; refinanciamento legado difere do completo.

**Funcionalidades relacionadas:** FUNC-014–015. **Requisitos relacionados:** RF-014–015. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Contrato, parcelas, versões, sucessor.

**Origem:** FinanciamentoService; ParcelaFinanciamentoService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-012 — Capital e posição distintos

**Regra:** Aporte/resgate são movimentos patrimoniais; valor observado vem de posição manual datada. Rendimento e taxa são tipos explícitos.

**Motivo da regra:** 💡 preservar a coerência do processo de capital e posição distintos e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Investimento e patrimônio.

**Resultado:** Sem posição, mostrar capital líquido como alternativa, sem inferir valorização.

**Exceções e limites:** Consulta patrimonial não reconstrói saldos e dívidas históricos; posição não gera recebimento.

**Funcionalidades relacionadas:** FUNC-016–017, 023. **Requisitos relacionados:** RF-016–017, 023. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Movimento, capital, posição, referência.

**Origem:** MovimentoInvestimentoService; PosicaoInvestimentoService; PainelFinanceiroService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-013 — Responsabilidade histórica

**Regra:** Na divisão, base e responsabilidades ficam congeladas por despesa. Alterar composição do grupo não redistribui despesas anteriores.

**Motivo da regra:** 💡 preservar a coerência do processo de responsabilidade histórica e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Associar despesa e alterar participantes.

**Resultado:** Histórico mantém o acordo original.

**Exceções e limites:** Vínculos legados pendentes de revisão exigem responsabilidade explícita; não reconstruir pelo grupo atual.

**Funcionalidades relacionadas:** FUNC-018, 020. **Requisitos relacionados:** RF-018, 020. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Base, participantes, percentuais/valores, fotografia.

**Origem:** DivisaoCompartilhadaService; histórico e vínculos; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-014 — Pagamento compartilhado comprovado

**Regra:** Alocações e reembolsos referenciam transações reais; sua associação não cria segunda movimentação.

**Motivo da regra:** 💡 preservar a coerência do processo de pagamento compartilhado comprovado e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Pagamento e compensação em divisão.

**Resultado:** Pago e devido são apurados sem caixa paralelo.

**Exceções e limites:** Saldo compartilhado não é entrada automática na conta; rateio legado não pode ser marcado pago diretamente.

**Funcionalidades relacionadas:** FUNC-020–021. **Requisitos relacionados:** RF-020–021. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Transação, pagador, recebedor, valor alocado.

**Origem:** DivisaoCompartilhadaService; RateioDespesaService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-015 — Importação depende de decisão

**Regra:** Leitura produz prévia, não movimento. Revisão decide criar, ignorar ou associar; confirmação processa selecionados aptos.

**Motivo da regra:** 💡 preservar a coerência do processo de importação depende de decisão e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Envio, revisão e confirmação.

**Resultado:** Vínculos existentes não produzem novos efeitos.

**Exceções e limites:** Não confirmar sem selecionados ou com linhas incertas; lote confirmado não volta à revisão.

**Funcionalidades relacionadas:** FUNC-022. **Requisitos relacionados:** RF-022. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Documento, linha, decisão, justificativa, destino.

**Origem:** ImportacaoFinanceiraService; ImportacaoFinanceira; LancamentoImportado; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-016 — Documento repetido

**Regra:** Mesmo conteúdo no mesmo usuário/banco/contexto reaproveita lote; confirmar novamente devolve resultado existente.

**Motivo da regra:** 💡 preservar a coerência do processo de documento repetido e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Reenvio e reconfirmação.

**Resultado:** Não cria outro lote/efeito no mesmo contexto.

**Exceções e limites:** Intenção ou destino divergentes conflitam; outro usuário/banco não é a mesma identidade de importação.

**Funcionalidades relacionadas:** FUNC-022. **Requisitos relacionados:** RF-022. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Conteúdo, banco, usuário, intenção, destino.

**Origem:** ImportacaoFinanceiraService; importação persistida; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-017 — Desativação preserva fatos

**Regra:** Desativar usuário torna acesso inválido, revoga sessões e preserva dados identificados e financeiros.

**Motivo da regra:** 💡 preservar a coerência do processo de desativação preserva fatos e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Desativação do próprio perfil.

**Resultado:** Usuário inativo; históricos continuam registrados.

**Exceções e limites:** Não equivale a excluir/anonimizar nem determina execução de pedido de privacidade.

**Funcionalidades relacionadas:** FUNC-001, 025. **Requisitos relacionados:** RF-001, 025. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Usuário, sessões e histórico.

**Origem:** PerfilUsuarioService; UsuarioSessaoJwtValidator; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-018 — Exportação sem credenciais

**Regra:** Exportação entrega seções dos dados do titular sem senha, tokens ou hashes técnicos de idempotência.

**Motivo da regra:** 💡 preservar a coerência do processo de exportação sem credenciais e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Solicitação autenticada de dados.

**Resultado:** Cópia versionada com instante da exportação.

**Exceções e limites:** Não prova anonimização nem substitui política de retenção; novos módulos precisam integrar o inventário.

**Funcionalidades relacionadas:** FUNC-025. **Requisitos relacionados:** RF-025. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Perfil, cadastros e históricos próprios.

**Origem:** PerfilUsuarioService; DadosPessoaisJdbcAdapter; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-019 — Saldos não negativos

**Regra:** Conta nasce com zero; subtração que resultaria em valor monetário negativo é recusada pelo modelo de valor.

**Motivo da regra:** 💡 preservar a coerência do processo de saldos não negativos e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Débito, reversão de crédito ou saldo informado.

**Resultado:** Operação não pode persistir saldo negativo pelos fluxos analisados.

**Exceções e limites:** Saldo compartilhado e resultados analíticos podem ser negativos; não confundir com saldo da conta.

**Funcionalidades relacionadas:** FUNC-002, 004, 007–010, 012, 014–016. **Requisitos relacionados:** RF-002, 004, 007–010, 012, 014–016. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Saldo, entrada, saída.

**Origem:** Conta; ValorMonetario; serviços de caixa; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-020 — Composição de itens

**Regra:** Se há itens, sua soma deve ser exatamente o valor da transação. Linha livre exige descrição; quantidade opcional deve ser positiva; snapshot preserva dados.

**Motivo da regra:** 💡 preservar a coerência do processo de composição de itens e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Criação e detalhamento.

**Resultado:** Itens explicam o total sem recalcular saldo no detalhamento.

**Exceções e limites:** Catálogo é opcional; descrição/categoria históricas não seguem automaticamente edição cadastral.

**Funcionalidades relacionadas:** FUNC-003–004, 006, 011. **Requisitos relacionados:** RF-003–004, 006, 011. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Descrição, quantidade, valor, categoria, catálogo.

**Origem:** Transacao; TransacaoItem; TransacaoService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-021 — Origem governa alteração

**Regra:** Correção genérica não modifica transações vinculadas a fatura, compra, recorrência ou transferência; liquidações de obrigação/parcela usam seu módulo.

**Motivo da regra:** 💡 preservar a coerência do processo de origem governa alteração e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Correção, detalhamento e estorno.

**Resultado:** Preserva coerência entre movimento e processo de origem.

**Exceções e limites:** Detalhamento admite gasto de cartão e recorrência; estorno genérico tem condições próprias, não iguais à correção.

**Funcionalidades relacionadas:** FUNC-005–006. **Requisitos relacionados:** RF-005–006. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Vínculos, estado de estorno e compartilhamento.

**Origem:** Transacao; TransacaoService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-022 — Reversão e cancelamento de obrigação

**Regra:** Estorno individual desfaz um pagamento ativo e recompõe pendente; comando legado desfaz todos os ativos. Cancelamento exige nenhum valor abatido.

**Motivo da regra:** 💡 preservar a coerência do processo de reversão e cancelamento de obrigação e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Reversão/cancelamento.

**Resultado:** Reabertura em aberto ou vencida segundo a data operacional.

**Exceções e limites:** Obrigação parcialmente paga não pode ser cancelada antes de estornar pagamentos.

**Funcionalidades relacionadas:** FUNC-009. **Requisitos relacionados:** RF-009. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Pagamentos, vencimento, data operacional.

**Origem:** ObrigacaoFinanceiraService; ObrigacaoFinanceira; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-023 — Competência e data da recorrência

**Regra:** Gerar apenas mês atual/anterior; limitar dia ao fim do mês; realizar usa a data operacional e os dados congelados da ocorrência.

**Motivo da regra:** 💡 preservar a coerência do processo de competência e data da recorrência e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Geração/realização.

**Resultado:** Uma ocorrência por mês e transação na data efetiva do comando.

**Exceções e limites:** Não há data retroativa no comando nem reabertura automática por estorno de transação.

**Funcionalidades relacionadas:** FUNC-010. **Requisitos relacionados:** RF-010. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Dia, competência, fotografia e data operacional.

**Origem:** RecorrenciaService; OcorrenciaRecorrencia; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-024 — Ciclo e limite do cartão

**Regra:** Novos gastos exigem cartão ativo e fatura aberta; soma verificada não supera limite. Fechamento no ciclo cria próxima competência de cartão ativo.

**Motivo da regra:** 💡 preservar a coerência do processo de ciclo e limite do cartão e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Gasto/compra e processamento de ciclos.

**Resultado:** Datas respeitam meses curtos; competência por cartão é única.

**Exceções e limites:** Limite é verificado por fatura no fluxo atual; não presumir crédito disponível global. Vencimento passa ao mês seguinte quando dia não é posterior ao fechamento.

**Funcionalidades relacionadas:** FUNC-011–013. **Requisitos relacionados:** RF-011–013. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Limite, dias, datas, competência.

**Origem:** FaturaService; CompraParceladaService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-025 — Parcelamento e cancelamento seletivo

**Regra:** Distribuir valor com residual na última parcela. Com cartão vincular a faturas; sem cartão manter previsão sem caixa.

**Motivo da regra:** 💡 preservar a coerência do processo de parcelamento e cancelamento seletivo e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Criar/cancelar compra.

**Resultado:** Total preservado; cancelamento só estorna parcelas elegíveis.

**Exceções e limites:** Com cartão: fatura aberta. Sem cartão: data estritamente futura; demais parcelas permanecem.

**Funcionalidades relacionadas:** FUNC-013. **Requisitos relacionados:** RF-013. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Valor total, quantidade, datas, fatura.

**Origem:** CompraParceladaService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-026 — Patrimônio sem dupla custódia

**Regra:** Custódia deve ser aplicação ativa própria, distinta da origem e exclusiva de um investimento; painel exclui seu saldo ao considerar a posição.

**Motivo da regra:** 💡 preservar a coerência do processo de patrimônio sem dupla custódia e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Cadastro de investimento e apuração patrimonial.

**Resultado:** Representação do mesmo ativo não é somada duas vezes como conta e investimento.

**Exceções e limites:** Contas/investimentos inativos com valor residual continuam; dívidas de compras parceladas têm ressalva PO-002.

**Funcionalidades relacionadas:** FUNC-002, 016–017, 023. **Requisitos relacionados:** RF-002, 016–017, 023. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Custódia, posição, capital, contas inativas.

**Origem:** InvestimentoService; PainelFinanceiroService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-027 — Limites da renegociação

**Regra:** Amortização não supera principal pendente; redução de prestação mantém quantidade; redução de prazo busca menor prazo compatível com prestação vigente.

**Motivo da regra:** 💡 preservar a coerência do processo de limites da renegociação e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Amortização, recálculo e cancelamento.

**Resultado:** Somente pendentes recalculadas; quitação por amortização finaliza; histórico preservado.

**Exceções e limites:** Não misturar modalidade e quantidade legada; cancelamento exige ausência de pagas; correção não remove única parcela.

**Funcionalidades relacionadas:** FUNC-014–015. **Requisitos relacionados:** RF-014–015. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Principal, juros, parcelas, modalidade.

**Origem:** FinanciamentoService; ParcelaFinanciamentoService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-028 — Rateio por alvo e consentimento

**Regra:** Compartilhar transação inteira ou item, sem sobreposição ativa; responsabilidades fecham base/100%; participante novo deve ser interno e ter opt-in.

**Motivo da regra:** 💡 preservar a coerência do processo de rateio por alvo e consentimento e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Criar compartilhamento legado.

**Resultado:** Base do item pode ser parcial; parte não compartilhada fica individual.

**Exceções e limites:** Externos antigos podem existir, mas novos são bloqueados; divisão não tem a mesma verificação de opt-in.

**Funcionalidades relacionadas:** FUNC-018–019. **Requisitos relacionados:** RF-018–019. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Alvo, base, modalidade, participante, consentimento.

**Origem:** DespesaCompartilhadaService; RateioDespesa; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-029 — Pagamento legado e migração

**Regra:** Marcação direta de rateio como pago é bloqueada. Migração exige despesa ativa, divisão ativa do criador, responsabilidades válidas e mapeamento exato de externos.

**Motivo da regra:** 💡 preservar a coerência do processo de pagamento legado e migração e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Pagar rateio antigo ou migrar.

**Resultado:** Erro no pagamento fictício; migração conserva origem e cancela legado ao associar destino.

**Exceções e limites:** Rateios pagos, recusados ou cancelados impedem migração; não pressupor migração de múltiplos itens da mesma transação sem conflito.

**Funcionalidades relacionadas:** FUNC-019, 021. **Requisitos relacionados:** RF-019, 021. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Despesa, rateios, usuários e destino.

**Origem:** RateioDespesaService; DespesaCompartilhadaService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-030 — Alocação limitada à despesa

**Regra:** Divisão exige criador participante e usuários ativos; percentuais todos presentes somam 100 ou todos ausentes. Base é positiva e não supera saída eficaz. Alocações não superam base nem valor disponível da transação.

**Motivo da regra:** 💡 preservar a coerência do processo de alocação limitada à despesa e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Criar divisão, associar e substituir alocações.

**Resultado:** Devido e pago apurados; centavos distribuídos sem perder total.

**Exceções e limites:** Somente criador mantém/cancela alocações; participante associa saída própria; gasto de cartão sem caixa é recusado.

**Funcionalidades relacionadas:** FUNC-020. **Requisitos relacionados:** RF-020. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Participantes, base, responsabilidades, alocações.

**Origem:** DivisaoCompartilhada; DivisaoCompartilhadaService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-031 — Reembolso limitado ao acerto

**Regra:** Pagador usa saída própria ativa com caixa; recebedor é outro participante; valor não supera débito do pagador nem crédito do recebedor até a data da transação.

**Motivo da regra:** 💡 preservar a coerência do processo de reembolso limitado ao acerto e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Registrar/cancelar reembolso.

**Resultado:** Compensação registrada sem novo débito.

**Exceções e limites:** Saída já usada em reembolso ativo é recusada; cancelamento de referência pelo criador não estorna automaticamente transação.

**Funcionalidades relacionadas:** FUNC-021. **Requisitos relacionados:** RF-021. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Saída, participantes, valor, data, saldos.

**Origem:** DivisaoCompartilhadaService.registrarReembolso; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-032 — Limites e contexto da importação

**Regra:** PDF não vazio de até 10 MB com texto legível; destino compatível; linhas revisadas com justificativa. Associação exige correspondência e acesso ao registro.

**Motivo da regra:** 💡 preservar a coerência do processo de limites e contexto da importação e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Leitura, revisão, associação e confirmação.

**Resultado:** Cobrança vira obrigação; extrato vira movimento; fatura vira gasto/crédito; associação preserva caixa.

**Exceções e limites:** OCR não suportado. Sugestão em janela de três dias não dispensa correspondência exata requerida para associação. Limites adicionais são configuráveis.

**Funcionalidades relacionadas:** FUNC-022. **Requisitos relacionados:** RF-022. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Arquivo, banco, tipo, destino, data/tipo/valor.

**Origem:** ImportacaoFinanceiraService; LeitorDocumentoFinanceiroPdfAdapter; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-033 — Limites e significado dos painéis

**Regra:** Resumo aceita 1, 3, 6 ou 12 meses; anual contém 12 meses; agenda/compartilhados do painel até 366 dias inclusivos; visão geral aceita janela de 1 a 90 dias.

**Motivo da regra:** 💡 preservar a coerência do processo de limites e significado dos painéis e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Consultas analíticas.

**Resultado:** Indicadores com composição; patrimônio declara ausência de histórico integral.

**Exceções e limites:** A janela da visão geral consulta da referência até referência mais dias, inclusivamente. Fórmulas atuais e inconsistências em PO-001 a PO-004.

**Funcionalidades relacionadas:** FUNC-023. **Requisitos relacionados:** RF-023. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Datas, períodos, saldos, dívidas, posições.

**Origem:** DashboardFinanceiroService; PainelFinanceiroService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-034 — Previsão armazenada

**Regra:** Recálculo de 1 a 12 meses começa no mês operacional e combina recorrências ativas, transações não recorrentes não estornadas e parcelas retornadas pelo repositório.

**Motivo da regra:** 💡 preservar a coerência do processo de previsão armazenada e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Recalcular projeção.

**Resultado:** Substitui projeção por mês/categoria e não altera caixa.

**Exceções e limites:** Não usa a mesma separação de origens do dashboard; não garante ausência de dupla contagem entre previsto e realizado.

**Funcionalidades relacionadas:** FUNC-024. **Requisitos relacionados:** RF-024. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Horizonte, categoria, registros conhecidos.

**Origem:** PrevisaoFluxoCaixaService; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.

## RN-035 — Pedido de privacidade não é execução

**Regra:** Motivo deve ter de 1 a 500 caracteres; solicitação aberta é reutilizada. Registro do pedido não executa anonimização.

**Motivo da regra:** 💡 preservar a coerência do processo de pedido de privacidade não é execução e permitir compreender seu resultado; justificativa original a validar com PO.

**Quando se aplica:** Solicitar/consultar pedido.

**Resultado:** Pedido auditável em solicitado, vinculado ao titular.

**Exceções e limites:** Estados em análise/concluído/recusado existem, mas transições operacionais não estão expostas no fluxo analisado.

**Funcionalidades relacionadas:** FUNC-025. **Requisitos relacionados:** RF-025. Os intervalos representam todos os IDs incluídos.

**Dados envolvidos:** Motivo, estado, titular, datas.

**Origem:** PerfilUsuarioService; SolicitacaoPrivacidade; V65; [matriz de fontes](08-rastreabilidade.md).

**Status da informação:** ✅ Confirmado por leitura estática, limitado pelas exceções indicadas. Benefício/motivo: 💡 inferido.
