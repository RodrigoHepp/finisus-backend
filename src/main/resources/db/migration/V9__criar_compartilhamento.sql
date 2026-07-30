-- V9: Despesa compartilhada
CREATE TABLE configuracao_compartilhamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    aceita_compartilhamento BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_config_compartilhamento_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_config_compartilhamento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE TABLE despesa_compartilhada (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transacao_id BIGINT NOT NULL,
    criador_id BIGINT NOT NULL,
    tipo_rateio VARCHAR(15) NOT NULL,
    CONSTRAINT uk_despesa_compartilhada_transacao UNIQUE (transacao_id),
    CONSTRAINT fk_despesa_compartilhada_transacao FOREIGN KEY (transacao_id) REFERENCES transacao(id),
    CONSTRAINT fk_despesa_compartilhada_criador FOREIGN KEY (criador_id) REFERENCES usuario(id),
    CONSTRAINT chk_despesa_tipo_rateio CHECK (tipo_rateio IN ('VALOR_FIXO', 'PERCENTUAL'))
);

CREATE TABLE rateio_despesa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    despesa_compartilhada_id BIGINT NOT NULL,
    tipo_participante VARCHAR(10) NOT NULL,
    usuario_id BIGINT NULL,
    nome_externo VARCHAR(150) NULL,
    email_externo VARCHAR(255) NULL,
    valor_fixo DECIMAL(19,2) NULL,
    percentual DECIMAL(5,2) NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'PENDENTE',
    CONSTRAINT fk_rateio_despesa_compartilhada FOREIGN KEY (despesa_compartilhada_id) REFERENCES despesa_compartilhada(id),
    CONSTRAINT fk_rateio_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_rateio_tipo_participante CHECK (tipo_participante IN ('INTERNO', 'EXTERNO')),
    CONSTRAINT chk_rateio_status CHECK (status IN ('PENDENTE', 'ACEITO', 'RECUSADO', 'PAGO'))
);

ALTER TABLE transacao ADD CONSTRAINT fk_transacao_despesa_compartilhada
    FOREIGN KEY (despesa_compartilhada_id) REFERENCES despesa_compartilhada(id);
