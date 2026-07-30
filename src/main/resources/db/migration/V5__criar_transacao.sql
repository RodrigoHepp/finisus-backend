-- V5: Transacao, itens e histórico
CREATE TABLE transacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    valor DECIMAL(19,2) NOT NULL,
    data DATE NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    conta_id BIGINT NOT NULL,
    categoria_id BIGINT NULL,
    meio_pagamento_id BIGINT NULL,
    fatura_id BIGINT NULL,
    compra_parcelada_id BIGINT NULL,
    recorrencia_id BIGINT NULL,
    despesa_compartilhada_id BIGINT NULL,
    estornado_em TIMESTAMP NULL,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_transacao_conta FOREIGN KEY (conta_id) REFERENCES conta(id),
    CONSTRAINT fk_transacao_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    CONSTRAINT fk_transacao_meio_pagamento FOREIGN KEY (meio_pagamento_id) REFERENCES meio_pagamento(id),
    CONSTRAINT chk_transacao_tipo CHECK (tipo IN ('ENTRADA', 'SAIDA')),
    CONSTRAINT chk_transacao_origem_exclusiva CHECK (
        (CASE WHEN fatura_id IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN compra_parcelada_id IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN recorrencia_id IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN despesa_compartilhada_id IS NOT NULL THEN 1 ELSE 0 END) <= 1
    )
);

CREATE TABLE transacao_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transacao_id BIGINT NOT NULL,
    descricao VARCHAR(300) NOT NULL,
    valor DECIMAL(19,2) NOT NULL,
    categoria_id BIGINT NULL,
    CONSTRAINT fk_transacao_item_transacao FOREIGN KEY (transacao_id) REFERENCES transacao(id),
    CONSTRAINT fk_transacao_item_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);

CREATE TABLE transacao_historico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transacao_id BIGINT NOT NULL,
    campo_alterado VARCHAR(100) NOT NULL,
    valor_anterior VARCHAR(500) NULL,
    valor_novo VARCHAR(500) NULL,
    alterado_por BIGINT NOT NULL,
    alterado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transacao_historico_transacao FOREIGN KEY (transacao_id) REFERENCES transacao(id),
    CONSTRAINT fk_transacao_historico_usuario FOREIGN KEY (alterado_por) REFERENCES usuario(id)
);

CREATE INDEX idx_transacao_usuario ON transacao(usuario_id);
CREATE INDEX idx_transacao_conta ON transacao(conta_id);
CREATE INDEX idx_transacao_data ON transacao(data);
