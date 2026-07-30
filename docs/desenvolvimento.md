# Desenvolvimento

Java 25, Maven e um MySQL local são necessários para executar a aplicação com o perfil `dev`.

```powershell
Copy-Item src/main/resources/application-dev.example.yml src/main/resources/application-dev.yml
$env:FINISUS_DB_USERNAME = 'seu-usuario'
$env:FINISUS_DB_PASSWORD = 'sua-senha'
$env:FINISUS_JWT_PRIVATE_KEY_LOCATION = 'file:C:/caminho/chave-privada.pem'
$env:FINISUS_JWT_PUBLIC_KEY_LOCATION = 'file:C:/caminho/chave-publica.pem'
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

`application-dev.yml` é local e está no `.gitignore`. Não versione chaves JWT, senhas de banco ou artefatos em `target/`.
