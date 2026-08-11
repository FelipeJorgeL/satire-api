# Instruções gerais do projeto

## Contexto do projeto

- O projeto é uma API de e-commerce modular monolith chamada Satire API.
- Stack principal: Java 17, Spring Boot, Spring Modulith, JPA, Flyway, PostgreSQL, Bean Validation e Spring Security.
- O contrato completo de endpoints está em `docs/ENDPOINTS.md`; as fases gerais estão em `docs/IMPLEMENTATION_PHASES.md`.
- Os módulos de domínio são `customer`, `catalog`, `inventory`, `cart`, `order`, `payment` e `shipping`.
- Cada módulo segue `domain/<modulo>/`, com contratos públicos na raiz e implementação interna em `internal/`.
- `infra/security` e `infra/web` são infraestrutura compartilhada, não módulos de domínio.
- O contrato público entre módulos deve usar gateways e DTOs; entidades JPA e detalhes de persistência não devem atravessar o limite de `internal/`.
- Rotas da loja usam `/api/v1/**`; rotas do painel usam `/api/v1/admin/**` e exigem o perfil `ADMIN`.
- Rotas do cliente autenticado exigem Bearer token válido. Rotas públicas devem ser liberadas explicitamente na configuração de segurança.
- Listagens devem definir paginação, filtros e ordenação. Mudanças de status e movimentações de estoque devem preservar histórico append-only.
- Pagamentos devem usar `Idempotency-Key`; webhooks devem validar a assinatura do provedor antes de alterar dados.
- A branch planejada para o trabalho do painel é `feature/admin-panel`.

## Organização funcional dos módulos

| Módulo | Responsabilidades |
|---|---|
| `customer` | Cadastro, autenticação, sessão, conta, endereços, perfis e administração de usuários. |
| `catalog` | Categorias, produtos, variações, imagens, catálogo público, favoritos e avaliações. |
| `inventory` | Disponibilidade, saldo e movimentações de estoque. |
| `cart` | Carrinho do cliente e seus itens. |
| `order` | Checkout, pedidos, cancelamento, status e histórico. |
| `payment` | Pagamentos, idempotência, webhooks e estornos administrativos. |
| `shipping` | Entrega, envio, rastreamento e status logístico. |

## Regras gerais de trabalho

- Antes de editar código, verificar branch, estado do worktree e a referência que contém o código-fonte atual.
- Preservar `.env`, credenciais, arquivos locais e alterações não relacionadas.
- Não alterar dependências, lockfiles, migrations, configuração, infraestrutura ou contratos públicos sem necessidade comprovada e aprovação explícita.
- Não criar branch, commit, push ou pull request sem autorização explícita para essa ação.
- Implementar somente o módulo e a etapa confirmados; não antecipar endpoints de fases posteriores.
- Reutilizar classes e contratos existentes quando compatível; evitar refatorações amplas durante uma etapa funcional.

## Etapa 7A — `shipping`: entrega administrativa

- O módulo usa `internal/model/`, `internal/persistence/`, `internal/usecase/details/`, `internal/usecase/status/`, `internal/dto/`, `internal/mapper/` e `internal/web/`.
- O contrato público `ShipmentCreationGateway` permite que o checkout do módulo `order` crie a entrega sem importar entidades ou repositórios internos de `shipping`.
- `ensureForOrder` é idempotente e cria uma única entrega por pedido, inicialmente em `AGUARDANDO_ENVIO`. O checkout da Fase 7B invoca o contrato na mesma transação da criação do pedido.
- `POST /api/v1/admin/orders/{orderId}/shipping` apenas completa os dados da entrega já criada: transportadora, código de rastreio e previsão.
- `PATCH /api/v1/admin/shipments/{shipmentId}/status` usa lock pessimista e aceita somente transições válidas. As datas `enviado_em` e `entregue_em` são preenchidas pelo domínio, nunca pelo cliente.
- Entregas `ENTREGUE` e `DEVOLVIDO` são terminais. `DEVOLVIDO` não dispara estorno, devolução de estoque, alteração de pedido ou notificação nesta etapa.
- Não foi criada migration: a tabela `entregas` já existe em `V1__create_ecommerce_schema.sql` e sua restrição `UNIQUE (pedido_id)` protege contra duplicidade.

### Fora do escopo da Etapa 7A

- Criação do pedido pelo cliente, carrinho e checkout; esses itens pertencem à Fase 7B.
- Rastreamento público, integração com transportadora, webhook, notificação e histórico logístico append-only.
- Estorno, devolução automática de estoque, alteração de status do pedido e integração de pagamento.

### Validação mínima

- Confirmar `401` sem token, `403` para `CLIENTE` e acesso para `ADMIN` nos dois endpoints.
- Validar entrega inexistente, rastreio duplicado, corpo vazio, transições inválidas, estados terminais e preenchimento automático das datas.
- Validar lock pessimista, contrato público sem referências a `shipping.internal` e teste de arquitetura Modulith.
- Executar `mvn verify` antes de solicitar autorização para a Fase 7B.

## Etapa 7B — `cart` e `order`: carrinho e checkout

- O módulo `cart` mantém um carrinho por cliente, usa lock pessimista nas alterações e expõe apenas snapshots e gateways públicos para o checkout.
- Endpoints implementados: `GET /api/v1/me/cart`, `POST /api/v1/me/cart/items`, `PATCH /api/v1/me/cart/items/{itemId}`, `DELETE /api/v1/me/cart/items/{itemId}` e `DELETE /api/v1/me/cart`.
- O endpoint `POST /api/v1/orders` exige endereço pertencente ao cliente, rejeita carrinho vazio, produto inativo, variação inexistente e estoque insuficiente.
- O checkout calcula subtotal com o preço atual, consulta o `ShippingQuoteGateway`, cria o pedido em `AGUARDANDO_PAGAMENTO`, grava snapshots imutáveis dos itens e endereço, registra o histórico inicial, baixa o estoque com movimento `VENDA`, cria automaticamente a entrega em `AGUARDANDO_ENVIO` com previsão e limpa o carrinho somente após concluir a operação.
- A sequência é transacional; falhas durante estoque, persistência ou entrega desfazem o pedido parcial e a baixa de estoque. O número do pedido é gerado no servidor e o cliente nunca informa `customerId`, preço, total, estoque ou status.
- O frete atual é uma implementação determinística e simulada atrás de `ShippingQuoteGateway`, preparada para futura substituição por API de cotação. O contrato recebe CEP, subtotal e quantidade de itens e retorna serviço, valor e data estimada.
- Não foi criada migration nem dependência: o checkout reutiliza as tabelas existentes em `V1__create_ecommerce_schema.sql`, incluindo `carrinhos`, `itens_carrinhos`, `pedidos`, snapshots, `movimentacoes_estoque` e `entregas`.

### Classes e contratos principais da Etapa 7B

- Contratos públicos: `CartCheckoutGateway`, `CartCheckoutSnapshot`, `ProductVariationPurchaseGateway`, `CustomerAddressGateway`, `StockSaleGateway`, `ShippingQuoteGateway` e `ShipmentCreationGateway`.
- `cart/internal`: entidades `Cart` e `CartItem`, repositórios JPA, mapper, DTOs, casos de uso do carrinho e controller do cliente.
- `order/internal/usecase/creation`: `CreateOrderUseCase` e `OrderNumberGenerator`, além dos DTOs, snapshots, repositórios e exceções do checkout.
- `shipping/internal/quote`: `DeterministicShippingQuoteGateway`, sem expor implementação interna a outros módulos.
- `inventory/internal/usecase/sale`: `JpaStockSaleGateway`, responsável pela baixa concorrente e pelo movimento `VENDA`.

### Fora do escopo da Etapa 7B

- Cobrança real, gateway financeiro, captura, confirmação, cancelamento financeiro ou webhook de pagamento.
- Integração real com transportadora, rastreamento público, notificações e webhook logístico.
- Consulta pública de pedidos, cancelamento pelo cliente e efeitos de cancelamento ou estorno.

### Validação mínima

- Confirmar `401` sem token, `403` para usuário não autorizado quando aplicável e acesso isolado por cliente.
- Validar quantidade inválida, item próprio, estoque insuficiente, produto inativo, endereço de outro cliente, carrinho vazio e falha de criação idempotente da entrega.
- Validar snapshot de preço/endereço, movimento `VENDA`, limpeza do carrinho somente após sucesso, rollback transacional e criação de uma única entrega.
- Pesquisar referências a `.internal.` entre módulos, executar o teste de arquitetura Modulith e concluir com `mvn verify`.

## Encapsulamento Spring Modulith

- Nunca importar, em outro módulo ou em `infra`, classes de `domain/<modulo>/internal/`.
- Tipos `internal` pertencem exclusivamente ao módulo de origem; referências cruzadas podem gerar `Invalid reference to non-exposed type of module`.
- Dependências entre módulos devem usar contratos públicos na raiz de `domain/<modulo>/`, preferencialmente gateways e DTOs imutáveis.
- Adapters JPA, entidades, repositórios e casos de uso internos devem permanecer atrás desses contratos.
- Antes de concluir uma etapa, pesquisar referências cruzadas a `.internal.` e executar o teste de arquitetura Modulith.

## Documentação futura do projeto

- Futuramente deverá ser criado um arquivo explicando o projeto classe por classe.
- Essa documentação deverá explicar a estrutura das pastas, convenções de nomes, responsabilidades das classes, fluxo do código, arquitetura modular, contratos públicos e uso de `internal`.
- Essa documentação não faz parte da etapa atual e não deve ser criada antecipadamente.

## Regra obrigatória antes de cada etapa

Antes de iniciar qualquer etapa, confirmar explicitamente:

1. o escopo exato da etapa;
2. os endpoints que serão criados ou alterados;
3. as classes e arquivos que serão criados ou alterados;
4. o que ficará fora do escopo;
5. os testes e comandos de validação.

Não iniciar a implementação sem essa confirmação explícita. A existência de um plano ou de uma etapa documentada não equivale à aprovação do escopo.

## Etapa 1 — `customer`: administração de usuários

### Endpoints incluídos

Todos os endpoints abaixo exigem `ROLE_ADMIN`:

| Método | Endpoint | Objetivo |
|---|---|---|
| `GET` | `/api/v1/admin/users` | Listar usuários com paginação, filtros e ordenação. |
| `GET` | `/api/v1/admin/users/{userId}` | Consultar dados, endereços e perfis do usuário. |
| `PATCH` | `/api/v1/admin/users/{userId}` | Atualizar somente dados administrativos permitidos. |
| `PATCH` | `/api/v1/admin/users/{userId}/status` | Ativar ou desativar usuário. |
| `PUT` | `/api/v1/admin/users/{userId}/profiles/{profile}` | Atribuir o perfil `CLIENTE` ou `ADMIN`. |
| `DELETE` | `/api/v1/admin/users/{userId}/profiles/{profile}` | Remover perfil sem permitir a remoção do último `ADMIN`. |

O endpoint `GET /api/v1/customers/{id}` permanece preservado como rota legada já implementada. Não criar uma segunda implementação para ele nesta etapa.

### Classes e arquivos previstos

Os nomes abaixo são o contrato de implementação desta etapa e devem ser confirmados novamente antes do código:

#### Web e segurança

- `domain/customer/internal/web/AdminCustomerController.java` — expõe os seis endpoints administrativos.
- `infra/security/SecurityConfig.java` — ajustar a autorização de `/api/v1/admin/**`, se necessário; não substituir a configuração JWT existente.

#### Casos de uso

- `ListAdminCustomersUseCase.java`
- `GetAdminCustomerUseCase.java`
- `UpdateAdminCustomerUseCase.java`
- `ChangeAdminCustomerStatusUseCase.java`
- `AssignCustomerProfileUseCase.java`
- `RemoveCustomerProfileUseCase.java`

Localização: `domain/customer/internal/usecase/`.

#### DTOs HTTP

- `AdminCustomerFilter.java` — filtros, paginação e ordenação da listagem.
- `UpdateAdminCustomerRequest.java`
- `UpdateAdminCustomerStatusRequest.java`
- `AdminCustomerListItemResponse.java`
- `AdminCustomerDetailsResponse.java`
- `AdminCustomerAddressResponse.java`

Localização: `domain/customer/internal/dto/request/` e `domain/customer/internal/dto/response/`.

#### Portas, persistência e modelo

- `AdminCustomerQuery.java` — porta para consulta administrativa paginada e detalhada.
- `JpaAdminCustomerQuery.java` — adapter JPA da porta administrativa.
- `Address.java` — entidade de `enderecos`, caso ainda não exista na referência escolhida.
- `AddressRepository.java` — persistência dos endereços usados no detalhe administrativo.
- `AdminCustomerNotFoundException.java`
- `LastAdminRemovalNotAllowedException.java`

As portas devem permanecer no pacote `internal/usecase/`; os adapters JPA devem permanecer em `internal/persistence/`. Reutilizar `Customer`, `Profile`, `CustomerRepository`, `ProfileRepository` e `CustomerGateway` quando o contrato atual permitir, sem expor entidades internas para outros módulos.

#### Testes

- `AdminCustomerUseCaseTest.java`
- `AdminCustomerSecurityWebTest.java`
- teste de persistência/integração administrativa, se o acesso paginado e os endereços exigirem validação real com PostgreSQL.

Localização: `src/test/java/br/com/api/satireapi/domain/customer/internal/`.

## Etapa 2 — `customer`: conta e endereços do cliente

- Casos de uso devem ser organizados em `internal/usecase/account/` e `internal/usecase/address/`.
- A persistência continua separada em `internal/persistence/`; criar subdiretório `address/` somente quando houver volume suficiente de adapters de endereço.
- Os endpoints da conta usam exclusivamente o cliente autenticado pelo Bearer token; nunca aceitar `customerId` vindo do cliente.
- Operações de endereço devem consultar por `addressId` e `customerId` juntos, preservando isolamento entre clientes.
- Deve existir no máximo um endereço principal por cliente; criação, exclusão e troca do principal devem ser transacionais e compatíveis com o índice único do banco.

### Fora do escopo da Etapa 1

- `POST`, `GET`, `PATCH` e `DELETE` de `/api/v1/me`.
- CRUD de endereços do próprio cliente.
- Registro, login, refresh, logout e confirmação de e-mail.
- Catálogo, estoque, carrinho, pedidos, pagamentos e entrega.
- CRUD administrativo de produtos e categorias.
- Migrações, dependências, armazenamento de imagens e alterações em `.env`, salvo se uma lacuna concreta for descoberta e aprovada antes.

### Validação mínima

- Confirmar `401` sem token, `403` para usuário sem `ADMIN` e sucesso para administrador.
- Validar paginação, filtros, ordenação e ausência de exposição de senha/hash.
- Validar atualização de status e perfis.
- Impedir a remoção do último administrador, inclusive em concorrência quando aplicável.
- Executar testes unitários, testes web, teste de arquitetura Modulith e `mvn verify` após a implementação autorizada.

## Etapa 3 — `catalog`: catálogo administrativo

- Os casos de uso devem ser organizados em `domain/catalog/internal/usecase/category/` e `domain/catalog/internal/usecase/product/`.
- As entidades e adapters de persistência permanecem em `internal/model/` e `internal/persistence/`; a persistência continua sem subdiretórios enquanto o volume não justificar separação adicional.
- Os endpoints administrativos de produtos e categorias exigem `ROLE_ADMIN`, com validação de entrada e sem exposição de entidades internas.
- Produtos são criados ou substituídos com variações e imagens em operação transacional; alterações de filhos usam a escrita do agregado para evitar estado parcial.
- Imagens são recebidas somente como URLs HTTPS. A API não recebe `multipart/form-data`, `MultipartFile` nem executa upload ou armazenamento externo.
- O schema atual não possui coluna `decorative` em `imagens_produtos`; portanto, `decorative=true` é persistido como `altText` nulo, enquanto imagens informativas exigem `altText` não vazio. O campo continua explícito no contrato JSON.
- A API rejeita URLs não HTTPS, SKUs duplicados, ordens de imagem duplicadas, múltiplas imagens principais e metadata de acessibilidade inconsistente.

### Fora do escopo da Etapa 3

- Upload, armazenamento e integração com provedor de imagens.
- Catálogo público, estoque, carrinho, pedidos, pagamentos, entrega, avaliações e favoritos.
- Migrações de banco, dependências e alterações em `.env`, salvo lacuna concreta aprovada antes.

### Validação mínima

- Confirmar `401` sem token, `403` para `CLIENTE` e sucesso para `ADMIN`.
- Validar criação, listagem filtrada, substituição, ativação/desativação e exclusão de imagem.
- Validar criação, atualização e desativação de categorias.
- Executar testes unitários, testes web, teste de arquitetura Modulith e `mvn verify` após a implementação autorizada.

## Etapa 4 — `inventory`: estoque administrativo

- O módulo usa `internal/model/`, `internal/persistence/`, `internal/usecase/movement/`, `internal/usecase/variation/`, `internal/dto/`, `internal/mapper/` e `internal/web/`.
- O `inventory` não importa entidades internas do `catalog`. O ajuste de saldo usa o contrato público `ProductVariationStockGateway`, mantendo JPA e lock pessimista dentro do `catalog`.
- `POST /api/v1/admin/inventory/movements` aceita somente `ENTRADA` e `SAIDA`, atualiza a variação e grava a auditoria na mesma transação.
- Movimentações manuais são append-only e registram usuário autenticado, saldo anterior, saldo novo, quantidade, tipo, observação e data.
- Saídas maiores que o saldo, quantidades inválidas, variação inexistente e períodos de consulta invertidos devem ser rejeitados.
- `VENDA` e `CANCELAMENTO` permanecem reservados para a implementação do módulo `order`.

### Fora do escopo da Etapa 4

- Baixa automática por venda, devolução por cancelamento e checkout.
- Alterações em pedidos, pagamentos, carrinho, catálogo público e migrations.
- Dependências, lockfiles, `.env` e integração externa.

### Validação mínima

- Confirmar `401` sem token, `403` para `CLIENTE` e sucesso para `ADMIN`.
- Validar `ENTRADA`, `SAIDA`, estoque insuficiente, quantidade inválida e auditoria append-only.
- Validar filtros por variação, tipo e período, paginação e ordenação.
- Executar teste de arquitetura Modulith e `mvn verify` após a implementação autorizada.

## Etapa 5 — `order`: pedidos administrativos

- O módulo usa `internal/model/`, `internal/persistence/`, `internal/usecase/query/`, `internal/usecase/status/`, `internal/dto/`, `internal/mapper/` e `internal/web/`.
- O `order` é responsável pelo pedido, itens, endereço congelado e histórico de status. Não importa entidades internas de `payment`, `shipping`, `inventory` ou `catalog`.
- A listagem administrativa deve ser paginada e aceitar filtros por status, cliente e período, com ordenação limitada a campos conhecidos.
- A consulta detalhada não expõe entidades JPA; retorna DTOs de pedido, itens, endereço congelado e histórico append-only.
- A máquina de estados permite `AGUARDANDO_PAGAMENTO -> PAGO/CANCELADO`, `PAGO -> EM_SEPARACAO/CANCELADO`, `EM_SEPARACAO -> ENVIADO/CANCELADO` e `ENVIADO -> ENTREGUE`. Estados `ENTREGUE` e `CANCELADO` são terminais.
- `CANCELADO` exige motivo. Nesta etapa, o cancelamento altera o status e grava o histórico, mas não executa estorno, devolução de estoque nem atualização logística; esses efeitos ficam para as integrações de `payment`, `inventory` e `shipping`.
- Alterações de status usam lock pessimista no pedido e gravam a mudança e o histórico na mesma transação.

### Fora do escopo da Etapa 5

- Criação de pedidos pelo cliente, checkout e carrinho.
- Estorno, captura ou qualquer operação de pagamento.
- Baixa ou devolução automática de estoque.
- Registro de envio, rastreamento e atualização logística.
- Migrations, dependências, lockfiles, `.env` e integrações externas.

### Validação mínima

- Confirmar `401` sem token, `403` para `CLIENTE` e sucesso para `ADMIN`.
- Validar paginação, filtros, ordenação e consulta detalhada sem exposição de entidades internas.
- Validar transições permitidas, estados terminais, motivo obrigatório para `CANCELADO` e histórico append-only.
- Executar testes unitários, testes web, teste de arquitetura Modulith e `mvn verify` após a implementação autorizada.

## Etapa 6 — `payment`: solicitação administrativa de estorno

- O endpoint `POST /api/v1/admin/payments/{paymentId}/refund` exige `ROLE_ADMIN`, motivo e header `Idempotency-Key` com formato controlado.
- O estorno só pode ser solicitado para pagamento `APROVADO`; o usuário administrativo é obtido do Bearer token, nunca do corpo da requisição.
- A operação usa lock pessimista no pagamento e cria uma solicitação persistente em `estornos` como `SOLICITADO`, retornando `202 Accepted`.
- A mesma chave para o mesmo pagamento retorna a solicitação existente; a mesma chave para outro pagamento e uma segunda solicitação para o mesmo pagamento são conflitos.
- O schema exige a migration `V4__create_refunds_schema.sql`, com auditoria, valor copiado do pagamento, motivo, usuário, status e chaves únicas.
- A API não simula confirmação financeira e não marca `pagamentos.status` como `ESTORNADO` nesta etapa. A conclusão depende do gateway e de uma confirmação futura.

### Fora do escopo da Etapa 6

- Integração real com gateway, captura, estorno financeiro efetivo e webhook de confirmação.
- Alteração de pedido, estoque ou entrega.
- Retry de solicitação recusada e múltiplos estornos parciais.
- Dependências, lockfiles, `.env` e alterações não relacionadas.

### Validação mínima

- Confirmar `401` sem token, `403` para `CLIENTE` e `202` para `ADMIN` autorizado.
- Validar pagamento inexistente, status não aprovado, motivo ausente e `Idempotency-Key` ausente ou inválida.
- Validar replay idempotente, conflito de chave e bloqueio de segunda solicitação para o mesmo pagamento.
- Executar testes unitários, testes web, teste de arquitetura Modulith e `mvn verify` após a implementação autorizada.
