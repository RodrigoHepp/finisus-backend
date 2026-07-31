ALTER TABLE despesa_compartilhada DROP FOREIGN KEY fk_despesa_compartilhada_transacao;
ALTER TABLE despesa_compartilhada DROP INDEX uk_despesa_compartilhada_transacao;
CREATE INDEX idx_despesa_compartilhada_transacao ON despesa_compartilhada(transacao_id);
ALTER TABLE despesa_compartilhada ADD CONSTRAINT fk_despesa_compartilhada_transacao
    FOREIGN KEY (transacao_id) REFERENCES transacao(id);
ALTER TABLE despesa_compartilhada ADD COLUMN transacao_item_id BIGINT NULL;
ALTER TABLE despesa_compartilhada ADD COLUMN tipo_alvo VARCHAR(20) NOT NULL DEFAULT 'TRANSACAO';
ALTER TABLE despesa_compartilhada ADD CONSTRAINT fk_despesa_compartilhada_item
    FOREIGN KEY (transacao_item_id) REFERENCES transacao_item(id);
ALTER TABLE despesa_compartilhada ADD CONSTRAINT chk_despesa_tipo_alvo
    CHECK (tipo_alvo IN ('TRANSACAO', 'ITEM_TRANSACAO'));
CREATE UNIQUE INDEX uk_despesa_compartilhada_item ON despesa_compartilhada(transacao_item_id);
