# Arquitetura

O código usa o pacote-base `com.finisus`, coerente com o `groupId` Maven e a configuração atual.

O domínio não depende de Spring. Casos de uso dependem de portas e os adapters web e JPA realizam a integração com HTTP, segurança e persistência. O relógio operacional é exposto por `ObterDataAtualPort` e implementado com `Clock` na infraestrutura.

Na borda de segurança, um JWT validado é convertido em `UsuarioAutenticado`. A anotação web `@UsuarioAtual` fornece somente o identificador ao controller, mantendo JWT e `SecurityContext` fora dos casos de uso.

`TipoInvestimento` permanece um enum fechado (`RENDA_FIXA`, `RENDA_VARIAVEL`, `FUNDO`, `CRIPTO`, `OUTRO`). A opção `OUTRO` é deliberadamente uma categoria residual estável; uma taxonomia configurável exigirá catálogo persistido, migration e migração de dados em uma evolução própria.

`Item` é um catálogo pessoal independente. `TransacaoItem` representa a ocorrência financeira e mantém a referência ao item juntamente com o snapshot de nome, categoria e valor. Assim, alterar ou inativar um item não reescreve lançamentos anteriores. O compartilhamento aponta para a ocorrência (`transacao_item`) quando o rateio for de apenas um item.
## Checklist de separação de responsabilidades

- [x] Controllers, input ports e serviços próprios para Banco, Conta, Categoria, Meio de Pagamento e Transação.
- [x] Consulta de histórico de transação exposta por input port próprio.
- [x] DTO de transação extraído do antigo controller agrupador; recorrências e cartão não dependem mais de DTO de outro controller.
- [x] Operações de fatura movidas para `FaturaController` e `FaturaUseCase`, com as rotas anteriores preservadas.
- [x] Movimento de investimento depende de `RegistrarTransacaoUseCase`, e não do use case financeiro agrupado.
- [x] Operações de movimento movidas para `MovimentoInvestimentoController` e `MovimentoInvestimentoUseCase`, mantendo as rotas existentes.
- [x] Definir e implementar refinanciamento e correção de lançamento de parcela, com finalização persistida do financiamento.
- [x] Separar financiamento e parcela em serviços, portas e adapters de persistência distintos; o financiamento coordena criação, refinanciamento e correção, delegando a manutenção do plano ao caso de uso de parcela.
- [x] Separar cartão de crédito e fatura em serviços e adapters distintos; `FaturaService` consulta o cartão por seu input port e concentra os processos que envolvem as duas entidades.
- [x] Separar investimento e movimento em serviços, portas e adapters de persistência distintos; movimentos validam o investimento por seu caso de uso e registram a transação financeira pelo contrato dedicado.
- [x] Separar configuração, despesa compartilhada e rateio em serviços, portas, adapters e controllers distintos; a criação de despesa coordena o processo multi-entidade.
- [x] Dividir adapters de persistência que ainda tratam mais de uma entidade quando houver responsabilidades independentes. Transação/histórico e recorrência/geração continuam coesos por ciclo de vida; parcela consulta financiamento somente para materializar a associação JPA.
- [ ] Substituir o proxy transacional baseado em nomes de métodos por decorators explícitos.
- [ ] Cobrir os novos boundaries com testes unitários, JPA e REST dedicados.

Os processos que alteram mais de uma entidade permanecem transacionais e explícitos; eles não se tornam CRUD de uma entidade vizinha.
