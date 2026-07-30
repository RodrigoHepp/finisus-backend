-- V8: Cartão de crédito e fatura
CREATE TABLE cartao_credito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    limite DECIMAL(19,2) NOT NULL,
    dia_fechamento INT NOT NULL,
    dia_vencimento INT NOT NULL,
    CONSTRAINT fk_cartao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_cartao_dia_fechamento CHECK (dia_fechamento BETWEEN 1 AND 31),
    CONSTRAINT chk_cartao_dia_vencimento CHECK (dia_vencimento BETWEEN 1 AND 31)
);

CREATE TABLE fatura (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cartao_id BIGINT NOT NULL,
    ano_mes VARCHAR(7) NOT NULL,
    data_fechamento DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ABERTA',
    conta_pagamento_id BIGINT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_fatura_cartao_mes UNIQUE (cartao_id, ano_mes),
    CONSTRAINT fk_fatura_cartao FOREIGN KEY (cartao_id) REFERENCES cartao_credito(id),
    CONSTRAINT fk_fatura_conta_pagamento FOREIGN KEY (conta_pagamento_id) REFERENCES conta(id),
    CONSTRAINT chk_fatura_status CHECK (status IN ('ABERTA', 'FECHADA', 'PAGA'))
);

ALTER TABLE transacao ADD CONSTRAINT fk_transacao_fatura
    FOREIGN KEY (fatura_id) REFERENCES fatura(id);

CREATE INDEX idx_cartao_usuario ON cartao_credito(usuario_id);
