# Desenvolvimento

## Ambiente local

Para executar o perfil `dev`, instale JDK 25, Maven e MySQL. O datasource padrão aponta para um banco local chamado `finisus`; Flyway cria e atualiza o schema durante a inicialização.

Crie `src/main/resources/application-dev.properties` a partir do arquivo de exemplo e defina as variáveis de banco e JWT:

```powershell
Copy-Item src/main/resources/application-dev.example.properties src/main/resources/application-dev.properties
$env:FINISUS_DB_USERNAME = 'seu-usuario'
$env:FINISUS_DB_PASSWORD = 'sua-senha'
$env:FINISUS_JWT_PRIVATE_KEY_LOCATION = 'file:C:/caminho/chave-privada.pem'
$env:FINISUS_JWT_PUBLIC_KEY_LOCATION = 'file:C:/caminho/chave-publica.pem'
```

Inicie a aplicação com:

```powershell
mvn "-Dspring-boot.run.profiles=dev" spring-boot:run
```

## Verificação

```powershell
mvn clean verify
```

O comando compila o projeto e executa a suíte. Com a aplicação iniciada, use `/actuator/health` para verificar disponibilidade e o Swagger UI para explorar a API.

## Arquivos locais

`application-dev.properties`, metadados de IDE e `target/` não devem ser versionados. Migrations Flyway aplicadas não são editadas: uma evolução de banco deve criar uma nova migration versionada.
