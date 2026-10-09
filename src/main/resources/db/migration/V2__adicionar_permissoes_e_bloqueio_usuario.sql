ALTER TABLE `usuario`
    ADD COLUMN `tentativas_login_invalidas` INT DEFAULT 0 NOT NULL;

ALTER TABLE `usuario`
    ADD COLUMN `bloqueado` BOOLEAN DEFAULT FALSE NOT NULL;

CREATE TABLE `usuario_permissao` (
    `usuario_id` BIGINT NOT NULL,
    `permissao` VARCHAR(40) NOT NULL,
    PRIMARY KEY (`usuario_id`, `permissao`),
    CONSTRAINT `fk_usuario_permissao_usuario`
        FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE
);
