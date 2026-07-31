-- Finalização por refinanciamento mantém o histórico das parcelas pagas.
ALTER TABLE financiamento
    ADD COLUMN finalizado_em TIMESTAMP NULL;
