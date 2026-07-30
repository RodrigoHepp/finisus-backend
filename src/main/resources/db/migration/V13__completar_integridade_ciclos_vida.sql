-- Evolução aditiva para suportar inativação de contas e leituras determinísticas de previsão.
ALTER TABLE conta ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;

CREATE UNIQUE INDEX uk_previsao_usuario_mes_categoria
    ON previsao_mensal (usuario_id, ano_mes, categoria_id);

CREATE INDEX idx_parcela_financiamento_vencimento_status
    ON parcela_financiamento (data_vencimento, status);
