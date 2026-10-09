# Domínios, glossário e escopo

[Voltar ao portal](00-README.md)

## Domínios identificados

| Domínio | O que representa e finalidade | Relações |
|---|---|---|
| Identidade | Cadastro, acesso, perfil e titularidade dos dados. | Autoriza registros próprios e participações compartilhadas. |
| Organização | Bancos, contas, categorias, meios de pagamento e catálogo de itens. | Fornece referências aos registros financeiros. |
| Caixa | Entradas, saídas, transferências e ajustes registrados. | Explica saldo de contas e liquidação de compromissos. |
| Obrigações | Dívidas com credor e vencimento. | Pagamentos geram transações e reduzem saldo pendente. |
| Recorrências | Expectativas mensais de receita ou despesa. | Geram ocorrências que só movimentam caixa quando realizadas. |
| Crédito | Cartões, faturas e compras parceladas. | Compras compõem competências; liquidação da fatura compõe caixa. |
| Financiamentos | Contratos com principal, juros e cronograma de parcelas. | Pagamentos, amortizações e refinanciamentos afetam dívida e caixa. |
| Investimentos | Aplicações classificadas e movimentos de capital, rendimento ou taxa. | Conta de origem, custódia opcional e posições datadas. |
| Compartilhamento | Responsabilidades de várias pessoas sobre despesas. | Relaciona transações reais, alocações e reembolsos. |
| Importação | Interpretação e revisão humana de documentos financeiros. | Produz ou associa transações, obrigações e lançamentos de fatura. |
| Acompanhamento | Visões de caixa, competência, agenda, patrimônio e projeção. | Consolida os demais domínios com regras próprias. |
| Privacidade | Exportação, pedido de anonimização e desativação do acesso. | Abrange dados próprios e históricos com participantes. |

## Dentro do escopo

✅ Operações do backend descritas em FUNC-001 a FUNC-025, inclusive consultas, histórico, estados, cancelamentos e estornos quando disponibilizados. Contas podem representar dinheiro físico, conta corrente, poupança ou aplicação. Investimentos são classificados em renda fixa, renda variável, fundo, cripto ou outro; essa classificação não implica integração com corretora.

✅ O comportamento inclui registros ainda não commitados no checkout examinado. Não se presume que estejam implantados.

## Fora do escopo da execução atual

- Movimentar dinheiro real em instituições financeiras: o usuário executa a operação externamente e a representa no Finisus.
- Comprar ou vender ativos numa corretora, obter cotação automaticamente ou garantir rendimento.
- Fazer OCR de PDF composto somente por imagem.
- Executar anonimização automaticamente após o pedido.
- Oferecer um livro contábil empresarial, emissão fiscal ou cálculo de tributos: não há fluxo desses processos no backend inventariado.

“Fora do escopo atual” não significa decisão definitiva de nunca oferecer essas capacidades.

## Escopo não confirmado

❓ Público prioritário, suporte formal a grupos familiares, orçamento por teto de categoria, metas, alertas externos, moeda diferente de real, tratamento cambial, acesso de contador, perfis administrativos, recuperação de senha, encerramento mensal e política de retenção. Não transformar a ausência no inventário em promessa de roadmap.

❓ Disponibilidade de telas para todas as operações: o frontend não foi homologado neste trabalho.

## Glossário de negócio

| Termo | Definição |
|---|---|
| Banco global | Instituição disponibilizada para consulta comum; usuários comuns não podem alterá-la. |
| Banco pessoal | Registro de instituição pertencente ao titular. |
| Conta física | Controle de dinheiro físico, sem banco associado. |
| Conta de origem | Conta usada para registrar os movimentos de um investimento. |
| Custódia | Conta de aplicação explicitamente vinculada ao investimento, evitando dupla soma patrimonial. |
| Categoria / subcategoria | Classificação hierárquica para organizar análise, com até cinco níveis. |
| Meio de pagamento | Referência descritiva da forma usada; cadastrá-la não integra um provedor de pagamentos. |
| Item de catálogo | Nome e categoria reutilizáveis; não representa estoque. |
| Item da transação | Detalhamento histórico de parte do valor total; pode ser uma linha livre. |
| Competência | Período usado na análise econômica; no cartão, o mês da fatura. |
| Caixa | Efeito realizado sobre saldo de conta. |
| Compromisso | Valor previsto ou devido, que pode ainda não ter efeito em caixa. |
| Saldo registrado | Saldo conservado na conta após movimentos e ajustes. |
| Saldo calculado | Apuração dos movimentos eficazes e ajustes usada na reconciliação. |
| Saldo livre | Saldo das contas menos compromissos considerados na visão geral; sujeito às limitações de apuração documentadas. |
| Principal | Capital da dívida, separado dos juros e encargos futuros. |
| Amortização | Pagamento extraordinário que reduz principal de financiamento e recalcula parcelas pendentes. |
| Refinanciamento | Encerramento do contrato de origem e, no fluxo completo, criação de contrato sucessor. |
| Cronograma | Sequência de parcelas e sua composição; versões anteriores ficam consultáveis. |
| Crédito da fatura | Valor favorável decorrente do documento ou excesso de pagamento; não equivale a receita bancária. |
| Aporte / resgate | Entrada e saída de capital investido, com efeito oposto na conta de origem. |
| Rendimento realizado | Valor explicitamente informado e recebido na conta de origem. |
| Posição | Valor observado e informado de um investimento numa data. |
| Capital líquido | Aportes menos resgates ativos; não é necessariamente valor de mercado. |
| Patrimônio líquido | Ativos considerados menos dívidas principais consideradas pelo painel. |
| Base compartilhada | Parte do valor que efetivamente entra no cálculo de responsabilidades. |
| Responsabilidade | Quanto cada participante deve assumir de uma despesa. |
| Alocação | Uso de parte de uma transação real para demonstrar pagamento de uma despesa. |
| Saldo compartilhado | Diferença entre pagamento e responsabilidade, ajustada por reembolsos. |
| Opt-in | Manifestação de que o usuário aceita receber compartilhamentos no modelo de rateio. |
| Fotografia histórica | Cópia dos dados válida no momento do fato, preservada após alterações cadastrais. |
| Conciliação de importação | Associação de linha do documento a registro existente, sem nova movimentação. |
| Idempotência | Repetir a mesma solicitação identificada sem produzir novo efeito; existe apenas nos fluxos explicitamente descritos. |
| Cancelamento | Interrupção de um registro conforme regras do módulo; não apaga necessariamente fatos anteriores. |
| Desativação | Bloqueio de uso futuro do cadastro ou acesso, com preservação de histórico. |
| Anonimização solicitada | Pedido registrado; não significa que os identificadores pessoais já foram removidos. |
| API | Meio pelo qual uma interface solicita operações ao backend. Não exige conhecimento técnico para entender as jornadas. |
| PO | Responsável pelas decisões de produto; não é um perfil de acesso identificado no sistema. |
| QA | Responsável pela avaliação de qualidade e cenários verificáveis. |
