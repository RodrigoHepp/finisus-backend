-- V11: Investimento
CREATE TABLE investimento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    conta_origem_id BIGINT NOT NULL,
    CONSTRAINT fk_investimento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_investimento_conta FOREIGN KEY (conta_origem_id) REFERENCES conta(id),
    CONSTRAINT chk_investimento_tipo CHECK (tipo IN ('RENDA_FIXA', 'RENDA_VARIAVEL', 'FUNDO', 'CRIPTO', 'OUTRO'))
);

CREATE TABLE movimento_investimento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    investimento_id BIGINT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    valor DECIMAL(19,2) NOT NULL,
    data DATE NOT NULL,
    transacao_id BIGINT NULL,
    CONSTRAINT fk_movimento_investimento FOREIGN KEY (investimento_id) REFERENCES investimento(id),
    CONSTRAINT fk_movimento_transacao FOREIGN KEY (transacao_id) REFERENCES transacao(id),
    CONSTRAINT chk_movimento_tipo CHECK (tipo IN ('APORTE', 'RESGATE'))
);

CREATE INDEX idx_investimento_usuario ON investimento(usuario_id);
