ALTER TABLE despesa_compartilhada ADD COLUMN status VARCHAR(10) NOT NULL DEFAULT 'ATIVA';
ALTER TABLE despesa_compartilhada ADD COLUMN cancelada_em TIMESTAMP NULL;
ALTER TABLE despesa_compartilhada ADD CONSTRAINT chk_despesa_compartilhada_status
    CHECK (status IN ('ATIVA', 'CANCELADA'));

ALTER TABLE rateio_despesa DROP CONSTRAINT chk_rateio_status;
ALTER TABLE rateio_despesa ADD CONSTRAINT chk_rateio_status
    CHECK (status IN ('PENDENTE', 'ACEITO', 'RECUSADO', 'PAGO', 'CANCELADO'));

CREATE INDEX idx_despesa_compartilhada_transacao_status ON despesa_compartilhada(transacao_id, status);
CREATE INDEX idx_despesa_compartilhada_item_status ON despesa_compartilhada(transacao_item_id, status);
CREATE INDEX idx_rateio_despesa_usuario_status ON rateio_despesa(usuario_id, status, id);
