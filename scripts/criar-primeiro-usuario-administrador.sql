-- Execute somente em um banco vazio, depois da aplicação das migrations.
-- Gere o hash BCrypt fora do banco e nunca registre a senha em texto puro.
SET @nome = 'Administrador inicial';
SET @email = 'admin@exemplo.local';
SET @senha_hash_bcrypt = '$2a$10$SUBSTITUA_PELO_HASH_BCRYPT';

START TRANSACTION;

SET @usuario_id = NULL;

INSERT INTO usuario (nome, email, senha_hash, ativo, criado_em, sessao_versao,
                     tentativas_login_invalidas, bloqueado)
SELECT @nome, @email, @senha_hash_bcrypt, TRUE, CURRENT_TIMESTAMP, 0, 0, FALSE
WHERE NOT EXISTS (SELECT 1 FROM usuario);

SET @usuario_id = IF(ROW_COUNT() = 1, LAST_INSERT_ID(), NULL);

INSERT INTO usuario_permissao (usuario_id, permissao)
SELECT @usuario_id, permissao
FROM (
    SELECT 'USUARIO_CADASTRAR' AS permissao
    UNION ALL SELECT 'USUARIO_DESBLOQUEAR'
    UNION ALL SELECT 'DOCUMENTACAO_API_LER'
    UNION ALL SELECT 'OBSERVABILIDADE_LER'
) permissoes
WHERE @usuario_id IS NOT NULL;

COMMIT;

SELECT @usuario_id AS usuario_administrador_criado;
