# Testes

Execute toda a suíte com:

```powershell
mvn test
```

A suíte atual inicializa a aplicação com H2 e executa as migrations Flyway. Testes que exigem MySQL real devem usar Testcontainers e permanecer condicionados à disponibilidade do Docker no ambiente de execução.

Os testes de segurança também cobrem a conversão do `sub` validado do JWT para `UsuarioAutenticado`, incluindo a preservação de authorities.
