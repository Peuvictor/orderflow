# OrderFlow — API de gerenciamento de pedidos e estoque

O **OrderFlow** é uma API REST desenvolvida em **Java 25 com Spring Boot** para gerenciar produtos e, nas próximas etapas, pedidos, reservas temporárias e movimentações de estoque.

É um projeto de estudo e portfólio com foco em **TDD (Test-Driven Development)**, arquitetura em camadas, integridade dos dados, persistência relacional, consistência transacional, concorrência, idempotência e, futuramente, eventos assíncronos.

> **Status (22/10/2026): em desenvolvimento.** A documentação acompanha as etapas implementadas no ambiente local de desenvolvimento. **A branch `main` do GitHub ainda pode estar atrás do código local**: na última conferência, os endpoints de consulta (`GET`) e alguns tratamentos/validações HTTP ainda não constavam na versão remota. Consulte [Sincronização com o GitHub](#sincronização-com-o-github).

## Tecnologias

### Em uso

- **Java 25** e **Spring Boot 4**
- **Gradle**, com Groovy DSL e Gradle Wrapper
- **Spring Web MVC** — endpoints REST e MockMvc
- **Spring Data JPA / Hibernate** — persistência
- **Jakarta Bean Validation** — validação de requisições
- **PostgreSQL 17** — banco relacional
- **Flyway** — versionamento do esquema SQL
- **Lombok** — redução seletiva de código repetitivo
- **JUnit 5 e Mockito** — testes de domínio, serviço e camada HTTP
- **Testcontainers** — testes com PostgreSQL real e isolado
- **Docker / Docker Compose** — ambiente local

### Planejadas

- **RabbitMQ** — publicação e consumo de eventos de negócio
- **React + TypeScript** — dashboard para produtos, pedidos e estoque

## Arquitetura

O sistema segue a separação de responsabilidades:

```text
Cliente HTTP
     │
     ▼
Controller ──► DTOs de requisição/resposta
     │
     ▼
  Service ───► Regras de negócio e exceções
     │
     ▼
 Repository ──► Spring Data JPA / Hibernate
     │
     ▼
 PostgreSQL ◄── Flyway (migrations)
```

- **Controller:** define os contratos e status HTTP, delegando as operações ao serviço.
- **DTOs:** separam o contrato público da API das entidades persistidas.
- **Service:** aplica regras de negócio, como normalização e verificação de SKU.
- **Repository:** consulta e persiste entidades.
- **Model:** encapsula a entidade `Product`, suas validações e comportamentos.
- **GlobalExceptionHandler:** converte exceções da aplicação em respostas HTTP.

### Principais arquivos

```text
src/
├── main/
│   ├── java/com/orderflow/
│   │   ├── controller/
│   │   │   ├── ProductController.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── dto/
│   │   │   ├── CreateProductRequest.java
│   │   │   ├── ProductResponse.java
│   │   │   └── ApiErrorResponse.java
│   │   ├── model/
│   │   │   └── Product.java
│   │   ├── repository/
│   │   │   └── ProductRepository.java
│   │   └── service/
│   │       ├── ProductService.java
│   │       ├── DuplicateSkuException.java
│   │       └── ProductNotFoundException.java
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           └── V1__create_products_and_inventory.sql
└── test/
    └── java/com/orderflow/
        ├── model/ProductTest.java
        ├── repository/ProductRepositoryTest.java
        ├── service/ProductServiceTest.java
        ├── controller/ProductControllerTest.java
        └── OrderflowApplicationTests.java
```

> Um teste adicional, `ProductApiIntegrationTest`, foi planejado para verificar o fluxo HTTP completo com PostgreSQL. Confira o arquivo e a execução na sua cópia do projeto antes de marcá-lo como concluído.

## Módulo de produtos

### Regras implementadas

- Produto com **SKU, nome, preço e estado ativo/inativo**.
- SKU obrigatório, com normalização de espaços externos e letras maiúsculas.
- Nome obrigatório.
- Preço obrigatório, positivo e com precisão monetária de até duas casas decimais; na API, o DTO também limita o número de dígitos inteiros.
- Produto criado inicialmente como **ativo**; operação de desativação disponível no domínio.
- SKU único: verificação no `ProductService` e restrição no PostgreSQL.
- Consulta por SKU normalizado; exceção específica quando não encontrado.
- Entidade mapeada com JPA, sem expô-la diretamente no JSON da API.

### Endpoints REST desenvolvidos localmente

| Método | Rota | Comportamento | Status |
|---|---|---|---|
| `POST` | `/products` | Cadastra produto válido | `201 Created` |
| `POST` | `/products` | Rejeita campos obrigatórios ou preço inválido | `400 Bad Request` |
| `POST` | `/products` | Rejeita SKU já cadastrado | `409 Conflict` |
| `GET` | `/products/{sku}` | Consulta um produto | `200 OK` |
| `GET` | `/products/{sku}` | Produto inexistente | `404 Not Found` |

A consulta usa o SKU informado na URL, normalizado pelo serviço. Os testes HTTP utilizam **MockMvc** para verificar esses contratos. **A implementação completa ainda precisa ser sincronizada e verificada na branch remota `main`.**

### Exemplo: cadastrar produto

```http
POST /products
Content-Type: application/json
```

```json
{
  "sku": " sku-001 ",
  "name": "Teclado Mecânico",
  "price": 250.00
}
```

**Resposta esperada — `201 Created`:**

```json
{
  "sku": "SKU-001",
  "name": "Teclado Mecânico",
  "price": 250.00,
  "active": true
}
```

### Exemplo: consultar produto

```http
GET /products/SKU-001
```

**Resposta esperada — `200 OK`:**

```json
{
  "sku": "SKU-001",
  "name": "Teclado Mecânico",
  "price": 250.00,
  "active": true
}
```

### Respostas de erro

Para regras de negócio, a API utiliza o DTO `ApiErrorResponse`:

```json
{
  "message": "Product with SKU SKU-001 already exists"
}
```

- **`409 Conflict`:** `DuplicateSkuException`.
- **`404 Not Found`:** `ProductNotFoundException`.
- **`400 Bad Request`:** validações de `@NotBlank`, `@NotNull`, `@DecimalMin` e `@Digits`.

O tratamento de erros de validação via `MethodArgumentNotValidException` foi desenvolvido para reutilizar o mesmo formato `{"message": "..."}`. A consolidação do refactor, a execução completa dos testes e o push das mudanças de código ainda precisam ser verificados.

> A verificação de SKU antes de `save()` facilita o erro de negócio, mas **não elimina disputas concorrentes**. A restrição única no banco é a proteção definitiva; o mapeamento do erro de integridade concorrente para HTTP 409 ainda pode ser aprimorado.

## Estoque e pedidos — planejamento

A migration inicial do Flyway prepara **produtos e inventário**. O domínio completo de estoque/pedidos ainda será implementado.

### Estoque

```text
Estoque disponível = Estoque físico − Estoque reservado
```

A reserva reduz o estoque disponível. A baixa no estoque físico ocorrerá **somente na expedição**.

O módulo deverá oferecer entradas de estoque, consulta de saldos, histórico de movimentações e proteção contra estoque negativo e concorrência.

### Pedidos

- Um pedido poderá conter **múltiplos itens**.
- Falta de estoque em qualquer item deverá **rejeitar o pedido inteiro**, sem reserva parcial.
- A criação do pedido deverá reservar o estoque por **10 minutos**.
- A confirmação será explícita, via `POST /orders/{id}/confirm`, antes da expiração.
- Cancelamento permitido apenas para pedidos `RESERVED` e ainda válidos.
- Produtos inativos não poderão compor novos pedidos.
- Valores unitários serão preservados no momento da criação do pedido.
- Pedidos terão identificadores **UUID** e chave de idempotência para evitar duplicação.
- As operações de reserva/expedição deverão garantir consistência sob concorrência.

### Ciclo de vida previsto

```text
                    RESERVED
                   /   |   \
                  v    v    v
          CANCELLED  EXPIRED  CONFIRMED
                                |
                                v
                             SHIPPED
```

| Estado | Significado |
|---|---|
| `RESERVED` | Pedido criado, com estoque reservado até o prazo de expiração |
| `CONFIRMED` | Pedido confirmado dentro do prazo; reserva mantida |
| `SHIPPED` | Pedido expedido; baixa definitiva do estoque físico |
| `CANCELLED` | Pedido cancelado enquanto reservado; estoque liberado |
| `EXPIRED` | Prazo de confirmação vencido; estoque liberado |

## Executar localmente

### Pré-requisitos

- **JDK 25**
- **Docker Desktop + Docker Compose**, com o Docker Engine em funcionamento
- **Git**

O desenvolvimento atual utiliza **IntelliJ IDEA no Windows com Ubuntu WSL2** e integração WSL habilitada no Docker Desktop.

### 1. Inicializar o banco de desenvolvimento

Na raiz do repositório:

```bash
docker compose up -d postgres
docker compose ps
```

O `compose.yaml` publica o PostgreSQL em **`127.0.0.1:5433`** (porta `5432` dentro do container), evitando conflito com um PostgreSQL local na porta `5432`.

### 2. Iniciar a API

```bash
./gradlew bootRun --no-daemon --no-watch-fs --console=plain
```

As migrations Flyway são aplicadas na inicialização; o Hibernate valida a estrutura existente (`ddl-auto: validate`). Por padrão, a API fica disponível em `http://localhost:8080`, salvo configuração diferente.

> As credenciais do Compose são destinadas **somente ao desenvolvimento local**. Não reutilize essas senhas em produção.

### 3. Testar os endpoints manualmente

Com a aplicação rodando:

```bash
curl -i -X POST http://localhost:8080/products \
  -H 'Content-Type: application/json' \
  -d '{"sku":"SKU-001","name":"Teclado Mecânico","price":250.00}'

curl -i http://localhost:8080/products/SKU-001
```

### 4. Executar os testes

Toda a suíte:

```bash
./gradlew test --no-daemon --no-watch-fs --console=plain
```

Por camada:

```bash
./gradlew test --tests 'com.orderflow.model.ProductTest' --no-daemon --no-watch-fs --console=plain
./gradlew test --tests 'com.orderflow.repository.ProductRepositoryTest' --no-daemon --no-watch-fs --console=plain
./gradlew test --tests 'com.orderflow.service.ProductServiceTest' --no-daemon --no-watch-fs --console=plain
./gradlew test --tests 'com.orderflow.controller.ProductControllerTest' --no-daemon --no-watch-fs --console=plain
./gradlew test --tests 'com.orderflow.OrderflowApplicationTests' --no-daemon --no-watch-fs --console=plain
```

Caso você já tenha criado `ProductApiIntegrationTest`, execute:

```bash
./gradlew test --tests 'com.orderflow.ProductApiIntegrationTest' --no-daemon --no-watch-fs --console=plain
```

**Testcontainers exige Docker ativo**, mas usa um PostgreSQL temporário e separado do banco de desenvolvimento. O teste de inicialização (`contextLoads`) também foi adaptado para utilizar essa conexão isolada.

## Banco de dados e migrações

Os scripts SQL ficam em:

```text
src/main/resources/db/migration/
```

A migration `V1__create_products_and_inventory.sql` cria o esquema inicial de produtos e inventário. Migrações futuras deverão acrescentar pedidos, itens, reservas e histórico de movimentos de estoque.

O Flyway é a fonte de verdade do esquema; não usamos Hibernate para criar tabelas automaticamente.

## Qualidade e estratégia de testes

O desenvolvimento segue o ciclo **RED → GREEN → REFACTOR**:

1. **RED:** escrever um teste que expõe o comportamento ausente.
2. **GREEN:** implementar o mínimo para o teste passar.
3. **REFACTOR:** melhorar o código, preservando as verificações.

| Classe de teste | Escopo |
|---|---|
| `ProductTest` | Criação, validações, SKU e desativação |
| `ProductRepositoryTest` | Salvamento, busca por SKU, restrição única com PostgreSQL/Testcontainers |
| `ProductServiceTest` | Regras de cadastro e consulta com Mockito; exceções de negócio |
| `ProductControllerTest` | Contratos HTTP de cadastro/consulta, validações e respostas de erro com MockMvc |
| `OrderflowApplicationTests` | Inicialização do contexto Spring com banco de teste isolado |
| `ProductApiIntegrationTest` | **Em validação:** fluxo HTTP real `POST` → banco → `GET` |

Os testes de Controller isolados usam mocks do serviço. O teste de integração da API, quando validado, deve usar componentes reais com PostgreSQL iniciado pelo Testcontainers.

## Progresso e próximos passos

**Concluído e exercitado durante o desenvolvimento:**

- [x] Projeto Spring Boot / Java 25 / Gradle Groovy.
- [x] Docker Compose com PostgreSQL 17 e Flyway V1.
- [x] Entidade `Product`, regras e testes unitários.
- [x] Persistência JPA e testes de Repository com Testcontainers.
- [x] Serviço de cadastro/consulta, validação de SKU e exceções de negócio.
- [x] `POST /products` e `GET /products/{sku}`, com DTOs.
- [x] Testes de Controller para sucesso, validação de entrada, conflito e produto inexistente.

**Em validação / próximas entregas:**

- [ ] Confirmar a suíte completa após o refactor de `GlobalExceptionHandler`.
- [ ] Verificar o teste `ProductApiIntegrationTest` com PostgreSQL real.
- [ ] Ampliar a cobertura de respostas de erro e casos de borda.
- [ ] Implementar módulo de estoque e operações transacionais.
- [ ] Implementar pedidos, reservas, expiração e movimentações.
- [ ] Tratar concorrência, idempotência e eventos assíncronos.
- [ ] Construir dashboard com React + TypeScript.

Confira o [TODO.md](TODO.md) para o roadmap detalhado, organizado por etapas.

## Sincronização com o GitHub

Este README descreve o **progresso relatado e testado no ambiente local**. Na última conferência da branch `main` remota, o código publicado ainda não incluía todas as etapas posteriores ao cadastro de produtos, incluindo o endpoint GET, parte das validações de preço, os tratamentos HTTP 404/400 e o teste `ProductApiIntegrationTest`.

Ao sincronizar o código a partir do WSL2, primeiro confira as alterações pendentes e o histórico remoto. **Como a documentação pode ser atualizada diretamente no GitHub, não faça `git push --force`.** Se houver trabalho local ainda sem commit, registre-o antes de integrar o histórico:

```bash
git status
git add <arquivos-de-codigo-alterados>
git commit -m "feat: consolida endpoints e validacoes de produtos"
git pull --rebase origin main
./gradlew test --no-daemon --no-watch-fs --console=plain
git push origin main
```

Substitua `<arquivos-de-codigo-alterados>` pelos caminhos reais que deseja incluir no commit; não execute o comando literalmente. Se o rebase acusar conflitos, resolva-os antes do push.

## Status

**Em desenvolvimento.** O projeto prioriza incrementos pequenos, testes automatizados, contratos HTTP claros e integridade dos dados. O módulo de produtos é a base para os próximos módulos de estoque e pedidos.