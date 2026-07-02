# Spring Data MongoDB — ERP Catalog Mappings

Colecciones: `audit_logs` · `catalogs` · `product_documents`

> **Convención aplicada**
> - `class` → documentos raíz con ciclo de vida propio en MongoDB (`@Document`)
> - `record` → objetos embebidos inmutables sin identidad propia

---

## Módulo destino

Todos los documentos van en **`erp-domain`**, junto a las entidades JPA:

```
erp-domain/src/main/java/com/SpringBoot/domain/document/
│
├── AuditLog.java              @Document  → audit_logs
├── Catalog.java               @Document  → catalogs
│   └── CatalogItem.java       record     (embebido en Catalog.items)
├── ProductDocument.java       @Document  → product_documents
│   └── ProductSpecifications.java  record  (embebido en ProductDocument.specifications)
```

> `ProductDocument` (no `Product`) para evitar conflicto de nombre con la entidad JPA
> `com.SpringBoot.domain.entity.Product`.

---

## 1. `AuditLog` — colección `audit_logs`

```java
package com.SpringBoot.domain.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    private String id;

    @Field("className")
    private String className;

    @Field("methodName")
    private String methodName;

    @Indexed
    @Field("userId")
    private String userId;

    @Indexed
    @Field("timestamp")
    private Instant timestamp;

    @Field("executionTimeMs")
    private Long executionTimeMs;

    @Field("success")
    private Boolean success;

    @Field("errorMessage")
    private String errorMessage;

    @Field("ipAddress")
    private String ipAddress;

    @Field("endpoint")
    private String endpoint;
}
```

### Decisiones de diseño

| Campo | Tipo Java | Notas |
|---|---|---|
| `id` | `String` | El `_id` es un `ObjectId` generado por Mongo |
| `timestamp` | `Instant` | Mapea directamente el `$date` de BSON |
| `executionTimeMs` | `Long` | Nullable — puede estar ausente en logs de error |
| `success` | `Boolean` | Boxed para permitir `null` |
| `userId`, `timestamp` | `@Indexed` | Queries de auditoría más frecuentes |

---

## 2. `CatalogItem` — record embebido

```java
package com.SpringBoot.domain.document;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

public record CatalogItem(

        @Field("id")
        String id,

        @Field("code")
        String code,

        @Field("value")
        String value,

        @Field("description")
        String description,

        @Field("displayOrder")
        Integer displayOrder,

        /**
         * Flexible metadata map — content varies per catalogType
         * (e.g. icon, color, fee, cost, estimatedDays, flag, currency, nextStatuses…).
         */
        @Field("metadata")
        Map<String, Object> metadata
) {}
```

### Decisiones de diseño

| Campo | Tipo Java | Notas |
|---|---|---|
| `metadata` | `Map<String, Object>` | Polimórfico: cada `catalogType` tiene estructura diferente (`nextStatuses` como lista, `fee` como double, `flag` como string, etc.) |
| `displayOrder` | `Integer` | Determina el orden de render en UI |

---

## 3. `Catalog` — colección `catalogs`

```java
package com.SpringBoot.domain.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Document(collection = "catalogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Catalog {

    /**
     * Human-readable ID (e.g. "catalog-order-statuses").
     * MongoDB stores it as _id directly — no auto-generated ObjectId needed here.
     */
    @Id
    private String id;

    @Indexed(unique = true)
    @Field("catalogType")
    private String catalogType;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("active")
    private Boolean active;

    /** Embedded list of catalog entries — each entry is an immutable record. */
    @Field("items")
    private List<CatalogItem> items;

    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;
}
```

### Decisiones de diseño

| Campo | Tipo Java | Notas |
|---|---|---|
| `id` | `String` | El `_id` es el slug legible (`"catalog-payment-methods"`), no un ObjectId |
| `catalogType` | `String` + `@Indexed(unique=true)` | Clave de búsqueda principal desde aplicación |
| `items` | `List<CatalogItem>` | Lista de records embebidos — se serializa completa dentro del documento |

---

## 4. `ProductSpecifications` — record embebido

```java
package com.SpringBoot.domain.document;

import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Embedded specifications for a {@link ProductDocument}.
 * Modelled as a record — immutable snapshot of technical attributes.
 *
 * All fields are nullable because specifications vary significantly
 * per product category (electronics vs. furniture vs. stationery).
 * Fields not present in a given product's document are simply null.
 *
 * Category coverage:
 *   Electronics : processor, ram, storage, display, weight, size, resolution,
 *                 panel, connectivity, colorGamut, switches, layout, frame,
 *                 backlight, dpi, buttons, battery, type, speed, duplex, fps,
 *                 microphone, autofocus, interfaceStandard, ports, powerDelivery,
 *                 videoOutput, length, standard, dataTransfer, charging,
 *                 compatibility, noiseCancelling
 *   Furniture   : dimensions, heightRange, weightCapacity, material, motor,
 *                 brand, model, adjustable, maxWeight, brightness, colorTemp,
 *                 usbPort, compartments, cableManagement, surface, includes, mounting
 *   Accessories : capacity, laptopSize, color, height, maintenance
 *   Stationery  : pages, ruling, cover, quantity, tipSize, inkColor, refillable
 */
public record ProductSpecifications(

        // -- Electronics: laptops / tablets ----------------------------------
        @Field("processor")   String processor,
        @Field("ram")         String ram,
        @Field("storage")     String storage,
        @Field("display")     String display,
        @Field("weight")      String weight,

        // -- Electronics: monitors -------------------------------------------
        @Field("size")        String size,
        @Field("resolution")  String resolution,
        @Field("panel")       String panel,
        @Field("connectivity") String connectivity,
        @Field("colorGamut")  String colorGamut,

        // -- Electronics: keyboards ------------------------------------------
        @Field("switches")    String switches,
        @Field("layout")      String layout,
        @Field("frame")       String frame,
        @Field("backlight")   String backlight,

        // -- Electronics: mouse ----------------------------------------------
        @Field("dpi")         String dpi,
        @Field("buttons")     String buttons,
        @Field("battery")     String battery,

        // -- Electronics: printer / webcam -----------------------------------
        @Field("type")        String type,
        @Field("speed")       String speed,
        @Field("duplex")      String duplex,
        @Field("fps")         String fps,
        @Field("microphone")  String microphone,
        @Field("autofocus")   String autofocus,

        // -- Electronics: dock -----------------------------------------------
        @Field("interface")      String interfaceStandard,
        @Field("ports")          Integer ports,
        @Field("powerDelivery")  String powerDelivery,
        @Field("videoOutput")    String videoOutput,

        // -- Electronics: cables ---------------------------------------------
        @Field("length")         String length,
        @Field("standard")       String standard,
        @Field("dataTransfer")   String dataTransfer,
        @Field("charging")       String charging,
        @Field("compatibility")  String compatibility,

        // -- Furniture: desk -------------------------------------------------
        @Field("dimensions")     String dimensions,
        @Field("heightRange")    String heightRange,
        @Field("weightCapacity") String weightCapacity,
        @Field("material")       String material,
        @Field("motor")          String motor,

        // -- Furniture: chair ------------------------------------------------
        @Field("brand")          String brand,
        @Field("model")          String model,
        @Field("adjustable")     String adjustable,
        @Field("maxWeight")      String maxWeight,

        // -- Furniture: lamp -------------------------------------------------
        @Field("brightness")     String brightness,
        @Field("colorTemp")      String colorTemp,
        @Field("usbPort")        String usbPort,

        // -- Furniture: organizer / whiteboard -------------------------------
        @Field("compartments")   Integer compartments,
        @Field("cableManagement") String cableManagement,
        @Field("surface")        String surface,
        @Field("includes")       String includes,
        @Field("mounting")       String mounting,

        // -- Accessories: backpack -------------------------------------------
        @Field("capacity")       String capacity,
        @Field("laptopSize")     String laptopSize,
        @Field("color")          String color,

        // -- Accessories: artificial plant -----------------------------------
        @Field("height")         String height,
        @Field("maintenance")    String maintenance,

        // -- Stationery: notebook --------------------------------------------
        @Field("pages")          Integer pages,
        @Field("ruling")         String ruling,
        @Field("cover")          String cover,

        // -- Stationery: pens ------------------------------------------------
        @Field("quantity")       Integer quantity,
        @Field("tipSize")        String tipSize,
        @Field("inkColor")       String inkColor,
        @Field("refillable")     String refillable,

        // -- Electronics: headset --------------------------------------------
        @Field("noiseCancelling") String noiseCancelling
) {}
```

### Decisiones de diseño

| Decisión | Razón |
|---|---|
| Todos los campos son `null`able | `specifications` es polimórfico: cada categoría usa un subconjunto distinto de campos |
| `interface` → `interfaceStandard` | `interface` es keyword reservada en Java |
| `ports`, `compartments`, `pages`, `quantity` como `Integer` | Son valores numéricos enteros en el JSON, no strings |
| Sin `Map<String, Object>` | A diferencia de `CatalogItem.metadata`, aquí los campos son conocidos y tipados — un record fuertemente tipado es mejor para validación y autocompletion |

---

## 5. `ProductDocument` — colección `product_documents`

```java
package com.SpringBoot.domain.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "product_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("sku")
    private String sku;

    @TextIndexed
    @Field("name")
    private String name;

    @TextIndexed
    @Field("description")
    private String description;

    @Field(name = "price", targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    @Field("currency")
    private String currency;

    @Field("stock")
    private Integer stock;

    @Indexed
    @Field("categoryId")
    private String categoryId;

    @Field("categoryName")
    private String categoryName;

    @Field("imageUrl")
    private String imageUrl;

    @Indexed
    @Field("active")
    private Boolean active;

    @Builder.Default
    @Field("tags")
    private List<String> tags = new java.util.ArrayList<>();

    /** Embedded specifications — immutable record, varies per category. */
    @Field("specifications")
    private ProductSpecifications specifications;

    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;
}
```

### Decisiones de diseño

| Campo | Tipo Java | Notas |
|---|---|---|
| `price` | `BigDecimal` + `FieldType.DECIMAL128` | Evita errores de precisión flotante; `DECIMAL128` garantiza que se guarde como número en MongoDB, no como String |
| `name`, `description` | `@TextIndexed` | Habilita búsqueda full-text con `$text` de MongoDB |
| `sku` | `@Indexed(unique=true)` | Clave de negocio, siempre presente y única |
| `categoryId` | `@Indexed` | Join lógico con `Catalog.items[].id` |
| `active` | `@Indexed` | Filtro frecuente para excluir productos inactivos |
| `tags` | `List<String>` | Array nativo de MongoDB |
| `specifications` | `ProductSpecifications` (record) | Embebido inmutable — se lee completo junto al producto |

---

## Dependencias Maven requeridas

Ya declarada en `erp-infrastructure/pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

## Configuración `application.yml`

Ya configurada en `erp-api/src/main/resources/application.yaml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://debuggeandoideas:secret@localhost:27017/erp_catalog_db?authSource=admin
      auto-index-creation: true
```

> `?authSource=admin` es obligatorio — el usuario fue creado con `MONGO_INITDB_ROOT_USERNAME`,
> que registra las credenciales en la base `admin`, no en `erp_catalog_db`.
>
> `auto-index-creation: true` es necesario para que las anotaciones `@Indexed` y `@TextIndexed`
> creen los índices automáticamente en entornos sin el script de inicialización.

## Auditoría automática de fechas

Para que `@CreatedDate` y `@LastModifiedDate` funcionen, agregar en cualquier clase `@Configuration`:

```java
package com.SpringBoot.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {}
```
