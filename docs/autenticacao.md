# Autenticação

As rotas protegidas usam access tokens JWT no cabeçalho `Authorization: Bearer <token>`.

O token de acesso contém `sub` como o identificador numérico do usuário, além de `type=access` e da versão da sessão. Antes de uma requisição ser autenticada, `UsuarioSessaoJwtValidator` confirma o tipo do token, o usuário ativo e a versão de sessão atual.

Após a validação, a infraestrutura converte o JWT em `UsuarioAutenticado`. Controllers recebem somente o ID pelo parâmetro `@UsuarioAtual Long usuarioId`; os casos de uso continuam recebendo esse identificador explicitamente e não dependem de Spring Security, JWT ou `SecurityContext`.

Tokens não devem ser incluídos em logs, respostas de erro ou configurações versionadas.
