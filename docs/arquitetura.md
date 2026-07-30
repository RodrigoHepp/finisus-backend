# Arquitetura

O código usa o pacote-base `com.financeiro`. A referência anterior a `br.com.finisus` não corresponde ao código, ao `groupId` Maven nem à configuração atual e foi descontinuada.

O domínio não depende de Spring. Casos de uso dependem de portas e os adapters web e JPA realizam a integração com HTTP, segurança e persistência. O relógio operacional é exposto por `ObterDataAtualPort` e implementado com `Clock` na infraestrutura.

`TipoInvestimento` permanece um enum fechado (`RENDA_FIXA`, `RENDA_VARIAVEL`, `FUNDO`, `CRIPTO`, `OUTRO`). A opção `OUTRO` é deliberadamente uma categoria residual estável; uma taxonomia configurável exigirá catálogo persistido, migration e migração de dados em uma evolução própria.
