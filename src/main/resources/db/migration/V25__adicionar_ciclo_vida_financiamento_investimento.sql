ALTER TABLE financiamento ADD COLUMN status VARCHAR(12) NOT NULL DEFAULT 'ATIVO';
ALTER TABLE financiamento ADD COLUMN cancelada_em TIMESTAMP NULL;
ALTER TABLE financiamento ADD CONSTRAINT chk_financiamento_status
    CHECK (status IN ('ATIVO', 'FINALIZADO', 'CANCELADO'));

UPDATE financiamento SET status = 'FINALIZADO' WHERE finalizado_em IS NOT NULL;

ALTER TABLE movimento_investimento ADD COLUMN estornado_em TIMESTAMP NULL;
ALTER TABLE movimento_investimento ADD COLUMN movimento_origem_id BIGINT NULL;
ALTER TABLE movimento_investimento ADD CONSTRAINT fk_movimento_investimento_origem
    FOREIGN KEY (movimento_origem_id) REFERENCES movimento_investimento(id);
ALTER TABLE movimento_investimento ADD CONSTRAINT uk_movimento_investimento_origem UNIQUE (movimento_origem_id);
