CREATE TABLE importacao_financeira (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    banco_id BIGINT NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    hash_arquivo CHAR(64) NOT NULL,
    leitor VARCHAR(100) NOT NULL,
    tipo_documento VARCHAR(20) NOT NULL,
    identificador_origem VARCHAR(120) NULL,
    periodo_inicio DATE NULL,
    periodo_fim DATE NULL,
    data_vencimento DATE NULL,
    saldo_inicial DECIMAL(19,2) NULL,
    saldo_final DECIMAL(19,2) NULL,
    valor_total DECIMAL(19,2) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE_REVISAO',
    conta_id BIGINT NULL,
    fatura_id BIGINT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_importacao_financeira_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_importacao_financeira_banco FOREIGN KEY (banco_id) REFERENCES banco(id),
    CONSTRAINT fk_importacao_financeira_conta FOREIGN KEY (conta_id) REFERENCES conta(id),
    CONSTRAINT fk_importacao_financeira_fatura FOREIGN KEY (fatura_id) REFERENCES fatura(id),
    CONSTRAINT chk_importacao_financeira_tipo CHECK (tipo_documento IN ('EXTRATO_CONTA', 'FATURA_CARTAO', 'COBRANCA')),
    CONSTRAINT chk_importacao_financeira_status CHECK (status IN ('PENDENTE_REVISAO', 'CONFIRMADA'))
);

CREATE TABLE lancamento_importado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    importacao_id BIGINT NOT NULL,
    ordem INT NOT NULL,
    data DATE NULL,
    descricao VARCHAR(500) NULL,
    conteudo_original VARCHAR(1000) NOT NULL,
    valor DECIMAL(19,2) NULL,
    tipo VARCHAR(10) NULL,
    pendente_confirmacao BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_pendencia VARCHAR(300) NULL,
    importar BOOLEAN NOT NULL DEFAULT TRUE,
    categoria_id BIGINT NULL,
    item_id BIGINT NULL,
    transacao_id BIGINT NULL,
    CONSTRAINT fk_lancamento_importado_importacao FOREIGN KEY (importacao_id) REFERENCES importacao_financeira(id),
    CONSTRAINT fk_lancamento_importado_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    CONSTRAINT fk_lancamento_importado_item FOREIGN KEY (item_id) REFERENCES item(id),
    CONSTRAINT fk_lancamento_importado_transacao FOREIGN KEY (transacao_id) REFERENCES transacao(id),
    CONSTRAINT chk_lancamento_importado_tipo CHECK (tipo IN ('ENTRADA', 'SAIDA')),
    CONSTRAINT uk_lancamento_importado_ordem UNIQUE (importacao_id, ordem)
);

CREATE INDEX idx_importacao_financeira_usuario_status ON importacao_financeira(usuario_id, status, id);
CREATE INDEX idx_lancamento_importado_transacao ON lancamento_importado(transacao_id);
