-- Integridade complementar e índices para as consultas paginadas do lote 5.
-- A unicidade (usuario_id, ano_mes, categoria_id) já existe na V13.

ALTER TABLE conta
    ADD CONSTRAINT chk_conta_tipo_banco
    CHECK (
        (tipo = 'FISICO' AND banco_id IS NULL)
        OR (tipo IN ('CORRENTE', 'POUPANCA', 'APLICACAO') AND banco_id IS NOT NULL)
    );

ALTER TABLE rateio_despesa
    ADD CONSTRAINT chk_rateio_participante_dados
    CHECK (
        (tipo_participante = 'INTERNO' AND usuario_id IS NOT NULL AND nome_externo IS NULL AND email_externo IS NULL)
        OR (tipo_participante = 'EXTERNO' AND usuario_id IS NULL AND nome_externo IS NOT NULL AND email_externo IS NOT NULL)
    );

ALTER TABLE rateio_despesa
    ADD CONSTRAINT chk_rateio_base_calculo
    CHECK (
        (valor_fixo IS NOT NULL AND valor_fixo > 0 AND percentual IS NULL)
        OR (valor_fixo IS NULL AND percentual IS NOT NULL AND percentual > 0 AND percentual <= 100)
    );

CREATE INDEX idx_financiamento_usuario_data_inicio
    ON financiamento (usuario_id, data_inicio, id);

CREATE INDEX idx_despesa_compartilhada_criador_id
    ON despesa_compartilhada (criador_id, id);

CREATE INDEX idx_rateio_despesa_id
    ON rateio_despesa (despesa_compartilhada_id, id);
