-- Baseline inicial do Finisus.
-- O produto ainda nao possui dados persistentes a preservar; por isso este arquivo
-- declara diretamente o schema de lancamento, sem backfills ou compatibilidade legado.
CREATE TABLE `item`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `nome` VARCHAR(300) NOT NULL,
    `categoria_padrao_id` BIGINT,
    `ativo` BOOLEAN DEFAULT TRUE NOT NULL
);


CREATE INDEX `idx_item_usuario_ativo_nome` ON `item`(`usuario_id`, `ativo`, `nome`);

CREATE TABLE `refresh_token`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `token` VARCHAR(512) NOT NULL,
    `usuario_id` BIGINT NOT NULL,
    `expira_em` TIMESTAMP NOT NULL,
    `invalidado` BOOLEAN DEFAULT FALSE NOT NULL,
    `criado_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


CREATE INDEX `idx_refresh_token_usuario` ON `refresh_token`(`usuario_id`);

CREATE TABLE `banco`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `nome` VARCHAR(150) NOT NULL,
    `codigo` VARCHAR(20) NOT NULL,
    `ativo` BOOLEAN DEFAULT TRUE NOT NULL,
    `usuario_id` BIGINT
);


CREATE TABLE `pagamento_fatura`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `fatura_id` BIGINT NOT NULL,
    `usuario_id` BIGINT NOT NULL,
    `transacao_id` BIGINT NOT NULL,
    `conta_id` BIGINT NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `credito` DECIMAL(19, 2) DEFAULT 0 NOT NULL,
    `data_pagamento` DATE NOT NULL,
    `chave_idempotencia` VARCHAR(100) NOT NULL,
    `hash_requisicao` VARCHAR(64) NOT NULL,
    `estornado_em` TIMESTAMP,
    `version` BIGINT DEFAULT 0 NOT NULL
);


CREATE INDEX `idx_pagamento_fatura_historico` ON `pagamento_fatura`(`fatura_id`, `usuario_id`, `data_pagamento`, `id`);

CREATE TABLE `categoria`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `nome` VARCHAR(100) NOT NULL,
    `categoria_pai_id` BIGINT,
    `ativo` BOOLEAN DEFAULT TRUE NOT NULL
);


CREATE INDEX `idx_categoria_usuario` ON `categoria`(`usuario_id`);

CREATE TABLE `meio_pagamento`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `nome` VARCHAR(100) NOT NULL,
    `ativo` BOOLEAN DEFAULT TRUE NOT NULL
);


CREATE INDEX `idx_meio_pagamento_usuario` ON `meio_pagamento`(`usuario_id`);

CREATE TABLE `ajuste_saldo_conta`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `conta_id` BIGINT NOT NULL,
    `saldo_anterior` DECIMAL(19, 2) NOT NULL,
    `saldo_calculado_anterior` DECIMAL(19, 2) NOT NULL,
    `saldo_informado` DECIMAL(19, 2) NOT NULL,
    `valor_ajuste` DECIMAL(19, 2) NOT NULL,
    `motivo` VARCHAR(500) NOT NULL,
    `data_ajuste` DATE NOT NULL,
    `chave_idempotencia` VARCHAR(100) NOT NULL,
    `hash_requisicao` VARCHAR(64) NOT NULL,
    `criado_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `version` BIGINT DEFAULT 0 NOT NULL
);


CREATE INDEX `idx_ajuste_saldo_conta_data` ON `ajuste_saldo_conta`(`conta_id`, `data_ajuste`);

CREATE TABLE `compra_parcelada`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `descricao` VARCHAR(300) NOT NULL,
    `valor_total` DECIMAL(19, 2) NOT NULL,
    `numero_parcelas` INTEGER NOT NULL,
    `data_compra` DATE NOT NULL,
    `categoria_id` BIGINT,
    `conta_id` BIGINT NOT NULL,
    `cancelada_em` TIMESTAMP,
    `cartao_id` BIGINT
);


CREATE INDEX `idx_compra_parcelada_cartao` ON `compra_parcelada`(`cartao_id`);

CREATE INDEX `idx_compra_parcelada_usuario` ON `compra_parcelada`(`usuario_id`);

CREATE TABLE `lancamento_importado`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `importacao_id` BIGINT NOT NULL,
    `ordem` INTEGER NOT NULL,
    `data` DATE,
    `descricao` VARCHAR(500),
    `conteudo_original` VARCHAR(1000) NOT NULL,
    `data_original` DATE,
    `descricao_original` VARCHAR(500),
    `valor_original` DECIMAL(19, 2),
    `tipo_original` VARCHAR(10),
    `valor` DECIMAL(19, 2),
    `tipo` VARCHAR(10),
    `pendente_confirmacao` BOOLEAN DEFAULT FALSE NOT NULL,
    `motivo_pendencia` VARCHAR(300),
    `estado` VARCHAR(20) NOT NULL,
    `importar` BOOLEAN DEFAULT TRUE NOT NULL,
    `categoria_id` BIGINT,
    `item_id` BIGINT,
    `transacao_id` BIGINT,
    `obrigacao_financeira_id` BIGINT
);


CREATE INDEX `idx_lancamento_importado_transacao` ON `lancamento_importado`(`transacao_id`);

CREATE INDEX `idx_lancamento_importado_obrigacao` ON `lancamento_importado`(`obrigacao_financeira_id`);

CREATE TABLE `importacao_financeira`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `banco_id` BIGINT NOT NULL,
    `nome_arquivo` VARCHAR(255) NOT NULL,
    `hash_arquivo` CHAR(64) NOT NULL,
    `leitor` VARCHAR(100) NOT NULL,
    `tipo_documento` VARCHAR(20) NOT NULL,
    `tipo_documento_pretendido` VARCHAR(20) NOT NULL,
    `identificador_origem` VARCHAR(120),
    `periodo_inicio` DATE,
    `periodo_fim` DATE,
    `data_vencimento` DATE,
    `saldo_inicial` DECIMAL(19, 2),
    `saldo_final` DECIMAL(19, 2),
    `valor_total` DECIMAL(19, 2),
    `status` VARCHAR(20) DEFAULT 'PENDENTE_REVISAO' NOT NULL,
    `conta_id` BIGINT,
    `fatura_id` BIGINT,
    `version` BIGINT DEFAULT 0 NOT NULL,
    `criado_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


CREATE INDEX `idx_importacao_financeira_usuario_status` ON `importacao_financeira`(`usuario_id`, `status`, `id`);

CREATE TABLE `transacao_historico`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `transacao_id` BIGINT NOT NULL,
    `campo_alterado` VARCHAR(100) NOT NULL,
    `valor_anterior` VARCHAR(500),
    `valor_novo` VARCHAR(500),
    `alterado_por` BIGINT NOT NULL,
    `alterado_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `motivo` VARCHAR(500),
    `correlacao_id` VARCHAR(36),
    `snapshot_anterior` TEXT,
    `snapshot_novo` TEXT
);


CREATE INDEX `idx_transacao_historico_correlacao` ON `transacao_historico`(`correlacao_id`);

CREATE TABLE `recorrencia`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `nome` VARCHAR(150) NOT NULL,
    `tipo` VARCHAR(10) NOT NULL,
    `valor_esperado` DECIMAL(19, 2) NOT NULL,
    `dia_do_mes` INTEGER NOT NULL,
    `categoria_id` BIGINT,
    `conta_id` BIGINT NOT NULL,
    `meio_pagamento_id` BIGINT,
    `ativo` BOOLEAN DEFAULT TRUE NOT NULL
);


CREATE INDEX `idx_recorrencia_usuario` ON `recorrencia`(`usuario_id`);

CREATE TABLE `recorrencia_geracao`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `recorrencia_id` BIGINT NOT NULL,
    `usuario_id` BIGINT NOT NULL,
    `ano_mes` VARCHAR(7) NOT NULL,
    `vencimento` DATE NOT NULL,
    `tipo` VARCHAR(10) NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `descricao` VARCHAR(255) NOT NULL,
    `conta_id` BIGINT NOT NULL,
    `categoria_id` BIGINT,
    `meio_pagamento_id` BIGINT,
    `status` VARCHAR(12) NOT NULL,
    `transacao_id` BIGINT
);


CREATE INDEX `idx_recorrencia_geracao_usuario_periodo` ON `recorrencia_geracao`(`usuario_id`, `ano_mes`, `vencimento`);

CREATE TABLE `cartao_credito`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `nome` VARCHAR(100) NOT NULL,
    `limite` DECIMAL(19, 2) NOT NULL,
    `dia_fechamento` INTEGER NOT NULL,
    `dia_vencimento` INTEGER NOT NULL,
    `ativo` BOOLEAN DEFAULT TRUE NOT NULL
);


CREATE INDEX `idx_cartao_usuario` ON `cartao_credito`(`usuario_id`);

CREATE TABLE `transferencia_conta`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `conta_origem_id` BIGINT NOT NULL,
    `conta_destino_id` BIGINT NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `data_transferencia` DATE NOT NULL,
    `descricao` VARCHAR(500) NOT NULL,
    `chave_idempotencia` VARCHAR(100) NOT NULL,
    `hash_requisicao` VARCHAR(64) NOT NULL,
    `status` VARCHAR(20) NOT NULL,
    `estornada_em` TIMESTAMP,
    `criado_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `version` BIGINT DEFAULT 0 NOT NULL
);


CREATE INDEX `idx_transferencia_usuario_data` ON `transferencia_conta`(`usuario_id`, `data_transferencia`);

CREATE TABLE `movimento_investimento`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `investimento_id` BIGINT NOT NULL,
    `tipo` VARCHAR(10) NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `data` DATE NOT NULL,
    `transacao_id` BIGINT,
    `estornado_em` TIMESTAMP,
    `movimento_origem_id` BIGINT
);


CREATE TABLE `usuario`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `nome` VARCHAR(150) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `senha_hash` VARCHAR(255) NOT NULL,
    `ativo` BOOLEAN DEFAULT TRUE NOT NULL,
    `criado_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `sessao_versao` BIGINT DEFAULT 0 NOT NULL
);


CREATE TABLE `divisao_compartilhada`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `criador_id` BIGINT NOT NULL,
    `nome` VARCHAR(120) NOT NULL,
    `status` VARCHAR(10) DEFAULT 'ATIVA' NOT NULL,
    `version` BIGINT DEFAULT 0 NOT NULL
);


CREATE TABLE `configuracao_compartilhamento`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `aceita_compartilhamento` BOOLEAN DEFAULT FALSE NOT NULL
);


CREATE TABLE `parcela_financiamento_historico`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `financiamento_id` BIGINT NOT NULL,
    `cronograma_versao` INTEGER NOT NULL,
    `parcela_id_origem` BIGINT NOT NULL,
    `numero` INTEGER NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `valor_principal` DECIMAL(19, 2),
    `juros` DECIMAL(19, 2),
    `encargos` DECIMAL(19, 2),
    `saldo_devedor_inicial` DECIMAL(19, 2),
    `saldo_devedor_final` DECIMAL(19, 2),
    `data_vencimento` DATE NOT NULL,
    `status` VARCHAR(10) NOT NULL,
    `transacao_id` BIGINT,
    `registrado_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


CREATE INDEX `idx_parcela_historico_financiamento_versao` ON `parcela_financiamento_historico`(`financiamento_id`, `cronograma_versao`);

CREATE TABLE `pagamento_obrigacao`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `obrigacao_id` BIGINT NOT NULL,
    `usuario_id` BIGINT NOT NULL,
    `transacao_id` BIGINT NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `data_pagamento` DATE NOT NULL,
    `estornado_em` TIMESTAMP,
    `version` BIGINT DEFAULT 0 NOT NULL,
    `juros` DECIMAL(19, 2) DEFAULT 0 NOT NULL,
    `encargos` DECIMAL(19, 2) DEFAULT 0 NOT NULL,
    `desconto` DECIMAL(19, 2) DEFAULT 0 NOT NULL
);


CREATE INDEX `idx_pagamento_obrigacao_historico` ON `pagamento_obrigacao`(`obrigacao_id`, `usuario_id`, `data_pagamento`, `id`);

CREATE TABLE `posicao_investimento`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `investimento_id` BIGINT NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `data_referencia` DATE NOT NULL,
    `version` BIGINT DEFAULT 0 NOT NULL
);


CREATE INDEX `idx_posicao_investimento_referencia` ON `posicao_investimento`(`investimento_id`, `data_referencia` DESC, `id` DESC);

CREATE TABLE `investimento`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `nome` VARCHAR(150) NOT NULL,
    `tipo` VARCHAR(20) NOT NULL,
    `conta_origem_id` BIGINT NOT NULL,
    `ativo` BOOLEAN DEFAULT TRUE NOT NULL,
    `conta_custodia_id` BIGINT
);


CREATE INDEX `idx_investimento_conta_custodia` ON `investimento`(`conta_custodia_id`);

CREATE INDEX `idx_investimento_usuario` ON `investimento`(`usuario_id`);

CREATE TABLE `revisao_lancamento_importado`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `lancamento_importado_id` BIGINT NOT NULL,
    `revisado_por` BIGINT NOT NULL,
    `decisao` VARCHAR(30) NOT NULL,
    `motivo_incerteza` VARCHAR(300),
    `justificativa` VARCHAR(500) NOT NULL,
    `data_anterior` DATE,
    `descricao_anterior` VARCHAR(500),
    `valor_anterior` DECIMAL(19, 2),
    `tipo_anterior` VARCHAR(10),
    `importar_anterior` BOOLEAN NOT NULL,
    `categoria_id_anterior` BIGINT,
    `item_id_anterior` BIGINT,
    `transacao_id_anterior` BIGINT,
    `obrigacao_id_anterior` BIGINT,
    `data_nova` DATE,
    `descricao_nova` VARCHAR(500),
    `valor_novo` DECIMAL(19, 2),
    `tipo_novo` VARCHAR(10),
    `importar_novo` BOOLEAN NOT NULL,
    `categoria_id_nova` BIGINT,
    `item_id_novo` BIGINT,
    `transacao_id_nova` BIGINT,
    `obrigacao_id_nova` BIGINT,
    `revisado_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


CREATE INDEX `idx_revisao_lancamento_importado_lancamento` ON `revisao_lancamento_importado`(`lancamento_importado_id`, `id`);

CREATE TABLE `previsao_mensal`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `ano_mes` VARCHAR(7) NOT NULL,
    `categoria_id` BIGINT,
    `valor_projetado_entrada` DECIMAL(19, 2) DEFAULT 0.00 NOT NULL,
    `valor_projetado_saida` DECIMAL(19, 2) DEFAULT 0.00 NOT NULL,
    `calculado_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


CREATE UNIQUE INDEX `uk_previsao_usuario_mes_categoria` ON `previsao_mensal`(`usuario_id`, `ano_mes`, `categoria_id`);

CREATE INDEX `idx_previsao_usuario_mes` ON `previsao_mensal`(`usuario_id`, `ano_mes`);

CREATE TABLE `conta`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `nome` VARCHAR(150) NOT NULL,
    `tipo` VARCHAR(20) NOT NULL,
    `banco_id` BIGINT,
    `saldo` DECIMAL(19, 2) DEFAULT 0.00 NOT NULL,
    `version` BIGINT DEFAULT 0 NOT NULL,
    `criado_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `ativo` BOOLEAN DEFAULT TRUE NOT NULL
);


CREATE INDEX `idx_conta_usuario` ON `conta`(`usuario_id`);

CREATE TABLE `fatura`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `cartao_id` BIGINT NOT NULL,
    `ano_mes` VARCHAR(7) NOT NULL,
    `data_fechamento` DATE NOT NULL,
    `data_vencimento` DATE NOT NULL,
    `status` VARCHAR(10) DEFAULT 'ABERTA' NOT NULL,
    `conta_pagamento_id` BIGINT,
    `version` BIGINT DEFAULT 0 NOT NULL,
    `cancelada_em` DATE
);


CREATE TABLE `participante_divisao_compartilhada`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `divisao_compartilhada_id` BIGINT NOT NULL,
    `usuario_id` BIGINT NOT NULL,
    `percentual` DECIMAL(5, 2)
);


CREATE INDEX `idx_participante_divisao_usuario` ON `participante_divisao_compartilhada`(`usuario_id`, `divisao_compartilhada_id`);

CREATE TABLE `responsabilidade_transacao_divisao`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `vinculo_id` BIGINT NOT NULL,
    `usuario_id` BIGINT NOT NULL,
    `percentual` DECIMAL(5, 2) NOT NULL,
    `valor_devido` DECIMAL(19, 2) NOT NULL
);


CREATE INDEX `idx_responsabilidade_divisao_usuario` ON `responsabilidade_transacao_divisao`(`usuario_id`, `vinculo_id`);

CREATE TABLE `parcela_financiamento`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `financiamento_id` BIGINT NOT NULL,
    `numero` INTEGER NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `data_vencimento` DATE NOT NULL,
    `status` VARCHAR(10) DEFAULT 'PENDENTE' NOT NULL,
    `version` BIGINT DEFAULT 0 NOT NULL,
    `transacao_id` BIGINT,
    `valor_principal` DECIMAL(19, 2),
    `juros` DECIMAL(19, 2),
    `encargos` DECIMAL(19, 2),
    `saldo_devedor_inicial` DECIMAL(19, 2),
    `saldo_devedor_final` DECIMAL(19, 2)
);


CREATE INDEX `idx_parcela_financiamento_vencimento_status` ON `parcela_financiamento`(`data_vencimento`, `status`);

CREATE INDEX `idx_parcela_financiamento_status_vencimento` ON `parcela_financiamento`(`financiamento_id`, `status`, `data_vencimento`);

CREATE TABLE `financiamento`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `descricao` VARCHAR(300) NOT NULL,
    `principal` DECIMAL(19, 2) NOT NULL,
    `taxa_juros_mensal` DECIMAL(8, 6) NOT NULL,
    `numero_parcelas` INTEGER NOT NULL,
    `data_inicio` DATE NOT NULL,
    `conta_id` BIGINT NOT NULL,
    `finalizado_em` TIMESTAMP,
    `status` VARCHAR(12) DEFAULT 'ATIVO' NOT NULL,
    `cancelada_em` TIMESTAMP,
    `cronograma_versao` INTEGER DEFAULT 1 NOT NULL,
    `financiamento_origem_id` BIGINT
);


CREATE INDEX `idx_financiamento_usuario` ON `financiamento`(`usuario_id`);

CREATE INDEX `idx_financiamento_usuario_data_inicio` ON `financiamento`(`usuario_id`, `data_inicio`, `id`);

CREATE TABLE `obrigacao_financeira`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `descricao` VARCHAR(300) NOT NULL,
    `credor` VARCHAR(150) NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `data_vencimento` DATE NOT NULL,
    `conta_pagamento_id` BIGINT NOT NULL,
    `categoria_id` BIGINT,
    `status` VARCHAR(12) DEFAULT 'EM_ABERTO' NOT NULL,
    `data_liquidacao` DATE,
    `transacao_id` BIGINT,
    `cancelada_em` TIMESTAMP,
    `version` BIGINT DEFAULT 0 NOT NULL,
    `valor_pago` DECIMAL(19, 2) DEFAULT 0 NOT NULL
);


CREATE INDEX `idx_obrigacao_financeira_usuario_status_vencimento` ON `obrigacao_financeira`(`usuario_id`, `status`, `data_vencimento`, `id`);

CREATE INDEX `idx_obrigacao_importacao_candidatos` ON `obrigacao_financeira`(`usuario_id`, `conta_pagamento_id`, `valor`, `data_vencimento`, `status`);

CREATE TABLE `historico_participante_divisao`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `divisao_compartilhada_id` BIGINT NOT NULL,
    `usuario_id` BIGINT NOT NULL,
    `percentual` DECIMAL(5, 2),
    `vigente_desde` DATETIME NOT NULL,
    `vigente_ate` DATETIME
);


CREATE INDEX `idx_historico_participante_divisao_vigencia` ON `historico_participante_divisao`(`divisao_compartilhada_id`, `vigente_desde`, `vigente_ate`);

CREATE INDEX `idx_historico_participante_usuario` ON `historico_participante_divisao`(`usuario_id`, `vigente_desde`, `vigente_ate`);

CREATE TABLE `transacao_item`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `transacao_id` BIGINT NOT NULL,
    `descricao` VARCHAR(300) NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `categoria_id` BIGINT,
    `item_id` BIGINT,
    `quantidade` DECIMAL(19, 6)
);


CREATE INDEX `idx_transacao_item_item` ON `transacao_item`(`item_id`);

CREATE TABLE `transacao`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `tipo` VARCHAR(10) NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `data` DATE NOT NULL,
    `descricao` VARCHAR(500) NOT NULL,
    `conta_id` BIGINT NOT NULL,
    `categoria_id` BIGINT,
    `meio_pagamento_id` BIGINT,
    `fatura_id` BIGINT,
    `compra_parcelada_id` BIGINT,
    `recorrencia_id` BIGINT,
    `estornado_em` TIMESTAMP,
    `version` BIGINT DEFAULT 0 NOT NULL,
    `criado_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `fatura_pagamento_id` BIGINT,
    `transferencia_id` BIGINT
);


CREATE UNIQUE INDEX `uk_transacao_transferencia_tipo` ON `transacao`(`transferencia_id`, `tipo`);

CREATE INDEX `idx_transacao_usuario` ON `transacao`(`usuario_id`);

CREATE INDEX `idx_transacao_conta` ON `transacao`(`conta_id`);

CREATE INDEX `idx_transacao_data` ON `transacao`(`data`);

CREATE INDEX `idx_transacao_fatura_data_id` ON `transacao`(`fatura_id`, `data`, `id`);

CREATE INDEX `idx_transacao_fatura_pagamento` ON `transacao`(`fatura_pagamento_id`);

CREATE INDEX `idx_transacao_dashboard_usuario_data_estorno` ON `transacao`(`usuario_id`, `data`, `estornado_em`);

CREATE INDEX `idx_transacao_importacao_conta` ON `transacao`(`usuario_id`, `conta_id`, `tipo`, `valor`, `data`);

CREATE INDEX `idx_transacao_importacao_fatura` ON `transacao`(`usuario_id`, `fatura_id`, `tipo`, `valor`, `data`);

CREATE TABLE `aplicacao_credito_fatura`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `pagamento_origem_id` BIGINT NOT NULL,
    `fatura_destino_id` BIGINT NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `criada_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


CREATE INDEX `idx_aplicacao_credito_fatura_destino` ON `aplicacao_credito_fatura`(`fatura_destino_id`);

CREATE TABLE `transacao_divisao_compartilhada`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `divisao_compartilhada_id` BIGINT NOT NULL,
    `transacao_id` BIGINT NOT NULL,
    `status_snapshot` VARCHAR(20) DEFAULT 'PENDENTE_REVISAO' NOT NULL,
    `base_compartilhada` DECIMAL(19, 2) NOT NULL,
    `cancelado_em` DATETIME,
    `cancelado_por` BIGINT,
    `transacao_ativa_id` BIGINT
);


CREATE INDEX `idx_transacao_divisao_transacao` ON `transacao_divisao_compartilhada`(`transacao_id`);

CREATE UNIQUE INDEX `uk_transacao_divisao_transacao_ativa` ON `transacao_divisao_compartilhada`(`transacao_ativa_id`);

CREATE INDEX `idx_transacao_divisao_cancelamento` ON `transacao_divisao_compartilhada`(`divisao_compartilhada_id`, `cancelado_em`);

CREATE INDEX `idx_transacao_divisao_divisao` ON `transacao_divisao_compartilhada`(`divisao_compartilhada_id`, `transacao_id`);

CREATE TABLE `reembolso_divisao`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `divisao_id` BIGINT NOT NULL,
    `transacao_id` BIGINT NOT NULL,
    `pagador_id` BIGINT NOT NULL,
    `recebedor_id` BIGINT NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `criado_em` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `cancelado_em` DATETIME,
    `cancelado_por` BIGINT,
    `transacao_ativa_id` BIGINT
);


CREATE INDEX `idx_reembolso_divisao_periodo` ON `reembolso_divisao`(`divisao_id`, `cancelado_em`);

CREATE TABLE `alocacao_pagamento_divisao`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `vinculo_id` BIGINT NOT NULL,
    `transacao_id` BIGINT NOT NULL,
    `pagador_id` BIGINT NOT NULL,
    `valor` DECIMAL(19, 2) NOT NULL,
    `criada_em` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `cancelada_em` DATETIME,
    `cancelada_por` BIGINT
);


CREATE INDEX `idx_alocacao_divisao_cancelada_por` ON `alocacao_pagamento_divisao`(`cancelada_por`);

CREATE INDEX `idx_alocacao_divisao_vinculo` ON `alocacao_pagamento_divisao`(`vinculo_id`, `cancelada_em`);

CREATE INDEX `idx_alocacao_divisao_transacao` ON `alocacao_pagamento_divisao`(`transacao_id`, `cancelada_em`);

CREATE TABLE `solicitacao_privacidade`(
    `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `usuario_id` BIGINT NOT NULL,
    `tipo` VARCHAR(20) NOT NULL,
    `status` VARCHAR(20) DEFAULT 'SOLICITADA' NOT NULL,
    `motivo` VARCHAR(500) NOT NULL,
    `solicitada_em` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `concluida_em` TIMESTAMP,
    `observacao` VARCHAR(500),
    `version` BIGINT DEFAULT 0 NOT NULL
);


CREATE INDEX `idx_solicitacao_privacidade_usuario_data` ON `solicitacao_privacidade`(`usuario_id`, `solicitada_em`, `id`);

CREATE INDEX `idx_solicitacao_privacidade_status_data` ON `solicitacao_privacidade`(`status`, `solicitada_em`, `id`);

ALTER TABLE `conta` ADD CONSTRAINT `chk_conta_tipo_banco` CHECK(((`tipo` IN('CORRENTE', 'POUPANCA', 'APLICACAO'))
    AND (`banco_id` IS NOT NULL))
    OR ((`tipo` = 'FISICO')
    AND (`banco_id` IS NULL)));

ALTER TABLE `cartao_credito` ADD CONSTRAINT `chk_cartao_dia_vencimento` CHECK(`dia_vencimento` BETWEEN 1 AND 31);

ALTER TABLE `transferencia_conta` ADD CONSTRAINT `chk_transferencia_status` CHECK(`status` IN('ATIVA', 'ESTORNADA'));

ALTER TABLE `parcela_financiamento` ADD CONSTRAINT `chk_parcela_status` CHECK(`status` IN('PENDENTE', 'PAGA', 'ATRASADA'));

ALTER TABLE `recorrencia` ADD CONSTRAINT `chk_recorrencia_tipo` CHECK(`tipo` IN('ENTRADA', 'SAIDA'));

ALTER TABLE `parcela_financiamento_historico` ADD CONSTRAINT `chk_parcela_historico_versao` CHECK(`cronograma_versao` >= 1);

ALTER TABLE `recorrencia_geracao` ADD CONSTRAINT `chk_recorrencia_geracao_tipo` CHECK(`tipo` IN('ENTRADA', 'SAIDA'));

ALTER TABLE `pagamento_obrigacao` ADD CONSTRAINT `chk_pagamento_obrigacao_valor` CHECK(`valor` > 0);

ALTER TABLE `transferencia_conta` ADD CONSTRAINT `chk_transferencia_contas_distintas` CHECK(`conta_origem_id` <> `conta_destino_id`);

ALTER TABLE `parcela_financiamento` ADD CONSTRAINT `chk_parcela_composicao_completa` CHECK(((`valor_principal` IS NULL)
    AND (`juros` IS NULL)
    AND (`encargos` IS NULL)
    AND (`saldo_devedor_inicial` IS NULL)
    AND (`saldo_devedor_final` IS NULL))
    OR ((`valor_principal` IS NOT NULL)
    AND (`juros` IS NOT NULL)
    AND (`encargos` IS NOT NULL)
    AND (`saldo_devedor_inicial` IS NOT NULL)
    AND (`saldo_devedor_final` IS NOT NULL)
    AND (`valor_principal` >= 0)
    AND (`juros` >= 0)
    AND (`encargos` >= 0)
    AND (`saldo_devedor_inicial` >= 0)
    AND (`saldo_devedor_final` >= 0)
    AND (`saldo_devedor_inicial` = (`valor_principal` + `saldo_devedor_final`))
    AND (`valor` = ((`valor_principal` + `juros`) + `encargos`))));

ALTER TABLE `posicao_investimento` ADD CONSTRAINT `chk_posicao_investimento_valor` CHECK(`valor` >= 0);

ALTER TABLE `revisao_lancamento_importado` ADD CONSTRAINT `chk_revisao_lancamento_importado_decisao` CHECK(`decisao` IN('CRIAR', 'IGNORAR', 'ASSOCIAR_TRANSACAO', 'ASSOCIAR_OBRIGACAO'));

ALTER TABLE `financiamento` ADD CONSTRAINT `chk_financiamento_status` CHECK(`status` IN('ATIVO', 'FINALIZADO', 'CANCELADO'));

ALTER TABLE `cartao_credito` ADD CONSTRAINT `chk_cartao_dia_fechamento` CHECK(`dia_fechamento` BETWEEN 1 AND 31);

ALTER TABLE `solicitacao_privacidade` ADD CONSTRAINT `chk_solicitacao_privacidade_tipo` CHECK(`tipo` = 'ANONIMIZACAO');

ALTER TABLE `fatura` ADD CONSTRAINT `chk_fatura_status` CHECK(`status` IN('ABERTA', 'FECHADA', 'PAGA', 'CANCELADA'));

ALTER TABLE `movimento_investimento` ADD CONSTRAINT `chk_movimento_tipo` CHECK(`tipo` IN('APORTE', 'RESGATE', 'RENDIMENTO_REALIZADO', 'TAXA'));

ALTER TABLE `transacao_divisao_compartilhada` ADD CONSTRAINT `chk_transacao_divisao_base_compartilhada` CHECK(`base_compartilhada` > 0);

ALTER TABLE `transacao_item` ADD CONSTRAINT `ck_transacao_item_quantidade_positiva` CHECK((`quantidade` IS NULL)
    OR (`quantidade` > 0));

ALTER TABLE `historico_participante_divisao` ADD CONSTRAINT `chk_historico_participante_vigencia` CHECK((`vigente_ate` IS NULL)
    OR (`vigente_ate` >= `vigente_desde`));

ALTER TABLE `reembolso_divisao` ADD CONSTRAINT `chk_reembolso_valor` CHECK(`valor` > 0);

ALTER TABLE `investimento` ADD CONSTRAINT `chk_investimento_tipo` CHECK(`tipo` IN('RENDA_FIXA', 'RENDA_VARIAVEL', 'FUNDO', 'CRIPTO', 'OUTRO'));

ALTER TABLE `alocacao_pagamento_divisao` ADD CONSTRAINT `chk_alocacao_divisao_valor` CHECK(`valor` > 0);

ALTER TABLE `solicitacao_privacidade` ADD CONSTRAINT `chk_solicitacao_privacidade_status` CHECK(`status` IN('SOLICITADA', 'EM_ANALISE', 'CONCLUIDA', 'RECUSADA'));

ALTER TABLE `obrigacao_financeira` ADD CONSTRAINT `chk_obrigacao_financeira_status` CHECK(`status` IN('EM_ABERTO', 'PAGA', 'VENCIDA', 'CANCELADA'));

ALTER TABLE `obrigacao_financeira` ADD CONSTRAINT `chk_obrigacao_financeira_valor` CHECK(`valor` > 0);

ALTER TABLE `pagamento_obrigacao` ADD CONSTRAINT `chk_pagamento_obrigacao_juros` CHECK(`juros` >= 0);

ALTER TABLE `responsabilidade_transacao_divisao` ADD CONSTRAINT `chk_responsabilidade_divisao_valor` CHECK(`valor_devido` >= 0);

ALTER TABLE `historico_participante_divisao` ADD CONSTRAINT `chk_historico_participante_percentual` CHECK((`percentual` IS NULL)
    OR ((`percentual` > 0)
    AND (`percentual` <= 100)));

ALTER TABLE `reembolso_divisao` ADD CONSTRAINT `chk_reembolso_participantes` CHECK(`pagador_id` <> `recebedor_id`);

ALTER TABLE `aplicacao_credito_fatura` ADD CONSTRAINT `chk_aplicacao_credito_valor` CHECK(`valor` > 0);

ALTER TABLE `revisao_lancamento_importado` ADD CONSTRAINT `chk_revisao_lancamento_importado_tipo_novo` CHECK((`tipo_novo` IN('ENTRADA', 'SAIDA'))
    OR (`tipo_novo` IS NULL));

ALTER TABLE `transacao_divisao_compartilhada` ADD CONSTRAINT `chk_transacao_divisao_status_snapshot` CHECK(`status_snapshot` IN('CONFIRMADO', 'PENDENTE_REVISAO'));

ALTER TABLE `recorrencia_geracao` ADD CONSTRAINT `chk_recorrencia_geracao_status` CHECK(`status` IN('PENDENTE', 'REALIZADA'));

ALTER TABLE `financiamento` ADD CONSTRAINT `chk_financiamento_cronograma_versao` CHECK(`cronograma_versao` >= 1);

ALTER TABLE `lancamento_importado` ADD CONSTRAINT `chk_lancamento_importado_tipo_original` CHECK((`tipo_original` IN('ENTRADA', 'SAIDA'))
    OR (`tipo_original` IS NULL));

ALTER TABLE `responsabilidade_transacao_divisao` ADD CONSTRAINT `chk_responsabilidade_divisao_percentual` CHECK((`percentual` > 0)
    AND (`percentual` <= 100));

ALTER TABLE `lancamento_importado` ADD CONSTRAINT `chk_lancamento_importado_estado` CHECK(`estado` IN('PENDENTE', 'IGNORADA', 'ASSOCIADA', 'CRIADA'));

ALTER TABLE `revisao_lancamento_importado` ADD CONSTRAINT `chk_revisao_lancamento_importado_tipo_anterior` CHECK((`tipo_anterior` IN('ENTRADA', 'SAIDA'))
    OR (`tipo_anterior` IS NULL));

ALTER TABLE `pagamento_obrigacao` ADD CONSTRAINT `chk_pagamento_obrigacao_desconto` CHECK(`desconto` >= 0);

ALTER TABLE `participante_divisao_compartilhada` ADD CONSTRAINT `chk_participante_divisao_percentual` CHECK((`percentual` > 0)
    AND (`percentual` <= 100));

ALTER TABLE `divisao_compartilhada` ADD CONSTRAINT `chk_divisao_compartilhada_status` CHECK(`status` IN('ATIVA', 'INATIVA'));

ALTER TABLE `pagamento_obrigacao` ADD CONSTRAINT `chk_pagamento_obrigacao_encargos` CHECK(`encargos` >= 0);

ALTER TABLE `lancamento_importado` ADD CONSTRAINT `chk_lancamento_importado_tipo` CHECK(`tipo` IN('ENTRADA', 'SAIDA'));

ALTER TABLE `recorrencia` ADD CONSTRAINT `chk_recorrencia_dia` CHECK(`dia_do_mes` BETWEEN 1 AND 31);

ALTER TABLE `importacao_financeira` ADD CONSTRAINT `chk_importacao_financeira_status` CHECK(`status` IN('PENDENTE_REVISAO', 'CONFIRMADA'));

ALTER TABLE `pagamento_fatura` ADD CONSTRAINT `chk_pagamento_fatura_valor` CHECK(`valor` > 0);

ALTER TABLE `compra_parcelada` ADD CONSTRAINT `chk_compra_parcelas` CHECK(`numero_parcelas` > 0);

ALTER TABLE `importacao_financeira` ADD CONSTRAINT `chk_importacao_tipo_pretendido` CHECK(`tipo_documento_pretendido` IN('EXTRATO_CONTA', 'FATURA_CARTAO', 'COBRANCA'));

ALTER TABLE `importacao_financeira` ADD CONSTRAINT `chk_importacao_financeira_tipo` CHECK(`tipo_documento` IN('EXTRATO_CONTA', 'FATURA_CARTAO', 'COBRANCA'));

ALTER TABLE `pagamento_fatura` ADD CONSTRAINT `chk_pagamento_fatura_credito` CHECK((`credito` >= 0)
    AND (`credito` <= `valor`));

ALTER TABLE `conta` ADD CONSTRAINT `chk_conta_tipo` CHECK(`tipo` IN('FISICO', 'CORRENTE', 'POUPANCA', 'APLICACAO'));

ALTER TABLE `transferencia_conta` ADD CONSTRAINT `chk_transferencia_valor_positivo` CHECK(`valor` > 0);

ALTER TABLE `transacao` ADD CONSTRAINT `chk_transacao_tipo` CHECK(`tipo` IN('ENTRADA', 'SAIDA'));

ALTER TABLE `lancamento_importado` ADD CONSTRAINT `uk_lancamento_importado_ordem` UNIQUE (`importacao_id`, `ordem`);

ALTER TABLE `movimento_investimento` ADD CONSTRAINT `uk_movimento_investimento_origem` UNIQUE (`movimento_origem_id`);

ALTER TABLE `reembolso_divisao` ADD CONSTRAINT `uk_reembolso_transacao_ativa` UNIQUE (`transacao_ativa_id`);

ALTER TABLE `ajuste_saldo_conta` ADD CONSTRAINT `uk_ajuste_saldo_usuario_idempotencia` UNIQUE (`usuario_id`, `chave_idempotencia`);

ALTER TABLE `parcela_financiamento` ADD CONSTRAINT `uk_parcela_financiamento_transacao` UNIQUE (`transacao_id`);

ALTER TABLE `configuracao_compartilhamento` ADD CONSTRAINT `uk_config_compartilhamento_usuario` UNIQUE (`usuario_id`);

ALTER TABLE `refresh_token` ADD CONSTRAINT `uk_refresh_token` UNIQUE (`token`);

ALTER TABLE `parcela_financiamento_historico` ADD CONSTRAINT `uk_parcela_historico_versao_numero` UNIQUE (`financiamento_id`, `cronograma_versao`, `numero`);

ALTER TABLE `pagamento_fatura` ADD CONSTRAINT `uk_pagamento_fatura_idempotencia` UNIQUE (`usuario_id`, `chave_idempotencia`);

ALTER TABLE `investimento` ADD CONSTRAINT `uk_investimento_conta_custodia` UNIQUE (`conta_custodia_id`);

ALTER TABLE `pagamento_obrigacao` ADD CONSTRAINT `uk_pagamento_obrigacao_transacao` UNIQUE (`transacao_id`);

ALTER TABLE `banco` ADD CONSTRAINT `uk_banco_codigo_usuario` UNIQUE (`codigo`, `usuario_id`);

ALTER TABLE `parcela_financiamento` ADD CONSTRAINT `uk_parcela_financiamento` UNIQUE (`financiamento_id`, `numero`);

ALTER TABLE `responsabilidade_transacao_divisao` ADD CONSTRAINT `uk_responsabilidade_divisao_usuario` UNIQUE (`vinculo_id`, `usuario_id`);

ALTER TABLE `participante_divisao_compartilhada` ADD CONSTRAINT `uk_participante_divisao_usuario` UNIQUE (`divisao_compartilhada_id`, `usuario_id`);

ALTER TABLE `transferencia_conta` ADD CONSTRAINT `uk_transferencia_usuario_idempotencia` UNIQUE (`usuario_id`, `chave_idempotencia`);

ALTER TABLE `fatura` ADD CONSTRAINT `uk_fatura_cartao_mes` UNIQUE (`cartao_id`, `ano_mes`);

ALTER TABLE `pagamento_fatura` ADD CONSTRAINT `uk_pagamento_fatura_transacao` UNIQUE (`transacao_id`);

ALTER TABLE `recorrencia_geracao` ADD CONSTRAINT `uk_recorrencia_geracao` UNIQUE (`recorrencia_id`, `ano_mes`);

ALTER TABLE `aplicacao_credito_fatura` ADD CONSTRAINT `uk_aplicacao_credito_origem_destino` UNIQUE (`pagamento_origem_id`, `fatura_destino_id`);

ALTER TABLE `financiamento` ADD CONSTRAINT `uk_financiamento_origem` UNIQUE (`financiamento_origem_id`);

ALTER TABLE `usuario` ADD CONSTRAINT `uk_usuario_email` UNIQUE (`email`);

ALTER TABLE `obrigacao_financeira` ADD CONSTRAINT `uk_obrigacao_financeira_transacao` UNIQUE (`transacao_id`);

ALTER TABLE `item` ADD CONSTRAINT `fk_item_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `pagamento_obrigacao` ADD CONSTRAINT `fk_pagamento_obrigacao_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `pagamento_fatura` ADD CONSTRAINT `fk_pagamento_fatura_conta` FOREIGN KEY(`conta_id`) REFERENCES `conta`(`id`);

ALTER TABLE `importacao_financeira` ADD CONSTRAINT `fk_importacao_financeira_fatura` FOREIGN KEY(`fatura_id`) REFERENCES `fatura`(`id`);

ALTER TABLE `compra_parcelada` ADD CONSTRAINT `fk_compra_parcelada_categoria` FOREIGN KEY(`categoria_id`) REFERENCES `categoria`(`id`);

ALTER TABLE `transacao` ADD CONSTRAINT `fk_transacao_transferencia` FOREIGN KEY(`transferencia_id`) REFERENCES `transferencia_conta`(`id`);

ALTER TABLE `solicitacao_privacidade` ADD CONSTRAINT `fk_solicitacao_privacidade_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `transacao` ADD CONSTRAINT `fk_transacao_fatura_pagamento` FOREIGN KEY(`fatura_pagamento_id`) REFERENCES `fatura`(`id`);

ALTER TABLE `transacao` ADD CONSTRAINT `fk_transacao_conta` FOREIGN KEY(`conta_id`) REFERENCES `conta`(`id`);

ALTER TABLE `recorrencia_geracao` ADD CONSTRAINT `fk_recorrencia_geracao_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `revisao_lancamento_importado` ADD CONSTRAINT `fk_revisao_lancamento_importado_lancamento` FOREIGN KEY(`lancamento_importado_id`) REFERENCES `lancamento_importado`(`id`);

ALTER TABLE `importacao_financeira` ADD CONSTRAINT `fk_importacao_financeira_conta` FOREIGN KEY(`conta_id`) REFERENCES `conta`(`id`);

ALTER TABLE `ajuste_saldo_conta` ADD CONSTRAINT `fk_ajuste_saldo_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `reembolso_divisao` ADD CONSTRAINT `fk_reembolso_pagador` FOREIGN KEY(`pagador_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `lancamento_importado` ADD CONSTRAINT `fk_lancamento_importado_transacao` FOREIGN KEY(`transacao_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `obrigacao_financeira` ADD CONSTRAINT `fk_obrigacao_financeira_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `investimento` ADD CONSTRAINT `fk_investimento_conta` FOREIGN KEY(`conta_origem_id`) REFERENCES `conta`(`id`);

ALTER TABLE `lancamento_importado` ADD CONSTRAINT `fk_lancamento_importado_item` FOREIGN KEY(`item_id`) REFERENCES `item`(`id`);

ALTER TABLE `transacao` ADD CONSTRAINT `fk_transacao_recorrencia` FOREIGN KEY(`recorrencia_id`) REFERENCES `recorrencia`(`id`);

ALTER TABLE `divisao_compartilhada` ADD CONSTRAINT `fk_divisao_compartilhada_criador` FOREIGN KEY(`criador_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `categoria` ADD CONSTRAINT `fk_categoria_pai` FOREIGN KEY(`categoria_pai_id`) REFERENCES `categoria`(`id`);

ALTER TABLE `parcela_financiamento` ADD CONSTRAINT `fk_parcela_financiamento` FOREIGN KEY(`financiamento_id`) REFERENCES `financiamento`(`id`);

ALTER TABLE `alocacao_pagamento_divisao` ADD CONSTRAINT `fk_alocacao_divisao_vinculo` FOREIGN KEY(`vinculo_id`) REFERENCES `transacao_divisao_compartilhada`(`id`);

ALTER TABLE `recorrencia` ADD CONSTRAINT `fk_recorrencia_categoria` FOREIGN KEY(`categoria_id`) REFERENCES `categoria`(`id`);

ALTER TABLE `reembolso_divisao` ADD CONSTRAINT `fk_reembolso_transacao` FOREIGN KEY(`transacao_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `pagamento_fatura` ADD CONSTRAINT `fk_pagamento_fatura_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `lancamento_importado` ADD CONSTRAINT `fk_lancamento_importado_importacao` FOREIGN KEY(`importacao_id`) REFERENCES `importacao_financeira`(`id`);

ALTER TABLE `pagamento_fatura` ADD CONSTRAINT `fk_pagamento_fatura_transacao` FOREIGN KEY(`transacao_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `compra_parcelada` ADD CONSTRAINT `fk_compra_parcelada_cartao` FOREIGN KEY(`cartao_id`) REFERENCES `cartao_credito`(`id`);

ALTER TABLE `ajuste_saldo_conta` ADD CONSTRAINT `fk_ajuste_saldo_conta` FOREIGN KEY(`conta_id`) REFERENCES `conta`(`id`);

ALTER TABLE `aplicacao_credito_fatura` ADD CONSTRAINT `fk_aplicacao_credito_fatura` FOREIGN KEY(`fatura_destino_id`) REFERENCES `fatura`(`id`);

ALTER TABLE `previsao_mensal` ADD CONSTRAINT `fk_previsao_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `parcela_financiamento_historico` ADD CONSTRAINT `fk_parcela_historico_financiamento` FOREIGN KEY(`financiamento_id`) REFERENCES `financiamento`(`id`);

ALTER TABLE `recorrencia_geracao` ADD CONSTRAINT `fk_recorrencia_geracao_transacao` FOREIGN KEY(`transacao_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `transacao` ADD CONSTRAINT `fk_transacao_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `recorrencia_geracao` ADD CONSTRAINT `fk_recorrencia_geracao_recorrencia` FOREIGN KEY(`recorrencia_id`) REFERENCES `recorrencia`(`id`);

ALTER TABLE `banco` ADD CONSTRAINT `fk_banco_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `pagamento_fatura` ADD CONSTRAINT `fk_pagamento_fatura_fatura` FOREIGN KEY(`fatura_id`) REFERENCES `fatura`(`id`);

ALTER TABLE `refresh_token` ADD CONSTRAINT `fk_refresh_token_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `recorrencia_geracao` ADD CONSTRAINT `fk_recorrencia_geracao_categoria` FOREIGN KEY(`categoria_id`) REFERENCES `categoria`(`id`);

ALTER TABLE `conta` ADD CONSTRAINT `fk_conta_banco` FOREIGN KEY(`banco_id`) REFERENCES `banco`(`id`);

ALTER TABLE `pagamento_obrigacao` ADD CONSTRAINT `fk_pagamento_obrigacao_transacao` FOREIGN KEY(`transacao_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `investimento` ADD CONSTRAINT `fk_investimento_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `recorrencia` ADD CONSTRAINT `fk_recorrencia_meio_pagamento` FOREIGN KEY(`meio_pagamento_id`) REFERENCES `meio_pagamento`(`id`);

ALTER TABLE `transferencia_conta` ADD CONSTRAINT `fk_transferencia_conta_origem` FOREIGN KEY(`conta_origem_id`) REFERENCES `conta`(`id`);

ALTER TABLE `alocacao_pagamento_divisao` ADD CONSTRAINT `fk_alocacao_divisao_cancelada_por` FOREIGN KEY(`cancelada_por`) REFERENCES `usuario`(`id`);

ALTER TABLE `transacao` ADD CONSTRAINT `fk_transacao_categoria` FOREIGN KEY(`categoria_id`) REFERENCES `categoria`(`id`);

ALTER TABLE `compra_parcelada` ADD CONSTRAINT `fk_compra_parcelada_conta` FOREIGN KEY(`conta_id`) REFERENCES `conta`(`id`);

ALTER TABLE `pagamento_obrigacao` ADD CONSTRAINT `fk_pagamento_obrigacao_obrigacao` FOREIGN KEY(`obrigacao_id`) REFERENCES `obrigacao_financeira`(`id`);

ALTER TABLE `financiamento` ADD CONSTRAINT `fk_financiamento_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `historico_participante_divisao` ADD CONSTRAINT `fk_historico_participante_divisao` FOREIGN KEY(`divisao_compartilhada_id`) REFERENCES `divisao_compartilhada`(`id`);

ALTER TABLE `movimento_investimento` ADD CONSTRAINT `fk_movimento_investimento` FOREIGN KEY(`investimento_id`) REFERENCES `investimento`(`id`);

ALTER TABLE `participante_divisao_compartilhada` ADD CONSTRAINT `fk_participante_divisao_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `participante_divisao_compartilhada` ADD CONSTRAINT `fk_participante_divisao_compartilhada` FOREIGN KEY(`divisao_compartilhada_id`) REFERENCES `divisao_compartilhada`(`id`);

ALTER TABLE `reembolso_divisao` ADD CONSTRAINT `fk_reembolso_divisao` FOREIGN KEY(`divisao_id`) REFERENCES `divisao_compartilhada`(`id`);

ALTER TABLE `meio_pagamento` ADD CONSTRAINT `fk_meio_pagamento_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `transacao_item` ADD CONSTRAINT `fk_transacao_item_transacao` FOREIGN KEY(`transacao_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `fatura` ADD CONSTRAINT `fk_fatura_cartao` FOREIGN KEY(`cartao_id`) REFERENCES `cartao_credito`(`id`);

ALTER TABLE `fatura` ADD CONSTRAINT `fk_fatura_conta_pagamento` FOREIGN KEY(`conta_pagamento_id`) REFERENCES `conta`(`id`);

ALTER TABLE `conta` ADD CONSTRAINT `fk_conta_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `responsabilidade_transacao_divisao` ADD CONSTRAINT `fk_responsabilidade_divisao_vinculo` FOREIGN KEY(`vinculo_id`) REFERENCES `transacao_divisao_compartilhada`(`id`) ON DELETE CASCADE;

ALTER TABLE `transacao_divisao_compartilhada` ADD CONSTRAINT `fk_transacao_divisao_cancelado_por` FOREIGN KEY(`cancelado_por`) REFERENCES `usuario`(`id`);

ALTER TABLE `posicao_investimento` ADD CONSTRAINT `fk_posicao_investimento_investimento` FOREIGN KEY(`investimento_id`) REFERENCES `investimento`(`id`);

ALTER TABLE `transacao_historico` ADD CONSTRAINT `fk_transacao_historico_transacao` FOREIGN KEY(`transacao_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `transacao_item` ADD CONSTRAINT `fk_transacao_item_categoria` FOREIGN KEY(`categoria_id`) REFERENCES `categoria`(`id`);

ALTER TABLE `recorrencia` ADD CONSTRAINT `fk_recorrencia_conta` FOREIGN KEY(`conta_id`) REFERENCES `conta`(`id`);

ALTER TABLE `transacao_divisao_compartilhada` ADD CONSTRAINT `fk_transacao_divisao_transacao` FOREIGN KEY(`transacao_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `obrigacao_financeira` ADD CONSTRAINT `fk_obrigacao_financeira_conta` FOREIGN KEY(`conta_pagamento_id`) REFERENCES `conta`(`id`);

ALTER TABLE `parcela_financiamento` ADD CONSTRAINT `fk_parcela_financiamento_transacao` FOREIGN KEY(`transacao_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `transferencia_conta` ADD CONSTRAINT `fk_transferencia_conta_destino` FOREIGN KEY(`conta_destino_id`) REFERENCES `conta`(`id`);

ALTER TABLE `obrigacao_financeira` ADD CONSTRAINT `fk_obrigacao_financeira_transacao` FOREIGN KEY(`transacao_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `recorrencia_geracao` ADD CONSTRAINT `fk_recorrencia_geracao_conta` FOREIGN KEY(`conta_id`) REFERENCES `conta`(`id`);

ALTER TABLE `investimento` ADD CONSTRAINT `fk_investimento_conta_custodia` FOREIGN KEY(`conta_custodia_id`) REFERENCES `conta`(`id`);

ALTER TABLE `configuracao_compartilhamento` ADD CONSTRAINT `fk_config_compartilhamento_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `lancamento_importado` ADD CONSTRAINT `fk_lancamento_importado_obrigacao` FOREIGN KEY(`obrigacao_financeira_id`) REFERENCES `obrigacao_financeira`(`id`);

ALTER TABLE `aplicacao_credito_fatura` ADD CONSTRAINT `fk_aplicacao_credito_pagamento` FOREIGN KEY(`pagamento_origem_id`) REFERENCES `pagamento_fatura`(`id`);

ALTER TABLE `movimento_investimento` ADD CONSTRAINT `fk_movimento_investimento_origem` FOREIGN KEY(`movimento_origem_id`) REFERENCES `movimento_investimento`(`id`);

ALTER TABLE `historico_participante_divisao` ADD CONSTRAINT `fk_historico_participante_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `categoria` ADD CONSTRAINT `fk_categoria_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `movimento_investimento` ADD CONSTRAINT `fk_movimento_transacao` FOREIGN KEY(`transacao_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `transacao_historico` ADD CONSTRAINT `fk_transacao_historico_usuario` FOREIGN KEY(`alterado_por`) REFERENCES `usuario`(`id`);

ALTER TABLE `financiamento` ADD CONSTRAINT `fk_financiamento_origem` FOREIGN KEY(`financiamento_origem_id`) REFERENCES `financiamento`(`id`);

ALTER TABLE `importacao_financeira` ADD CONSTRAINT `fk_importacao_financeira_banco` FOREIGN KEY(`banco_id`) REFERENCES `banco`(`id`);

ALTER TABLE `obrigacao_financeira` ADD CONSTRAINT `fk_obrigacao_financeira_categoria` FOREIGN KEY(`categoria_id`) REFERENCES `categoria`(`id`);

ALTER TABLE `revisao_lancamento_importado` ADD CONSTRAINT `fk_revisao_lancamento_importado_usuario` FOREIGN KEY(`revisado_por`) REFERENCES `usuario`(`id`);

ALTER TABLE `importacao_financeira` ADD CONSTRAINT `fk_importacao_financeira_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `recorrencia` ADD CONSTRAINT `fk_recorrencia_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `reembolso_divisao` ADD CONSTRAINT `fk_reembolso_cancelado_por` FOREIGN KEY(`cancelado_por`) REFERENCES `usuario`(`id`);

ALTER TABLE `previsao_mensal` ADD CONSTRAINT `fk_previsao_categoria` FOREIGN KEY(`categoria_id`) REFERENCES `categoria`(`id`);

ALTER TABLE `transacao_item` ADD CONSTRAINT `fk_transacao_item_item` FOREIGN KEY(`item_id`) REFERENCES `item`(`id`);

ALTER TABLE `responsabilidade_transacao_divisao` ADD CONSTRAINT `fk_responsabilidade_divisao_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `transferencia_conta` ADD CONSTRAINT `fk_transferencia_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `transacao` ADD CONSTRAINT `fk_transacao_meio_pagamento` FOREIGN KEY(`meio_pagamento_id`) REFERENCES `meio_pagamento`(`id`);

ALTER TABLE `transacao` ADD CONSTRAINT `fk_transacao_fatura` FOREIGN KEY(`fatura_id`) REFERENCES `fatura`(`id`);

ALTER TABLE `alocacao_pagamento_divisao` ADD CONSTRAINT `fk_alocacao_divisao_transacao` FOREIGN KEY(`transacao_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `lancamento_importado` ADD CONSTRAINT `fk_lancamento_importado_categoria` FOREIGN KEY(`categoria_id`) REFERENCES `categoria`(`id`);

ALTER TABLE `financiamento` ADD CONSTRAINT `fk_financiamento_conta` FOREIGN KEY(`conta_id`) REFERENCES `conta`(`id`);

ALTER TABLE `transacao` ADD CONSTRAINT `fk_transacao_compra_parcelada` FOREIGN KEY(`compra_parcelada_id`) REFERENCES `compra_parcelada`(`id`);

ALTER TABLE `compra_parcelada` ADD CONSTRAINT `fk_compra_parcelada_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `cartao_credito` ADD CONSTRAINT `fk_cartao_usuario` FOREIGN KEY(`usuario_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `alocacao_pagamento_divisao` ADD CONSTRAINT `fk_alocacao_divisao_pagador` FOREIGN KEY(`pagador_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `item` ADD CONSTRAINT `fk_item_categoria_padrao` FOREIGN KEY(`categoria_padrao_id`) REFERENCES `categoria`(`id`);

ALTER TABLE `recorrencia_geracao` ADD CONSTRAINT `fk_recorrencia_geracao_meio` FOREIGN KEY(`meio_pagamento_id`) REFERENCES `meio_pagamento`(`id`);

ALTER TABLE `transacao_divisao_compartilhada` ADD CONSTRAINT `fk_transacao_divisao_transacao_ativa` FOREIGN KEY(`transacao_ativa_id`) REFERENCES `transacao`(`id`);

ALTER TABLE `reembolso_divisao` ADD CONSTRAINT `fk_reembolso_recebedor` FOREIGN KEY(`recebedor_id`) REFERENCES `usuario`(`id`);

ALTER TABLE `transacao_divisao_compartilhada` ADD CONSTRAINT `fk_transacao_divisao_compartilhada` FOREIGN KEY(`divisao_compartilhada_id`) REFERENCES `divisao_compartilhada`(`id`);
ALTER TABLE `importacao_financeira`
    ADD CONSTRAINT `uk_importacao_financeira_documento`
    UNIQUE (`usuario_id`, `banco_id`, `hash_arquivo`);

ALTER TABLE `transacao`
    ADD CONSTRAINT `chk_transacao_origem_exclusiva` CHECK (
        (`fatura_id` IS NOT NULL AND `compra_parcelada_id` IS NOT NULL
            AND `fatura_pagamento_id` IS NULL AND `recorrencia_id` IS NULL
            AND `transferencia_id` IS NULL)
        OR
        ((CASE WHEN `fatura_id` IS NOT NULL THEN 1 ELSE 0 END +
          CASE WHEN `fatura_pagamento_id` IS NOT NULL THEN 1 ELSE 0 END +
          CASE WHEN `compra_parcelada_id` IS NOT NULL THEN 1 ELSE 0 END +
          CASE WHEN `recorrencia_id` IS NOT NULL THEN 1 ELSE 0 END +
          CASE WHEN `transferencia_id` IS NOT NULL THEN 1 ELSE 0 END) <= 1)
    );

INSERT INTO `banco` (`nome`, `codigo`, `ativo`, `usuario_id`) VALUES
('Banco do Brasil', '001', TRUE, NULL),
('Bradesco', '237', TRUE, NULL),
('Caixa Econômica Federal', '104', TRUE, NULL),
('Itaú Unibanco', '341', TRUE, NULL),
('Santander', '033', TRUE, NULL),
('Nubank', '260', TRUE, NULL),
('Inter', '077', TRUE, NULL),
('C6 Bank', '336', TRUE, NULL),
('Sicoob', '756', TRUE, NULL),
('Sicredi', '748', TRUE, NULL);
