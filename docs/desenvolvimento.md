# Desenvolvimento

Java 25, Maven e um MySQL local são necessários para executar a aplicação com o perfil `dev`.

```powershell
Copy-Item src/main/resources/application-dev.example.properties src/main/resources/application-dev.properties
$env:FINISUS_DB_USERNAME = 'seu-usuario'
$env:FINISUS_DB_PASSWORD = 'sua-senha'
$env:FINISUS_JWT_PRIVATE_KEY_LOCATION = 'file:C:/caminho/chave-privada.pem'
$env:FINISUS_JWT_PUBLIC_KEY_LOCATION = 'file:C:/caminho/chave-publica.pem'
mvn "-Dspring-boot.run.profiles=dev" spring-boot:run
```

`application-dev.properties` é local e está no `.gitignore`. Não versione chaves JWT, senhas de banco ou artefatos em `target/`.
