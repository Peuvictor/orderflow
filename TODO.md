# TODO — Roadmap do OrderFlow

> **Atualizado em 22/10/2026.** As caixas marcadas representam funcionalidades implementadas e exercitadas no ambiente local durante o desenvolvimento; **nem todas estão sincronizadas na branch `main` do GitHub**. As tarefas abertas incluem validações e commits de código ainda pendentes.
>
> **Método:** cada funcionalidade nova segue RED → GREEN → REFACTOR, com testes passando antes do commit.

## 0. Base e módulo de produtos — implementado

- [x] Inicializar projeto com Java 25, Spring Boot 4 e Gradle Groovy.
- [x] Subir PostgreSQL 17 com Docker Compose, na porta local `5433`.
- [x] Configurar Flyway V1 para `products` e `inventory`.
- [x] Modelar `Product` com JPA e Lombok seletivo.
- [x] Testar criação, validações, normalização de SKU e desativação no domínio.
- [x] Criar `ProductRepository` (`save`, `findBySku`, `existsBySku`).
- [x] Testar persistência e SKU único com PostgreSQL/Testcontainers.
- [x] Criar `ProductService` e exceções `DuplicateSkuException` / `ProductNotFoundException`.
- [x] Testar no Service: cadastro válido, SKU duplicado, consulta, produto inexistente e SKU vazio.
- [x] Criar DTOs `CreateProductRequest`, `ProductResponse` e `ApiErrorResponse`.
- [x] Implementar `POST /products` (HTTP 201) e `GET /products/{sku}` (HTTP 200).
- [x] Adicionar Bean Validation ao cadastro: SKU/nome obrigatórios, preço não nulo, positivo e até duas casas decimais.
- [x] Tratar `DuplicateSkuException` como HTTP 409 e `ProductNotFoundException` como HTTP 404.
- [x] Escrever testes HTTP com MockMvc para cenários de sucesso e erro.
- [x] Adaptar `OrderflowApplicationTests.contextLoads()` para Testcontainers.

## 1. Consolidar a API de produtos — prioridade imediata

- [ ] **Sincronizar com o GitHub as alterações locais posteriores ao último commit remoto:** endpoint GET, validações de preço, tratamento de HTTP 404 e 400, refactor do `GlobalExceptionHandler` e respectivos testes, conforme os arquivos efetivamente presentes no WSL2.
- [ ] Garantir que o histórico local incorpore o commit desta documentação (`git pull --rebase origin main`) antes de enviar alterações de código, sem usar `--force`.
- [ ] **Verificar RED → GREEN** do teste `shouldReturnStandardErrorResponseWhenSkuIsBlank`: HTTP 400 com `{"message":"SKU cannot be empty"}`.
- [ ] Confirmar o refactor de `GlobalExceptionHandler` e executar `ProductControllerTest` após a alteração.
- [ ] Executar a suíte completa com Docker ativo: `./gradlew test --no-daemon --no-watch-fs --console=plain`.
- [ ] Criar/validar `ProductApiIntegrationTest`: `POST /products` → PostgreSQL → `GET /products/{sku}` sem mocks.
- [ ] Adicionar testes HTTP com banco real para SKU duplicado, validação e produto não encontrado.
- [ ] Garantir que uma violação de unicidade causada por corrida concorrente também resulte em resposta HTTP adequada (409), sem expor detalhes internos do banco.
- [ ] Padronizar erros de entrada adicionais (JSON malformado, campo de tipo incorreto e SKU inválido na consulta) como HTTP 400.
- [ ] Definir política de erro para múltiplos campos inválidos: mensagem única determinística ou coleção estruturada de erros.
- [ ] Revisar limites de tamanho de SKU/nome e proteção da precisão monetária, coerentes com a migration SQL.
- [ ] Implementar consulta por ID/listagem paginada de produtos, caso necessária para o dashboard.
- [ ] Implementar atualização de nome/preço e ativação/desativação pela API, com testes.
- [ ] Adicionar documentação dos endpoints (ex.: OpenAPI), se fizer parte do escopo final.

## 2. Estoque e movimentações

- [ ] Mapear entidade `Inventory` e criar `InventoryRepository` conforme esquema Flyway.
- [ ] Implementar consulta de `onHand`, `reserved` e `available = onHand - reserved`.
- [ ] Criar testes para quantidades inválidas e proibição de estoque físico negativo.
- [ ] Implementar entradas/ajustes de estoque com transações.
- [ ] Modelar histórico de movimentos (`StockMovement`) com nova migration Flyway.
- [ ] Definir estratégia de locking/concorrência e testar disputas simultâneas.
- [ ] Garantir ordenação consistente do acesso a produtos por ID quando houver bloqueios múltiplos.
- [ ] Disponibilizar endpoints mínimos de consulta e movimentação de estoque.

## 3. Pedidos e reserva temporária

- [ ] Criar migration de `orders`, `order_items` e `stock_reservations`.
- [ ] Modelar pedidos e estados: `RESERVED`, `CONFIRMED`, `SHIPPED`, `CANCELLED`, `EXPIRED`.
- [ ] Utilizar UUID para identificar pedidos e `BigDecimal` para valores.
- [ ] Permitir pedidos com múltiplos itens e quantidades positivas.
- [ ] Rejeitar o pedido inteiro quando qualquer item não tiver estoque suficiente.
- [ ] Rejeitar produtos inexistentes ou inativos em novos pedidos.
- [ ] Preservar preço unitário no item no momento da criação do pedido.
- [ ] Reservar estoque ao criar o pedido (prazo de 10 minutos).
- [ ] Manter o estoque físico inalterado ao reservar; reduzir apenas o disponível.
- [ ] Implementar `POST /orders/{id}/confirm`, permitido somente antes do vencimento.
- [ ] Permitir cancelamento somente em `RESERVED` válido; liberar reserva.
- [ ] Expirar reservas automaticamente e liberar estoque de forma idempotente.
- [ ] Permitir expedição apenas após confirmação; reduzir o estoque físico uma única vez.
- [ ] Testar todas as transições válidas e inválidas de estado.

## 4. Integridade, concorrência e idempotência

- [ ] Tornar atômicas a criação de pedidos e as reservas de todos os seus itens.
- [ ] Implementar locks transacionais para reserva/expedição e prevenir overselling.
- [ ] Testar concorrência sob carga com PostgreSQL real.
- [ ] Definir e aplicar chave de idempotência para criação de pedido.
- [ ] Rejeitar requisição duplicada com HTTP 409, sem gerar segundo pedido.
- [ ] Garantir que confirmação, cancelamento, expiração e expedição não gerem movimentos duplicados.
- [ ] Cobrir rollback em falhas intermediárias e timeout de transação.

## 5. Mensageria e observabilidade

- [ ] Integrar RabbitMQ via Docker Compose.
- [ ] Definir eventos de negócio (pedido criado, confirmado, expedido, cancelado e expirado).
- [ ] Garantir publicação confiável de eventos após persistência (avaliar transactional outbox).
- [ ] Implementar consumidores idempotentes e tratamento de retry/falhas.
- [ ] Adicionar logs estruturados, métricas e verificações de saúde.

## 6. Frontend e entrega

- [ ] Construir dashboard com React + TypeScript.
- [ ] Implementar telas de produtos, estoque, pedidos e eventos.
- [ ] Integrar tratamento de respostas HTTP e validações no frontend.
- [ ] Criar fluxo de execução local de ponta a ponta com Docker Compose.
- [ ] Adicionar CI no GitHub Actions para build e testes automatizados.
- [ ] Revisar documentação, exemplos de uso, variáveis de ambiente e segurança antes de publicar.

## Rotina de desenvolvimento

```bash
# 1) Executar testes da camada alterada
./gradlew test --tests 'com.orderflow.controller.ProductControllerTest' --no-daemon --no-watch-fs --console=plain

# 2) Verificar a suíte completa (Docker ativo para Testcontainers)
./gradlew test --no-daemon --no-watch-fs --console=plain

# 3) Revisar antes de versionar
git status
git diff --check

# 4) Commit / push após testes verdes e revisão
git add README.md TODO.md
git commit -m "docs: atualiza README e roadmap do OrderFlow"
git push origin main
```

> Para incluir alterações de código no mesmo commit, revise-as e adicione explicitamente os arquivos correspondentes antes do `git commit`.