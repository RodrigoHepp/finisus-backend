-- Controle otimista de pagamento/marcação de atraso e consulta consolidada da previsão.
ALTER TABLE parcela_financiamento
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_parcela_financiamento_status_vencimento
    ON parcela_financiamento (financiamento_id, status, data_vencimento);
