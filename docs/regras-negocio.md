# Regras de negócio

## Transações, itens e saldo

- O estorno recompõe o saldo apenas quando o lançamento original o alterou. Gastos de cartão e parcelas programadas não movimentam o saldo no registro e, portanto, seu estorno não o modifica.
- Itens pertencem ao usuário e podem ser inativados, mas não podem ser usados em novos lançamentos quando inativos.
- Cada linha de lançamento referencia um item de catálogo e preserva nome, categoria e valor como snapshot histórico.

## Compartilhamento e rateio

- Um rateio exige exatamente uma base de cálculo: valor fixo positivo ou percentual entre 0 e 100.
- Uma transação pode ser compartilhada integralmente ou por item; os dois modos não coexistem na mesma transação.
- Um item de transação só pode ser compartilhado uma vez. O alvo `ITEM_TRANSACAO` exige um item da transação, enquanto `TRANSACAO` não recebe item.
- Uma transação com compartilhamento ativo não pode ser corrigida ou estornada. O cancelamento do compartilhamento libera a operação.
- O cancelamento de uma despesa compartilhada preserva auditoria e cancela os rateios ainda não pagos.

## Financiamentos e parcelas

- Financiamento exige principal positivo, taxa não negativa e número de parcelas positivo.
- Refinanciamento encerra o financiamento e preserva parcelas pagas; parcelas não pagas a partir da selecionada são excluídas.
- A exclusão por erro de lançamento só é permitida sem parcelas pagas. As parcelas restantes são renumeradas e recalculadas a partir da data inicial.
- Quando houver parcela paga, o sistema bloqueia o recálculo e o ajuste deve ser feito manualmente.
- Um financiamento ativo só pode ser cancelado quando não possui parcela paga.

## Faturas, investimentos e recorrências

- Faturas podem ser alteradas ou canceladas apenas enquanto abertas.
- O estorno de movimento de investimento preserva o original, estorna a transação financeira associada e cria um movimento compensatório do tipo oposto. Um movimento só pode ser estornado uma vez.
- Recorrências geram lançamentos somente para a competência atual ou anterior; competências futuras pertencem à previsão de fluxo de caixa.
- Parcelas ficam atrasadas somente quando o vencimento é anterior à data operacional.

## Dados de usuário

E-mails são normalizados com `Locale.ROOT` antes do uso nas regras de negócio.

## Importação financeira por PDF

- A importação exige um banco selecionado e um PDF legível de até 10 MB. O nome do arquivo não define o tipo do documento.
- O lote fica pendente de revisão: nenhuma transação ou gasto de fatura é criado durante a leitura.
- Cada linha preserva o conteúdo extraído. Campos incertos ficam pendentes de confirmação até a revisão do usuário.
- A confirmação exige conta para extratos e cobranças, ou uma fatura existente para gastos de cartão. Linhas removidas na revisão não são importadas.
- A prévia informa transações possivelmente duplicadas quando conta ou fatura, data, valor e descrição coincidem com um lançamento já existente.
