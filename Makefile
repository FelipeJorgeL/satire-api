.PHONY: up down logs status shell run stop test build

-include .env

POSTGRES_DB ?= satire
POSTGRES_USER ?= satire

up:
	docker compose up -d postgres

down:
	docker compose down

logs:
	docker compose logs -f postgres

status:
	docker compose ps

shell:
	docker compose exec postgres psql -U $(POSTGRES_USER) -d $(POSTGRES_DB)

run:
	docker compose --profile app up -d --build

stop:
	docker compose --profile app stop api

test:
	mvn test

build:
	mvn verify
