-- V7: Compra parcelada
CREATE TABLE compra_parcelada (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    descricao VARCHAR(300) NOT NULL,
    valor_total DECIMAL(19,2) NOT NULL,
    numero_parcelas INT NOT NULL,
    data_compra DATE NOT NULL,
    categoria_id BIGINT NULL,
    conta_id BIGINT NOT NULL,
    CONSTRAINT fk_compra_parcelada_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_compra_parcelada_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    CONSTRAINT fk_compra_parcelada_conta FOREIGN KEY (conta_id) REFERENCES conta(id),
    CONSTRAINT chk_compra_parcelas CHECK (numero_parcelas > 0)
);

ALTER TABLE transacao ADD CONSTRAINT fk_transacao_compra_parcelada
    FOREIGN KEY (compra_parcelada_id) REFERENCES compra_parcelada(id);

CREATE INDEX idx_compra_parcelada_usuario ON compra_parcelada(usuario_id);
