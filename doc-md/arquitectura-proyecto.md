# Arquitectura del proyecto — ERP Lite

Diagrama del estado actual del proyecto: módulos Maven, dependencias entre capas,
infraestructura (Docker) y modelo de dominio (JPA + Spring Data MongoDB).

## 1. Módulos Maven y dependencias entre capas

Arquitectura modular por capas (estilo Clean/Hexagonal): `erp-api` es el único punto de
entrada HTTP, `erp-domain` no depende de ningún otro módulo propio y `erp-common` es
compartido transversalmente.

```mermaid
flowchart TD
    subgraph Parent["erp-lite-parent (pom)"]
        API["erp-api\n(REST, punto de entrada)"]
        APP["erp-application\n(casos de uso)"]
        INFRA["erp-infrastructure\n(persistencia, config)"]
        DOMAIN["erp-domain\n(entidades JPA, documentos Mongo, repos)"]
        COMMON["erp-common\n(utilidades compartidas)"]
    end

    API --> APP
    API --> INFRA
    APP --> DOMAIN
    INFRA --> DOMAIN
    INFRA --> COMMON
    DOMAIN --> COMMON

    style DOMAIN fill:#2d5,stroke:#333,color:#000
```

- **erp-api**: `ErpApiApplication`, `HealthController`. Expone el servlet en `/api` (puerto `9090`).
- **erp-application**: `ApplicationService` (aún placeholder, capa de casos de uso).
- **erp-domain**: entidades JPA (`entity`), documentos Mongo (`document`), interfaces de
  repositorio (`repository`). No tiene dependencias a `erp-application` ni `erp-infrastructure`.
- **erp-infrastructure**: `MongoConfig` (`@EnableMongoAuditing`), `BaseRepository` (placeholder).
  Aporta los starters de Postgres/Mongo/Redis a los módulos que dependen de él.
- **erp-common**: utilidades genéricas (`CommonUtil`).

## 2. Infraestructura y despliegue (docker-compose)

```mermaid
flowchart LR
    Client(["Cliente HTTP"]) -->|":9090/api"| API["erp-api\n(Spring Boot 4.1 / JDK 25)"]

    subgraph Docker["docker-compose (erp-vpc network)"]
        PG[("PostgreSQL 17\nerp-postgres\n:5433 -> 5432")]
        MG[("MongoDB 8\nerp-mongodb\n:27017")]
        RD[("Redis 7\nerp-redis\n:6379")]
        RC["redis-commander\n(UI :8081)"]
    end

    API -->|"JDBC\nvalidate schema"| PG
    API -->|"spring.mongodb.uri"| MG
    API -->|"spring.data.redis\n(cache, lettuce pool)"| RD
    RC --> RD

    PG -.->|"init scripts"| SQL["01-schema.sql\n02-data.sql"]
    MG -.->|"init scripts"| JS["init-mongo.js"]
```

- Postgres: datos transaccionales (`orders`, `order_products`, `products`).
- MongoDB: catálogos, documentos flexibles y auditoría (`product_documents`, `catalogs`, `audit_logs`).
- Redis: cache (pool Lettuce configurado, `commons-pool2` en `erp-infrastructure`).

## 3. Modelo de dominio

### 3.1 Entidades JPA (PostgreSQL)

```mermaid
classDiagram
    class BaseEntity {
        <<MappedSuperclass>>
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }
    class Product {
        UUID id
        String sku
        String name
        String description
        BigDecimal price
        Integer stock
        String categoryId
        String imageUrl
        Boolean active
    }
    class Order {
        UUID id
        String orderNumber
        Long customerId
        String customerName
        String createdBy
        LocalDateTime orderDate
        OrderStatus status
        BigDecimal totalAmount
        String currency
        addOrderProduct()
        removeOrderProduct()
    }
    class OrderProduct {
        UUID id
        String productName
        Integer quantity
        BigDecimal unitPrice
        BigDecimal subtotal
    }
    class OrderStatus {
        <<enumeration>>
        PENDING
        CONFIRMED
        SHIPPED
        DELIVERED
        CANCELLED
    }

    BaseEntity <|-- Product
    BaseEntity <|-- Order
    Order "1" *-- "many" OrderProduct : cascade ALL, orphanRemoval
    Product "1" o-- "many" OrderProduct : ON DELETE RESTRICT
    Order --> OrderStatus
```

Repositorios: `ProductRepository`, `OrderRepository`, `OrderProductRepository`
(`JpaRepository<T, UUID>`).

### 3.2 Documentos MongoDB

```mermaid
classDiagram
    class ProductDocument {
        String id
        String sku
        String name
        String description
        BigDecimal price
        String categoryId
        List~String~ tags
        ProductSpecifications specifications
        Instant createdAt
        Instant updatedAt
    }
    class ProductSpecifications {
        <<embedded>>
    }
    class Catalog {
        String id
        String catalogType
        String name
        Boolean active
        List~CatalogItem~ items
    }
    class CatalogItem {
        <<embedded>>
    }
    class AuditLog {
        String id
        String className
        String methodName
        String userId
        Instant timestamp
        Long executionTimeMs
        Boolean success
    }

    ProductDocument *-- ProductSpecifications
    Catalog *-- "many" CatalogItem
```

Repositorios: `ProductDocumentRepository`, `CatalogRepository`, `AuditLogRepository`
(`MongoRepository`).

## Notas del estado actual

- `erp-application` (`ApplicationService`) e `erp-infrastructure` (`BaseRepository`) son
  todavía placeholders sin lógica real — la capa de casos de uso aún no está implementada.
- El módulo `erp-api` sólo expone `HealthController`; no hay controladores de negocio (productos,
  órdenes) todavía.
- Relación entre Postgres y Mongo es lógica, no por FK: `Product.categoryId` referencia un
  documento de catálogo en MongoDB (`erp_catalog_db`), no una tabla relacional.
