ALTER TABLE fatura
    ADD COLUMN cancelada_em DATE NULL;

ALTER TABLE fatura DROP CONSTRAINT chk_fatura_status;
ALTER TABLE fatura ADD CONSTRAINT chk_fatura_status
    CHECK (status IN ('ABERTA', 'FECHADA', 'PAGA', 'CANCELADA'));
