-- V2: Banco (catálogo pré-populado)
CREATE TABLE banco (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    usuario_id BIGINT NULL,
    CONSTRAINT uk_banco_codigo_usuario UNIQUE (codigo, usuario_id),
    CONSTRAINT fk_banco_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

INSERT INTO banco (nome, codigo, ativo, usuario_id) VALUES
('Banco do Brasil', '001', TRUE, NULL),
('Bradesco', '237', TRUE, NULL),
('Caixa Econômica Federal', '104', TRUE, NULL),
('Itaú Unibanco', '341', TRUE, NULL),
('Santander', '033', TRUE, NULL),
('Nubank', '260', TRUE, NULL),
('Inter', '077', TRUE, NULL),
('C6 Bank', '336', TRUE, NULL),
('Sicoob', '756', TRUE, NULL),
('Sicredi', '748', TRUE, NULL);
