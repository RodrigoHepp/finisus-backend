ALTER TABLE despesa_compartilhada ADD CONSTRAINT chk_despesa_alvo_item
    CHECK ((tipo_alvo = 'TRANSACAO' AND transacao_item_id IS NULL)
        OR (tipo_alvo = 'ITEM_TRANSACAO' AND transacao_item_id IS NOT NULL));

CREATE INDEX idx_transacao_fatura_data_id ON transacao(fatura_id, data, id);
