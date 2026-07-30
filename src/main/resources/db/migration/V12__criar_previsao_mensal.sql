-- V12: Previsão mensal (read model)
CREATE TABLE previsao_mensal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    ano_mes VARCHAR(7) NOT NULL,
    categoria_id BIGINT NULL,
    valor_projetado_entrada DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    valor_projetado_saida DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    calculado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_previsao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_previsao_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);

CREATE INDEX idx_previsao_usuario_mes ON previsao_mensal(usuario_id, ano_mes);
