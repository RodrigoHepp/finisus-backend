-- V4: Categoria e MeioPagamento
CREATE TABLE categoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    categoria_pai_id BIGINT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_categoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_categoria_pai FOREIGN KEY (categoria_pai_id) REFERENCES categoria(id)
);

CREATE TABLE meio_pagamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_meio_pagamento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE INDEX idx_categoria_usuario ON categoria(usuario_id);
CREATE INDEX idx_meio_pagamento_usuario ON meio_pagamento(usuario_id);
