# Satire API

API de e-commerce construída como monólito modular com Spring Boot.

## Requisitos

- Java 17
- Maven 3.6.3 ou superior
- PostgreSQL

## Configuração

A aplicação lê a conexão com o banco das seguintes variáveis:

- `DB_URL` (padrão: `jdbc:postgresql://localhost:5432/satire`)
- `DB_USERNAME` (padrão: `postgres`)
- `DB_PASSWORD` (sem valor padrão)

Não versione credenciais. Para executar os testes de arquitetura não é necessário iniciar o PostgreSQL.

## Ambiente local com Docker

Copie `.env.example` para `.env` e ajuste somente valores locais. O arquivo `.env` não é versionado.

```powershell
Copy-Item .env.example .env
docker compose up -d postgres
docker compose ps
```

O PostgreSQL fica disponível em `localhost:5432`. A migration Flyway é aplicada quando a API inicia.

Para executar banco e API em contêineres:

```powershell
docker compose --profile app up -d --build
```

Os mesmos comandos possuem atalhos no `Makefile` para ambientes com GNU Make.

## Arquitetura

Os módulos de negócio ficam em `domain/<module>`. A raiz de cada módulo é sua API pública; detalhes de implementação ficam em `internal`.

Módulos iniciais:

- `customer`
- `catalog`
- `cart`
- `inventory`
- `order`
- `payment`
- `shipping`

Spring Modulith verifica ciclos e acessos indevidos entre módulos durante os testes.

## Validação

```powershell
mvn test
mvn package
```
