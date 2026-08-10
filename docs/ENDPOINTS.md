# Mapa de endpoints da API

Documento de referência do contrato da API, separado entre **Loja** e **Painel administrativo**.

## Como ler este documento

- **Implementado**: rota encontrada em controller da aplicação atual.
- **Planejado**: rota definida para o contrato do módulo, mas ainda sem controller implementado.
- **Externo**: rota recebida de um provedor, autenticada pela assinatura do provedor.
- As tabelas indicadas são as fontes de persistência do módulo; tabelas de segurança e outbox são detalhes internos, não recursos CRUD públicos.
- A lista foi confrontada com o schema Flyway em `src/main/resources/db/migration` e com os controllers existentes.

## Loja

Rotas públicas ou usadas pelo cliente autenticado.

### Conta e autenticação — `customer`

Tabelas: `usuarios`, `perfis`, `usuarios_perfis`, `enderecos`, `refresh_tokens`, `confirmacoes_email`, `confirmacoes_email_outbox`.

| Método | Endpoint | Autorização | Estado | Responsabilidade |
|---|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Público | Implementado | Cadastrar cliente. Responde `202` sem corpo, inclusive para e-mail/CPF já existentes. |
| `POST` | `/api/v1/auth/confirm/resend` | Público | Implementado | Reenviar confirmação de e-mail sem revelar se a conta existe. |
| `POST` | `/api/v1/auth/login` | Público | Implementado | Autenticar cliente e emitir tokens. |
| `POST` | `/api/v1/auth/refresh` | Público | Implementado | Rotacionar o refresh token e emitir novo acesso. |
| `POST` | `/api/v1/auth/logout` | Bearer válido | Implementado | Invalidar a sessão/refresh token do cliente. |
| `POST` | `/api/v1/auth/confirm?token={token}` | Público | Implementado | Consumir o token e confirmar o e-mail. |
| `GET` | `/api/v1/auth/confirm?token={token}` | Público | Implementado | Exibir página HTML intermediária; não consome o token. |
| `GET` | `/api/v1/me` | Bearer válido | Implementado | Consultar o próprio cliente. |
| `PATCH` | `/api/v1/me` | Bearer válido | Implementado | Atualizar dados pessoais permitidos. |
| `DELETE` | `/api/v1/me` | Bearer válido | Implementado | Desativar a própria conta. |
| `GET` | `/api/v1/me/addresses` | Bearer válido | Implementado | Listar endereços do cliente. |
| `POST` | `/api/v1/me/addresses` | Bearer válido | Implementado | Cadastrar endereço. |
| `GET` | `/api/v1/me/addresses/{addressId}` | Bearer válido | Implementado | Consultar endereço próprio. |
| `PATCH` | `/api/v1/me/addresses/{addressId}` | Bearer válido | Implementado | Atualizar endereço próprio. |
| `DELETE` | `/api/v1/me/addresses/{addressId}` | Bearer válido | Implementado | Remover endereço próprio. |
| `PATCH` | `/api/v1/me/addresses/{addressId}/primary` | Bearer válido | Implementado | Definir endereço principal. |

Observação: `GET /api/v1/customers/{id}` está implementado, mas exige `ADMIN` e por isso aparece na seção do Painel, apesar de a URL não conter `/admin`.

### Catálogo — `catalog`

Tabelas: `categorias`, `produtos`, `variacoes_produtos`, `imagens_produtos`.

| Método | Endpoint | Autorização | Estado | Responsabilidade |
|---|---|---|---|---|
| `GET` | `/api/v1/categories` | Público | Planejado | Listar categorias ativas. |
| `GET` | `/api/v1/categories/{categoryId}` | Público | Planejado | Consultar categoria ativa. |
| `GET` | `/api/v1/products` | Público | Planejado | Listar produtos ativos com paginação e filtros. |
| `GET` | `/api/v1/products/{productId}` | Público | Planejado | Consultar produto, variações e imagens. |
| `GET` | `/api/v1/products/slug/{slug}` | Público | Planejado | Consultar produto por slug. |
| `GET` | `/api/v1/products/{productId}/variations` | Público | Planejado | Listar variações ativas/disponíveis. |
| `GET` | `/api/v1/products/{productId}/images` | Público | Planejado | Listar imagens na ordem de exibição. |

Contrato de acessibilidade das imagens: a resposta pública deve expor `altText` derivado de `imagens_produtos.texto_alternativo`. Para imagem informativa, o valor deve ser não vazio; para imagem decorativa, o contrato deve representar explicitamente `alt=""`. `principal` e `ordem_exibicao` organizam a apresentação, mas não substituem o texto alternativo.

### Favoritos — `catalog`

Tabelas: `favoritos`, `usuarios`, `produtos`.

| Método | Endpoint | Autorização | Estado | Responsabilidade |
|---|---|---|---|---|
| `GET` | `/api/v1/me/favorites` | Bearer válido | Planejado | Listar favoritos do cliente. |
| `PUT` | `/api/v1/me/favorites/{productId}` | Bearer válido | Planejado | Adicionar produto aos favoritos. |
| `DELETE` | `/api/v1/me/favorites/{productId}` | Bearer válido | Planejado | Remover produto dos favoritos. |

### Avaliações — `catalog`

Tabelas: `avaliacoes`, `usuarios`, `produtos`, `pedidos`.

| Método | Endpoint | Autorização | Estado | Responsabilidade |
|---|---|---|---|---|
| `GET` | `/api/v1/products/{productId}/reviews` | Público | Planejado | Listar avaliações públicas do produto. |
| `PUT` | `/api/v1/me/reviews/{productId}` | Bearer válido | Planejado | Criar ou atualizar a avaliação própria; exige pedido entregue contendo o produto. |
| `DELETE` | `/api/v1/me/reviews/{productId}` | Bearer válido | Planejado | Remover a avaliação própria. |

Não há endpoint de moderação de avaliações neste contrato.

### Carrinho — `cart`

Tabelas: `carrinhos`, `itens_carrinhos`, `usuarios`, `variacoes_produtos`.

| Método | Endpoint | Autorização | Estado | Responsabilidade |
|---|---|---|---|---|
| `GET` | `/api/v1/me/cart` | Bearer válido | Implementado | Consultar o carrinho atual, com subtotal, disponibilidade e estoque atual. |
| `POST` | `/api/v1/me/cart/items` | Bearer válido | Implementado | Adicionar uma variação ao carrinho, validando produto ativo e quantidade disponível. |
| `PATCH` | `/api/v1/me/cart/items/{itemId}` | Bearer válido | Implementado | Alterar quantidade de um item próprio, validando disponibilidade. |
| `DELETE` | `/api/v1/me/cart/items/{itemId}` | Bearer válido | Implementado | Remover item próprio. |
| `DELETE` | `/api/v1/me/cart` | Bearer válido | Implementado | Esvaziar o carrinho próprio. |

### Estoque — consulta da loja — `inventory`

Tabela principal: `variacoes_produtos`; movimentações são registradas em `movimentacoes_estoque`.

| Método | Endpoint | Autorização | Estado | Responsabilidade |
|---|---|---|---|---|
| `GET` | `/api/v1/products/{productId}/variations/{variationId}/availability` | Público | Planejado | Consultar disponibilidade da variação. |

### Pedidos — `order`

Tabelas: `pedidos`, `itens_pedidos`, `enderecos_pedidos`, `historicos_status_pedidos`.

| Método | Endpoint | Autorização | Estado | Responsabilidade |
|---|---|---|---|---|
| `POST` | `/api/v1/orders` | Bearer válido | Implementado | Criar pedido a partir do carrinho e do endereço próprio informado em `addressId`. Calcula frete, congela itens/endereço, baixa estoque, cria entrega e limpa o carrinho na mesma transação. |
| `GET` | `/api/v1/orders` | Bearer válido | Planejado | Listar pedidos do próprio cliente. |
| `GET` | `/api/v1/orders/{orderId}` | Bearer válido | Planejado | Consultar pedido próprio em detalhe. |
| `POST` | `/api/v1/orders/{orderId}/cancel` | Bearer válido | Planejado | Cancelar pedido quando permitido. |
| `GET` | `/api/v1/orders/{orderId}/status-history` | Bearer válido | Planejado | Consultar histórico de status do pedido próprio. |

O checkout inicia o pedido em `AGUARDANDO_PAGAMENTO`. O frete usa o adaptador determinístico `STANDARD-SIMULATED`, atrás do contrato `ShippingQuoteGateway`, até que uma API de cotação seja integrada. Não há cobrança, gateway financeiro ou webhook de pagamento nesta etapa.

### Pagamento — `payment`

Tabela: `pagamentos`; relacionamento principal: `pagamentos.pedido_id -> pedidos.id`.

| Método | Endpoint | Autorização | Estado | Responsabilidade |
|---|---|---|---|---|
| `POST` | `/api/v1/orders/{orderId}/payments` | Bearer válido + `Idempotency-Key` | Planejado | Iniciar pagamento do pedido. |
| `GET` | `/api/v1/orders/{orderId}/payments` | Bearer válido | Planejado | Listar pagamentos do pedido próprio. |
| `GET` | `/api/v1/payments/{paymentId}` | Bearer válido | Planejado | Consultar pagamento próprio. |
| `POST` | `/api/v1/payments/{paymentId}/webhook` | Assinatura do gateway | Externo / planejado | Receber atualização do provedor e atualizar o pagamento/pedido. |

### Entrega — `shipping`

Tabela: `entregas`; relacionamento principal: `entregas.pedido_id -> pedidos.id`.

| Método | Endpoint | Autorização | Estado | Responsabilidade |
|---|---|---|---|---|
| `GET` | `/api/v1/orders/{orderId}/shipping` | Bearer válido | Planejado | Consultar entrega do pedido próprio. |
| `GET` | `/api/v1/orders/{orderId}/tracking` | Bearer válido | Planejado | Consultar rastreio do pedido próprio. |

## Painel administrativo

Todas as rotas desta seção devem exigir perfil `ADMIN`. A única exceção de caminho é a rota legada `GET /api/v1/customers/{id}`, que já exige `ADMIN`, mas ainda não está sob `/api/v1/admin/**`.

### Usuários — `customer`

Tabelas: `usuarios`, `perfis`, `usuarios_perfis`, `enderecos`.

| Método | Endpoint | Estado | Responsabilidade |
|---|---|---|---|
| `GET` | `/api/v1/admin/users` | Implementado | Listar usuários com paginação, filtros e ordenação. |
| `GET` | `/api/v1/admin/users/{userId}` | Implementado | Consultar dados, endereços e perfis do usuário. |
| `PATCH` | `/api/v1/admin/users/{userId}` | Implementado | Atualizar dados administrativos permitidos. |
| `PATCH` | `/api/v1/admin/users/{userId}/status` | Implementado | Ativar ou desativar usuário. |
| `PUT` | `/api/v1/admin/users/{userId}/profiles/{profile}` | Implementado | Atribuir perfil `CLIENTE` ou `ADMIN`. |
| `DELETE` | `/api/v1/admin/users/{userId}/profiles/{profile}` | Implementado | Remover perfil; não permitir remover o último `ADMIN`. |
| `GET` | `/api/v1/customers/{id}` | Implementado | Consultar resumo de qualquer cliente; exige `ADMIN`. |

`refresh_tokens`, `confirmacoes_email` e `confirmacoes_email_outbox` não possuem endpoints administrativos de CRUD. São tabelas internas de sessão, confirmação e entrega de e-mail.

### Catálogo — `catalog`

Tabelas: `categorias`, `produtos`, `variacoes_produtos`, `imagens_produtos`.

| Método | Endpoint | Estado | Responsabilidade |
|---|---|---|---|
| `GET` | `/api/v1/admin/products` | Implementado | Listar produtos ativos e inativos. |
| `POST` | `/api/v1/admin/products` | Implementado | Criar produto, variações e imagens em operação transacional. |
| `PUT` | `/api/v1/admin/products/{productId}` | Implementado | Substituir dados, variações e imagens do produto. |
| `PATCH` | `/api/v1/admin/products/{productId}/status` | Implementado | Ativar ou desativar produto. |
| `DELETE` | `/api/v1/admin/products/{productId}` | Implementado | Desativar produto. |
| `DELETE` | `/api/v1/admin/products/{productId}/images/{imageId}` | Implementado | Remover uma imagem específica. |
| `POST` | `/api/v1/admin/categories` | Implementado | Criar categoria. |
| `PATCH` | `/api/v1/admin/categories/{categoryId}` | Implementado | Atualizar categoria. |
| `DELETE` | `/api/v1/admin/categories/{categoryId}` | Implementado | Desativar categoria. |

Contrato de produto: `POST` e `PUT` usam `application/json`. O frontend ou uma integração futura envia a URL HTTPS da imagem; a API não recebe `MultipartFile` nem armazena arquivos. A metadata de cada imagem identifica, sem ambiguidade, `altText`, `decorative`, `principal` e `displayOrder`.

### Estoque — `inventory`

Tabelas: `movimentacoes_estoque`, `variacoes_produtos`, `pedidos`, `usuarios`.

| Método | Endpoint | Estado | Responsabilidade |
|---|---|---|---|
| `POST` | `/api/v1/admin/inventory/movements` | Implementado | Registrar entrada ou saída manual (`ENTRADA`/`SAIDA`). |
| `GET` | `/api/v1/admin/inventory/movements` | Implementado | Consultar movimentações de estoque. |
| `GET` | `/api/v1/admin/inventory/variations/{variationId}` | Implementado | Consultar saldo e histórico resumido da variação. |

`VENDA` e `CANCELAMENTO` não têm endpoint administrativo próprio. São efeitos internos do serviço de pedidos e devem ser gravados em `movimentacoes_estoque` na mesma transação da operação de pedido.

### Pedidos — `order`

Tabelas: `pedidos`, `itens_pedidos`, `enderecos_pedidos`, `historicos_status_pedidos`.

| Método | Endpoint | Estado | Responsabilidade |
|---|---|---|---|
| `GET` | `/api/v1/admin/orders` | Implementado | Listar pedidos de qualquer cliente, com filtros por status, cliente e período. |
| `GET` | `/api/v1/admin/orders/{orderId}` | Implementado | Consultar pedido, itens, endereço congelado e histórico de status. |
| `PATCH` | `/api/v1/admin/orders/{orderId}/status` | Implementado | Avançar ou cancelar conforme a máquina de estados, registrando motivo e administrador. |

Regra de cancelamento: `CANCELADO` exige motivo e pode ser registrado administrativamente, mas nesta fase não executa estorno, devolução de estoque ou atualização logística. Esses efeitos dependem das integrações de `payment`, `inventory` e `shipping`.

### Pagamento — `payment`

Tabela: `pagamentos`.

| Método | Endpoint | Estado | Responsabilidade |
|---|---|---|---|
| `POST` | `/api/v1/admin/payments/{paymentId}/refund` | Implementado | Solicitar estorno autorizado com `Idempotency-Key`. |

Contrato de estorno: o administrador envia o motivo e uma chave `Idempotency-Key`; a API registra a solicitação como `SOLICITADO` e retorna `202 Accepted`. A API não marca o pagamento como `ESTORNADO` antes da confirmação do gateway. A tabela `estornos` mantém a auditoria, impede mais de uma solicitação por pagamento e garante replay idempotente.

### Entrega — `shipping`

Tabela: `entregas`.

| Método | Endpoint | Estado | Responsabilidade |
|---|---|---|---|
| `POST` | `/api/v1/admin/orders/{orderId}/shipping` | Implementado | Completar transportadora, rastreio e previsão da entrega criada com o pedido. |
| `PATCH` | `/api/v1/admin/shipments/{shipmentId}/status` | Implementado | Atualizar status logístico conforme a máquina de estados. |

Contrato da entrega: cada pedido possui no máximo uma entrega, criada pelo checkout na Fase 7B com status `AGUARDANDO_ENVIO`. Nesta Fase 7A, o painel apenas completa seus dados e altera o status. As transições permitidas são `AGUARDANDO_ENVIO -> ENVIADO`, `ENVIADO -> EM_TRANSITO/DEVOLVIDO` e `EM_TRANSITO -> ENTREGUE/DEVOLVIDO`; `ENTREGUE` e `DEVOLVIDO` são terminais. Marcar `DEVOLVIDO` não executa estorno, devolução de estoque ou notificação.

## Regras transversais

- Rotas sob `/api/v1/admin/**` exigem perfil `ADMIN`.
- Rotas da loja que acessam dados próprios (`/me`, `/cart`, `/orders`, pagamentos e entrega) exigem Bearer token válido.
- Rotas públicas de autenticação são somente as explicitamente liberadas pela configuração de segurança: registro, login, refresh, reenvio e confirmação de e-mail.
- Listagens devem definir paginação, ordenação e filtros no contrato; não depender de retorno ilimitado.
- Alterações de status de pedido e movimentações de estoque devem gerar histórico append-only.
- Pagamentos devem aceitar `Idempotency-Key` para impedir cobranças duplicadas.
- Webhooks devem validar a assinatura do gateway antes de alterar pagamento ou pedido.
- O banco não substitui requisitos de acessibilidade do frontend: foco, teclado, contraste, rótulos de campos, semântica e mensagens de erro continuam sendo responsabilidade da interface.

## Estado atual da implementação

No código atual, as rotas marcadas como `Implementado` nas tabelas possuem controllers e casos de uso funcionais. A implementação recente também cobre:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/confirm/resend`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/confirm`
- `GET /api/v1/auth/confirm`
- `GET /api/v1/me`
- `GET /api/v1/customers/{id}`
- `GET /api/v1/me/cart`
- `POST /api/v1/me/cart/items`
- `PATCH /api/v1/me/cart/items/{itemId}`
- `DELETE /api/v1/me/cart/items/{itemId}`
- `DELETE /api/v1/me/cart`
- `POST /api/v1/orders`
- `POST /api/v1/admin/orders/{orderId}/shipping`
- `PATCH /api/v1/admin/shipments/{shipmentId}/status`

As demais rotas continuam planejadas conforme o estado indicado em cada tabela. O checkout não inclui cobrança real: pagamento, confirmação financeira, webhooks e integrações externas permanecem nas etapas futuras.
