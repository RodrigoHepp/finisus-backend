-- V3: Conta
CREATE TABLE conta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    banco_id BIGINT NULL,
    saldo DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_conta_banco FOREIGN KEY (banco_id) REFERENCES banco(id),
    CONSTRAINT chk_conta_tipo CHECK (tipo IN ('FISICO', 'CORRENTE', 'POUPANCA', 'APLICACAO'))
);

CREATE INDEX idx_conta_usuario ON conta(usuario_id);
