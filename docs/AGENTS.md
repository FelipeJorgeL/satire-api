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
