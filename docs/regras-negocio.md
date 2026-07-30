# Regras de negócio

- Financiamento exige principal positivo, taxa não negativa e número de parcelas positivo.
- Rateio exige exatamente uma base de cálculo: valor fixo positivo ou percentual entre 0 e 100.
- Parcelas somente ficam atrasadas quando o vencimento é anterior à data operacional recebida.
- E-mails são normalizados com `Locale.ROOT`.
- O estorno recompõe o saldo somente quando o lançamento original o alterou. Gastos de cartão e parcelas programadas não movimentam o saldo no seu registro e, por isso, seu estorno não o modifica.
- Recorrências podem gerar lançamentos somente para a competência operacional atual ou anterior; competências futuras pertencem à previsão de fluxo de caixa.
