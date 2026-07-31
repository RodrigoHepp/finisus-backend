# Autenticação

O Finisus autentica requisições protegidas com JWT assinado por chaves RSA. O access token deve ser enviado no cabeçalho `Authorization: Bearer <token>`.

## Fluxos disponíveis

- `POST /api/v1/auth/cadastro` cria um usuário e retorna seus dados públicos.
- `POST /api/v1/auth/login` recebe e-mail e senha, e retorna access token, refresh token e a data de expiração do access token.
- `POST /api/v1/auth/refresh` recebe um refresh token e emite um novo par de tokens.

O access token contém o identificador numérico do usuário em `sub`, o tipo `access` e a versão da sessão. Antes de autenticar a requisição, `UsuarioSessaoJwtValidator` verifica o tipo do token, se o usuário está ativo e se a versão da sessão continua válida.

Depois da validação, a infraestrutura converte o JWT em `UsuarioAutenticado`. Controllers recebem somente o ID por `@UsuarioAtual`; casos de uso não dependem de JWT, Spring Security ou `SecurityContext`.

## Configuração segura

O perfil `dev` recebe os locais das chaves pelas variáveis `FINISUS_JWT_PRIVATE_KEY_LOCATION` e `FINISUS_JWT_PUBLIC_KEY_LOCATION`. Senhas, chaves privadas, tokens e arquivos de configuração local não devem ser versionados, registrados em logs nem enviados em respostas de erro.
