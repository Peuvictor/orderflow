# OrderFlow — API de Gerenciamento de Pedidos e Estoque

O **OrderFlow** é um projeto de API REST desenvolvido com **Java 25 e Spring Boot** para gerenciar produtos, pedidos, estoque e reservas temporárias.

O objetivo é exercitar conceitos de desenvolvimento backend, especialmente **regras de negócio, persistência, integridade dos dados, transações, concorrência, idempotência e processamento assíncrono**.

O desenvolvimento é incremental, seguindo **TDD (Test-Driven Development)**, arquitetura em camadas e testes automatizados.

> **Status:** em desenvolvimento. A infraestrutura de persistência e as primeiras regras de produtos já foram implementadas e testadas. Os endpoints REST e o fluxo de pedidos ainda estão planejados.

## Tecnologias utilizadas

- **Java 25**
- **Spring Boot**
- **Gradle (Groovy DSL)**
- **Spring Data JPA / Hibernate**
- **PostgreSQL 17**
- **Flyway** para versionamento do banco
- **Lombok** para redução de código repetitivo
- **JUnit 5 e Mockito** para testes unitários
- **Testcontainers** para testes de integração com PostgreSQL
- **Docker e Docker Compose**

### Tecnologias planejadas

- **RabbitMQ:** publicação e processamento assíncrono de eventos.
- **React + TypeScript:** dashboard para gerenciamento de produtos, pedidos e estoque.

## Arquitetura

A aplicação segue uma **arquitetura em camadas**, com responsabilidades separadas:

```text
Controller (planejado)
        ↓
      Service
        ↓
     Repository
        ↓
     PostgreSQL
```

- **Controller:** receberá requisições HTTP e retornará respostas da API.
- **Service:** implementa as regras de negócio e coordena operações da aplicação.
- **Repository:** oferece acesso aos dados por meio do Spring Data JPA.
- **Model:** representa as entidades e seus comportamentos.
- **DTO:** será utilizado para definir os contratos de entrada e saída da API.

### Estrutura atual dos principais componentes

```text
src/
├── main/
│   ├── java/com/orderflow/
│   │   ├── model/
│   │   │   └── Product.java
│   │   ├── repository/
│   │   │   └── ProductRepository.java
│   │   └── service/
│   │       ├── ProductService.java
│   │       └── DuplicateSkuException.java
│   └── resources/
│       └── db/migration/
│           └── V1__create_products_and_inventory.sql
└── test/
    └── java/com/orderflow/
        ├── model/ProductTest.java
        ├── repository/ProductRepositoryTest.java
        └── service/ProductServiceTest.java
```

## Funcionalidades implementadas

### Produtos — domínio

- Criação de produtos com **SKU, nome e preço**.
- Validação de campos obrigatórios e preço positivo.
- Restrição de preço a, no máximo, duas casas decimais significativas.
- Normalização do SKU: remoção de espaços externos e conversão para maiúsculas.
- Produtos criados inicialmente como **ativos**.
- Operação de **desativação** de produtos.
- Mapeamento da entidade `Product` com **JPA**.

### Produtos — persistência e serviço

- Persistência de produtos por meio do `ProductRepository`.
- Consulta de produtos por SKU (`findBySku`).
- Verificação de existência de SKU (`existsBySku`).
- Restrição de **SKU único** no PostgreSQL.
- Verificação de duplicidade no `ProductService` antes de salvar um produto.
- Exceção de negócio `DuplicateSkuException` para SKU já cadastrado.

> A validação no serviço melhora o tratamento da regra de negócio. A restrição no banco continua necessária para garantir integridade, inclusive em situações de concorrência. O tratamento dessa exceção como resposta HTTP `409 Conflict` será implementado na camada REST.

## Funcionalidades planejadas

### Gerenciamento de produtos

- Endpoints REST para cadastro e consulta.
- Atualização de informações e preços.
- Ativação e desativação pela API.
- Impedimento de novos pedidos com produtos desativados.

### Controle de estoque

- Registro de entradas e movimentações.
- Consulta de estoque físico, reservado e disponível.
- Prevenção de estoque negativo.
- Controle transacional e de concorrência nas reservas.

### Gerenciamento de pedidos

- Criação de pedidos com múltiplos produtos.
- Rejeição integral do pedido quando qualquer item não tiver estoque suficiente.
- Reserva automática de estoque na criação.
- Expiração de reservas após **10 minutos**.
- Confirmação manual de pedidos pela API.
- Cancelamento permitido somente para pedidos `RESERVED` dentro do prazo.
- Expedição de pedidos confirmados, com baixa do estoque físico.
- Proteção contra solicitações duplicadas usando chave de idempotência.
- Preservação dos preços dos produtos no momento do pedido.
- Identificação dos pedidos por **UUID**.

## Ciclo de vida do pedido (planejado)

```text
                    RESERVED
                   /   |   \
                  v    v    v
          CANCELLED  EXPIRED  CONFIRMED
                                |
                                v
                             SHIPPED
```

- **RESERVED:** pedido criado, com estoque reservado por até 10 minutos.
- **CONFIRMED:** pedido confirmado antes do vencimento da reserva; o estoque permanece reservado.
- **SHIPPED:** pedido expedido; ocorre a redução definitiva do estoque físico.
- **CANCELLED:** pedido cancelado enquanto estava reservado; a reserva é liberada.
- **EXPIRED:** pedido não confirmado dentro do prazo; a reserva é liberada automaticamente.

**Relação do estoque:**

```text
Estoque disponível = Estoque físico − Estoque reservado
```

A criação de uma reserva reduz o **estoque disponível**, mas não o **estoque físico**. A baixa física acontece somente na expedição.

## Como executar o projeto

### Pré-requisitos

- **JDK 25**
- **Docker** com suporte a Docker Compose
- **Git**

O ambiente utilizado no desenvolvimento é o **IntelliJ IDEA no Windows integrado ao Ubuntu WSL2**, com Docker Desktop e integração WSL habilitada.

### 1. Iniciar o PostgreSQL

Na raiz do projeto:

```bash
docker compose up -d postgres
```

O PostgreSQL de desenvolvimento é exposto em `127.0.0.1:5433` para evitar conflito com uma instalação local na porta `5432`.

Verifique o container:

```bash
docker compose ps
```

### 2. Executar a aplicação

```bash
./gradlew bootRun --no-daemon --no-watch-fs --console=plain
```

O **Flyway** aplica as migrations pendentes na inicialização. O Hibernate está configurado para **validar** o esquema, sem criá-lo automaticamente.

> Os dados e a senha definidos no `compose.yaml` destinam-se ao ambiente local de desenvolvimento; não devem ser reutilizados em produção.

### 3. Executar os testes

Todos os testes:

```bash
./gradlew test --no-daemon --no-watch-fs --console=plain
```

Testes unitários do domínio:

```bash
./gradlew test --tests 'com.orderflow.model.ProductTest' --no-daemon --no-watch-fs --console=plain
```

Testes de integração do repositório:

```bash
./gradlew test --tests 'com.orderflow.repository.ProductRepositoryTest' --no-daemon --no-watch-fs --console=plain
```

Testes unitários do serviço:

```bash
./gradlew test --tests 'com.orderflow.service.ProductServiceTest' --no-daemon --no-watch-fs --console=plain
```

**Importante:** os testes com **Testcontainers** precisam que o Docker esteja em execução. O container PostgreSQL usado nos testes é separado do banco de desenvolvimento.

## Banco de dados

O projeto utiliza **PostgreSQL 17** e **Flyway**. As migrations ficam em:

```text
src/main/resources/db/migration/
```

A migration inicial (`V1`) prepara as tabelas de **produtos e inventário**. As estruturas relacionadas a pedidos, reservas e movimentações serão adicionadas nas próximas etapas.

## Estratégia de testes

O desenvolvimento segue o ciclo:

1. **RED:** escrever um teste que falha, estabelecendo o comportamento esperado.
2. **GREEN:** implementar o mínimo necessário para fazê-lo passar.
3. **REFACTOR:** melhorar o código preservando os testes.

### Cobertura implementada até o momento

| Teste | Responsabilidade |
|---|---|
| `ProductTest` | Criação, validações, normalização do SKU e desativação |
| `ProductRepositoryTest` | Persistência, consulta por SKU e rejeição de SKU duplicado no PostgreSQL |
| `ProductServiceTest` | Rejeição de SKU duplicado, com Mockito e sem gravação no banco |

Os testes de repositório usam **Testcontainers** para validar o comportamento com PostgreSQL real, sem depender dos dados do banco de desenvolvimento.
- [x] Mapeamento JPA da entidade `Product`.
- [x] Implementação de `ProductRepository`.
- [x] Testes de integração com PostgreSQL usando Testcontainers.
- [x] Restrição de SKU único validada no banco.
- [x] Implementação inicial de `ProductService` e `DuplicateSkuException`.
- [x] Teste unitário de rejeição de SKU duplicado no serviço.

### Próximas etapas

- [ ] Ampliar os testes do `ProductService` para cadastro com SKU disponível.
- [ ] Implementar DTOs, tratamento de erros e endpoints REST de produtos.
- [ ] Implementar operações de estoque.
- [ ] Implementar criação de pedidos e reserva temporária.
- [ ] Implementar confirmação, cancelamento, expiração e expedição.
- [ ] Testar transações, concorrência e idempotência.
- [ ] Integrar mensageria com RabbitMQ.
- [ ] Desenvolver dashboard em React + TypeScript.

## Status do projeto

**Em desenvolvimento.**

O OrderFlow está sendo construído de maneira incremental, priorizando **testes automatizados, regras de negócio explícitas, integridade dos
## Progresso do desenvolvimento

### Concluído

- [x] Configuração de Java 25, Spring Boot e Gradle Groovy.
- [x] Configuração do PostgreSQL 17 com Docker Compose.
- [x] Configuração de conexão com o banco e primeira migration Flyway.
- [x] Testes unitários da entidade `Product`.

