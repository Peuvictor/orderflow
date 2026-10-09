# OrderFlow — API de Gerenciamento de Pedidos e Estoque

O **OrderFlow** é uma API REST desenvolvida com Java e Spring Boot para gerenciar produtos, pedidos, movimentações de estoque e reservas temporárias.

O projeto tem como objetivo aplicar conceitos de desenvolvimento backend, como **consistência transacional, controle de concorrência, persistência de dados e processamento assíncrono de eventos**.

O desenvolvimento segue a metodologia **TDD (Test-Driven Development)**, utilizando arquitetura em camadas e testes automatizados.

## Tecnologias utilizadas

- **Java 25**
- **Spring Boot**
- **Gradle (Groovy DSL)**
- **Spring Data JPA / Hibernate**
- **PostgreSQL 17**
- **Flyway**
- **Lombok**
- **JUnit e Mockito**
- **Docker e Docker Compose**

### Tecnologias planejadas

- **Testcontainers:** testes de integração utilizando containers.
- **RabbitMQ:** processamento assíncrono de eventos.
- **React + TypeScript:** desenvolvimento de um dashboard para gerenciamento dos pedidos e do estoque.

## Arquitetura

O projeto utiliza **arquitetura em camadas**, separando as responsabilidades da aplicação.

```text
Controller → Service → Repository → PostgreSQL
```

Responsabilidades:

- **Controller:** recebe requisições HTTP e retorna respostas.
- **Service:** executa as regras de negócio e gerencia as transações.
- **Repository:** realiza as operações de persistência.
- **Model:** representa as entidades e suas regras.
- **DTO:** transporta os dados das requisições e respostas.

## Funcionalidades planejadas

### Gerenciamento de produtos

- Cadastro e consulta de produtos.
- Identificação por SKU único.
- Atualização de informações e preços.
- Ativação e desativação de produtos.
- Impedimento de novos pedidos com produtos desativados.

### Controle de estoque

- Registro de entradas de estoque.
- Consulta do estoque físico, reservado e disponível.
- Histórico de movimentações.
- Prevenção de estoque negativo.
- Controle de concorrência durante reservas.

### Gerenciamento de pedidos

- Criação de pedidos com múltiplos produtos.
- Validação integral da disponibilidade.
- Reserva automática na criação do pedido.
- Expiração da reserva após **10 minutos**.
- Confirmação manual pela API.
- Cancelamento de pedidos reservados.
- Expedição de pedidos confirmados.
- Prevenção de requisições duplicadas por chave de idempotência.

## Ciclo de vida do pedido

Os pedidos poderão assumir cinco estados:

```text
              RESERVED
             /    |    \
            /     |     \
           v      v      v
    CANCELLED  EXPIRED  CONFIRMED
                           |
                           v
                        SHIPPED
```

- **RESERVED:** pedido criado com estoque reservado.
- **CONFIRMED:** pedido confirmado antes do prazo de expiração.
- **SHIPPED:** pedido expedido com baixa definitiva no estoque físico.
- **CANCELLED:** pedido cancelado e reserva liberada.
- **EXPIRED:** reserva expirada e estoque liberado.

## Regras de negócio

O sistema seguirá as seguintes regras:

1. Um pedido poderá conter múltiplos produtos.
2. Se qualquer produto não possuir estoque suficiente, o pedido inteiro será rejeitado.
3. O estoque será reservado no momento da criação do pedido.
4. As reservas terão validade de 10 minutos.
5. A confirmação será realizada manualmente por um endpoint.
6. Apenas pedidos reservados e dentro do prazo poderão ser cancelados.
7. O estoque físico será reduzido somente na expedição.
8. Pedidos confirmados não poderão ser cancelados no fluxo inicial.
9. Requisições duplicadas não poderão gerar pedidos adicionais.
10. Produtos deverão possuir SKU único.
11. Produtos desativados não poderão participar de novos pedidos.
12. Os preços serão preservados no momento da criação do pedido.
13. Os pedidos serão identificados por UUID.
14. Operações de estoque deverão preservar a consistência dos dados, inclusive em situações de concorrência.

## Como executar o projeto

### Pré-requisitos

- JDK 25
- Docker e Docker Compose
- Git

O ambiente de desenvolvimento atual utiliza IntelliJ IDEA integrado ao WSL2.

### 1. Iniciar o PostgreSQL

Na raiz do projeto, execute:

```bash
docker compose up -d
```

O PostgreSQL estará disponível na porta `5433`, configurada para evitar conflito com a instalação local.

### 2. Executar a aplicação

```bash
./gradlew bootRun
```

O Flyway executará as migrations pendentes durante a inicialização.

### 3. Executar os testes

```bash
./gradlew test --no-daemon --no-watch-fs
```

Para executar apenas os testes da entidade `Product`:

```bash
./gradlew test --tests 'com.orderflow.model.ProductTest' --no-daemon --no-watch-fs
```

## Banco de dados

O projeto utiliza PostgreSQL para persistência e Flyway para gerenciamento das migrations.

Os scripts SQL estão localizados em:

```text
src/main/resources/db/migration/
```

O controle de estoque seguirá a relação:

**Estoque disponível = Estoque físico − Estoque reservado**

A reserva compromete a disponibilidade, enquanto a baixa física acontece somente após a expedição.

## Testes automatizados

O desenvolvimento segue o ciclo TDD:

1. **RED:** escrever um teste que falha.
2. **GREEN:** implementar o mínimo necessário para passar.
3. **REFACTOR:** melhorar a implementação mantendo os testes passando.

A estratégia de testes inclui:

- Testes unitários para regras de negócio.
- Testes de integração com PostgreSQL.
- Testes de endpoints REST.
- Testes de transações e concorrência.
- Testes de expiração de reservas.

## Progresso do desenvolvimento

### Concluído

- [x] Configuração inicial do Spring Boot com Java 25.
- [x] Configuração do Gradle com Groovy DSL.
- [x] PostgreSQL 17 executando no Docker.
- [x] Configuração da conexão com o banco.
- [x] Primeira migration executada com Flyway.
- [x] Primeiros testes unitários da entidade Product utilizando TDD.

### Em desenvolvimento

- [ ] Persistência e endpoints de produtos.
- [ ] Gerenciamento de estoque.
- [ ] Criação e validação de pedidos.
- [ ] Reserva temporária e expiração automática.
- [ ] Confirmação, cancelamento e expedição.
- [ ] Tratamento de concorrência e idempotência.
- [ ] Testes de integração com Testcontainers.
- [ ] Mensageria com RabbitMQ.
- [ ] Dashboard em React e TypeScript.

## Status do projeto

**Em desenvolvimento.**

O OrderFlow está sendo construído de forma incremental, priorizando testes automatizados, organização do código, consistência dos dados e boas práticas de desenvolvimento backend.
