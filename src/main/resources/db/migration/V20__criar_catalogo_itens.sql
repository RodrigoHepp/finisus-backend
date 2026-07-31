CREATE TABLE item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(300) NOT NULL,
    categoria_padrao_id BIGINT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_item_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_item_categoria_padrao FOREIGN KEY (categoria_padrao_id) REFERENCES categoria(id)
);

CREATE INDEX idx_item_usuario_ativo_nome ON item(usuario_id, ativo, nome);

ALTER TABLE transacao_item ADD COLUMN item_id BIGINT NULL;
ALTER TABLE transacao_item ADD CONSTRAINT fk_transacao_item_item FOREIGN KEY (item_id) REFERENCES item(id);
CREATE INDEX idx_transacao_item_item ON transacao_item(item_id);
