-- V10: Financiamento
CREATE TABLE financiamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    descricao VARCHAR(300) NOT NULL,
    principal DECIMAL(19,2) NOT NULL,
    taxa_juros_mensal DECIMAL(8,6) NOT NULL,
    numero_parcelas INT NOT NULL,
    data_inicio DATE NOT NULL,
    conta_id BIGINT NOT NULL,
    CONSTRAINT fk_financiamento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_financiamento_conta FOREIGN KEY (conta_id) REFERENCES conta(id)
);

CREATE TABLE parcela_financiamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    financiamento_id BIGINT NOT NULL,
    numero INT NOT NULL,
    valor DECIMAL(19,2) NOT NULL,
    data_vencimento DATE NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'PENDENTE',
    CONSTRAINT uk_parcela_financiamento UNIQUE (financiamento_id, numero),
    CONSTRAINT fk_parcela_financiamento FOREIGN KEY (financiamento_id) REFERENCES financiamento(id),
    CONSTRAINT chk_parcela_status CHECK (status IN ('PENDENTE', 'PAGA', 'ATRASADA'))
);

CREATE INDEX idx_financiamento_usuario ON financiamento(usuario_id);
