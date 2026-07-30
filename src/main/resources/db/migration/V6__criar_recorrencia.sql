-- V6: Recorrencia
CREATE TABLE recorrencia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    valor_esperado DECIMAL(19,2) NOT NULL,
    dia_do_mes INT NOT NULL,
    categoria_id BIGINT NULL,
    conta_id BIGINT NOT NULL,
    meio_pagamento_id BIGINT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_recorrencia_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_recorrencia_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    CONSTRAINT fk_recorrencia_conta FOREIGN KEY (conta_id) REFERENCES conta(id),
    CONSTRAINT fk_recorrencia_meio_pagamento FOREIGN KEY (meio_pagamento_id) REFERENCES meio_pagamento(id),
    CONSTRAINT chk_recorrencia_tipo CHECK (tipo IN ('ENTRADA', 'SAIDA')),
    CONSTRAINT chk_recorrencia_dia CHECK (dia_do_mes BETWEEN 1 AND 31)
);

CREATE TABLE recorrencia_geracao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recorrencia_id BIGINT NOT NULL,
    ano_mes VARCHAR(7) NOT NULL,
    transacao_id BIGINT NOT NULL,
    CONSTRAINT uk_recorrencia_geracao UNIQUE (recorrencia_id, ano_mes),
    CONSTRAINT fk_recorrencia_geracao_recorrencia FOREIGN KEY (recorrencia_id) REFERENCES recorrencia(id),
    CONSTRAINT fk_recorrencia_geracao_transacao FOREIGN KEY (transacao_id) REFERENCES transacao(id)
);

ALTER TABLE transacao ADD CONSTRAINT fk_transacao_recorrencia
    FOREIGN KEY (recorrencia_id) REFERENCES recorrencia(id);

CREATE INDEX idx_recorrencia_usuario ON recorrencia(usuario_id);
