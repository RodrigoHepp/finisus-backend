# Regras de negócio

- Financiamento exige principal positivo, taxa não negativa e número de parcelas positivo.
- Rateio exige exatamente uma base de cálculo: valor fixo positivo ou percentual entre 0 e 100.
- Parcelas somente ficam atrasadas quando o vencimento é anterior à data operacional recebida.
- E-mails são normalizados com `Locale.ROOT`.
- O estorno recompõe o saldo somente quando o lançamento original o alterou. Gastos de cartão e parcelas programadas não movimentam o saldo no seu registro e, por isso, seu estorno não o modifica.
- Recorrências podem gerar lançamentos somente para a competência operacional atual ou anterior; competências futuras pertencem à previsão de fluxo de caixa.
- Itens pertencem ao usuário e podem ser inativados, mas itens inativos não são aceitos em novos lançamentos.
- Cada linha de lançamento referencia um item de catálogo e preserva nome, categoria e valor como histórico da ocorrência.
- Uma transação pode ser compartilhada integralmente ou por item, mas os dois modos não podem coexistir na mesma transação; um item de transação só pode ser compartilhado uma vez.
- O alvo `ITEM_TRANSACAO` exige um item da transação; o alvo `TRANSACAO` não pode receber item.
# Regras de negócio

## Financiamento e parcelas

- O refinanciamento encerra o financiamento e preserva todas as parcelas já pagas. Parcelas não pagas a partir da parcela selecionada são excluídas.
- A exclusão por erro de lançamento somente é permitida quando nenhuma parcela foi paga. Após a exclusão, as parcelas restantes são renumeradas, têm vencimentos recalculados a partir da data inicial e recebem novo valor calculado sobre o principal, taxa mensal e quantidade restante.
- Quando existir parcela paga, o sistema bloqueia o recálculo. O usuário deve ajustar os valores das parcelas manualmente.

## Cancelamentos e estornos

- Cancelamentos são lógicos. Uma despesa compartilhada cancelada preserva auditoria e cancela todos os rateios ainda não pagos.
- Uma transação com compartilhamento ativo, integral ou por item, não pode ser corrigida ou estornada. O cancelamento do compartilhamento libera essa operação.
- Faturas podem ser alteradas ou canceladas somente enquanto abertas.
- Um financiamento ativo só pode ser cancelado quando não possui parcela paga.
- O estorno de um movimento de investimento preserva o original, estorna sua transação financeira e cria um movimento compensatório do tipo oposto. Um movimento só pode ser estornado uma vez.
