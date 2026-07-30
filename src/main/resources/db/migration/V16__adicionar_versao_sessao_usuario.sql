-- Invalidacao de access tokens por usuario: a versao e verificada a cada autenticacao JWT.
ALTER TABLE usuario
    ADD COLUMN sessao_versao BIGINT NOT NULL DEFAULT 0;
