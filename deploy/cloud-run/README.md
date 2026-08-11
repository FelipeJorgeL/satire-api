# Preparação do Cloud Run

Esta pasta prepara o deploy da `satire-api` no Google Cloud Run. A Fase 4 não executa deploy, não altera o Neon e não cria endpoints ou classes Java.

## Parâmetros definidos

- Porta do container: `8080`.
- Escala: mínimo `0` e máximo `5` instâncias.
- Concorrência: `80` requisições por instância.
- Timeout: `60s`.
- CPU/memória: `1 vCPU` e `512Mi`.
- Ingress: `all`, pois a loja precisa ser pública.
- Imagem: Artifact Registry.
- Logs: stdout/stderr da aplicação, coletados automaticamente pelo Cloud Run.
- Encerramento: graceful shutdown com janela de `20s`.

O arquivo `service.template.yaml` é um modelo. Substitua todos os valores `REPLACE_WITH_*`, `PROJECT_ID`, `REPOSITORY` e `TAG` antes de aplicá-lo. Não versione um manifesto renderizado com valores locais ou credenciais.

## Service account e secrets

Use uma service account exclusiva para o runtime, por exemplo `satire-api-runtime`. Ela precisa receber somente `roles/secretmanager.secretAccessor` nos quatro secrets usados pelo serviço:

| Variável | Secret Manager | Conteúdo |
|---|---|---|
| `DB_URL` | `satire-db-url` | JDBC URL do Neon com `sslmode=verify-full` |
| `DB_PASSWORD` | `satire-db-password` | Senha atual do usuário do Neon |
| `JWT_SECRET` | `satire-jwt-secret` | Segredo JWT forte e aleatório |
| `SENDGRID_API_KEY` | `satire-sendgrid-api-key` | API key do SendGrid com permissões mínimas |

`DB_USERNAME`, `APP_BASE_URL` e `MAIL_FROM` são valores não secretos, mas devem ser definidos no ambiente de deploy. `ADMIN_ACCESS_TOKEN` não deve ser configurado no Cloud Run: o painel usa o JWT emitido pelo fluxo normal de autenticação e a aplicação mantém a autorização `ROLE_ADMIN`.

O deployer que cria a revisão precisa de permissões de Cloud Run e Artifact Registry. Não conceda essas permissões à service account de runtime sem necessidade. O runtime usa Neon, portanto não precisa de papel de Cloud SQL.

## Imagem

Exemplo para construir e publicar sem usar credenciais no Dockerfile:

```powershell
$IMAGE = "REGION-docker.pkg.dev/PROJECT_ID/REPOSITORY/satire-api:TAG"
gcloud builds submit --tag $IMAGE .
```

O Dockerfile compila o jar em uma etapa Maven e executa somente o JRE como usuário não root. Os parâmetros de memória da JVM são relativos ao limite da instância do Cloud Run; nenhum segredo é embutido na imagem.

## Configuração e primeiro deploy

O `REQUIRE_SECURE_TRANSPORT=true` exige que `APP_BASE_URL` seja uma URL HTTPS válida e que `DB_URL` contenha `sslmode=verify-full`. Prefira definir desde o início um domínio HTTPS conhecido. Se for usar a URL automática `run.app`, faça o bootstrap com a URL provisória apenas para obter o endereço do serviço e, imediatamente depois, atualize `APP_BASE_URL` e ative `REQUIRE_SECURE_TRANSPORT=true`.

Exemplo de configuração por comando, sem imprimir os valores dos secrets:

```powershell
gcloud run deploy satire-api `
  --image $IMAGE `
  --region REGION `
  --service-account satire-api-runtime@PROJECT_ID.iam.gserviceaccount.com `
  --port 8080 `
  --min 0 `
  --max 5 `
  --concurrency 80 `
  --timeout 60s `
  --ingress all `
  --allow-unauthenticated `
  --set-env-vars "APP_BASE_URL=https://PUBLIC_BASE_URL,REQUIRE_SECURE_TRANSPORT=true,DB_USERNAME=NEON_USERNAME,MAIL_FROM=VERIFIED_SENDER" `
  --set-secrets "DB_URL=satire-db-url:latest,DB_PASSWORD=satire-db-password:latest,JWT_SECRET=satire-jwt-secret:latest,SENDGRID_API_KEY=satire-sendgrid-api-key:latest"
```

O acesso público ao serviço é necessário para a loja; isso não libera o painel. Os controllers administrativos continuam exigindo `ROLE_ADMIN` na aplicação. A validação final dessa separação pertence à Fase 5.

## Logs e operação

O Cloud Run coleta stdout/stderr automaticamente. Não registre `DB_URL`, `DB_PASSWORD`, `JWT_SECRET`, `SENDGRID_API_KEY`, headers `Authorization`, cookies, tokens ou corpos contendo credenciais. A aplicação não deve ler secrets para exibi-los em logs.

Antes do deploy, revise os logs locais e procure por chamadas de logging que incluam credenciais. Depois do deploy, faça a mesma verificação nos logs da revisão. Rotação de secret exige uma nova revisão ou reinicialização das instâncias, porque os valores são carregados no startup.

Comandos de leitura para a etapa posterior:

```powershell
gcloud run services describe satire-api --region REGION --format="yaml(status.url,status.latestCreatedRevisionName)"
gcloud run services logs read satire-api --region REGION --limit 100
```

Os comandos acima são apenas referência operacional e não foram executados nesta fase.
