# Redis — checklist pendiente antes de un despliegue real

> **Estado: auditado, parcialmente resuelto.** El único punto crítico ya se implementó. Los
> demás quedaron deliberadamente pospuestos — ver "Por qué no se hicieron ya" al final.

## Configuración actual

**Conexión** (`erp-api/src/main/resources/application.yaml`):
```yaml
spring.data.redis:
  host: localhost
  port: 6379
  password: secret
  timeout: 2000ms
  lettuce:
    pool:
      max-active: 8
      max-idle: 8
      min-idle: 0
```

**Infraestructura** (`docker-compose.yml`): contenedor `redis:7` con
`--requirepass secret --maxmemory 256mb --maxmemory-policy allkeys-lru`, más `redis-commander`
(UI de administración) apuntando al mismo Redis.

**Caché de aplicación** (`erp-infrastructure/config/CacheConfig.java`): un `CacheManager` Redis
con TTL global de 15 minutos, valores serializados en JSON (`GenericJackson2JsonRedisSerializer`,
para no exigir `Serializable` en records como `CustomerInfo`), `disableCachingNullValues()`, y
`@EnableCaching(order = -100)` para que el advisor de caché quede por fuera de
`HandlerAuditAspect` (un cache hit no genera una escritura de auditoría).

7 caches activos:

| Cache | Quién lo usa | Invalidación |
|---|---|---|
| `productsById`, `productsBySku` | `GetProductByIdQueryHandler`, `GetProductBySkuQueryHandler` | `ProductProjection` (evicción dirigida por evento) |
| `ordersById` | `GetOrderByIdQueryHandler` | `OrderCacheEvictionListener` (evicción dirigida por evento) |
| `categoriesCatalog`, `categoriesByCode`, `categoriesById` | `ListCategoriesQueryHandler`, `GetCategoryByCodeQueryHandler`, `GetCategoryByIdQueryHandler` | `@CacheEvict(allEntries = true)` en los 3 comandos de categoría |
| `customers` | `CustomerHttpAdapter.findById` | **Ninguna** — depende solo del TTL |

`customers` tiene además `unless = "#result.isEmpty()"` — evita cachear un `Optional.empty()`
(cliente no encontrado). Con JSONPlaceholder (dataset estático) no cambiaba nada en la práctica,
pero protege contra un bug real si el adapter se reemplaza algún día por un cliente de un
servicio de clientes con datos mutables: sin esto, un cliente recién creado en el servicio real
seguiría devolviendo "no encontrado" acá hasta que expire el TTL.

## ✅ Ya resuelto: sin fallback si Redis se cae

Antes de este fix, `@Cacheable` no tenía un `CacheErrorHandler` configurado — el
`SimpleCacheErrorHandler` por defecto de Spring relanza cualquier excepción del proveedor de
caché, así que si Redis no respondía (timeout, conexión rechazada), **la query entera fallaba**
aunque el handler real (Postgres/Mongo) estuviera sano.

**Implementado**: `LoggingCacheErrorHandler` (`erp-infrastructure/config/`) + `CacheConfig
implements CachingConfigurer` con `errorHandler()` devolviéndolo. Ahora un GET/PUT/EVICT/CLEAR
fallido en Redis se loguea como warning y la operación sigue sin caché, en vez de propagar la
excepción.

## ⬜ Pendiente 1 — TTL único (15 min) para todo, sin distinguir por volatilidad

Los 15 minutos de `cacheDefaults(cacheConfiguration)` aplican igual a caches con invalidación
activa por evento (`productsById`, `ordersById`, los 3 de `categories*` — ahí el TTL es solo una
red de seguridad) que a `customers`, que no tiene ninguna invalidación activa — el TTL es su
**único** mecanismo de frescura.

**Cómo resolverlo**: `RedisCacheManager.builder(...).withInitialCacheConfigurations(Map<String,
RedisCacheConfiguration>)` para overridear el TTL por nombre de cache:

```java
Map<String, RedisCacheConfiguration> perCache = Map.of(
    ProductCacheNames.BY_ID,    defaultConfig.entryTtl(Duration.ofHours(1)),
    ProductCacheNames.BY_SKU,   defaultConfig.entryTtl(Duration.ofHours(1)),
    OrderCacheNames.BY_ID,      defaultConfig.entryTtl(Duration.ofHours(1)),
    CategoryCacheNames.CATALOG, defaultConfig.entryTtl(Duration.ofHours(1)),
    CategoryCacheNames.BY_CODE, defaultConfig.entryTtl(Duration.ofHours(1)),
    CategoryCacheNames.BY_ID,   defaultConfig.entryTtl(Duration.ofHours(1)),
    "customers",                defaultConfig.entryTtl(Duration.ofMinutes(5))
);

RedisCacheManager.builder(connectionFactory)
    .cacheDefaults(defaultConfig)
    .withInitialCacheConfigurations(perCache)
    .build();
```

## ⬜ Pendiente 2 — password en texto plano

`password: secret` hardcodeado en `application.yaml` y `docker-compose.yml`, sin variable de
entorno (a diferencia de Mail, que ya usa `${GMAIL_USERNAME}`/`${GMAIL_APP_PASSWORD}`). Mismo
problema en Postgres y Mongo. El archivo tampoco está en `.gitignore`.

**Cómo resolverlo**: `password: ${REDIS_PASSWORD:secret}` — mismo patrón que Mail, con un
default para que `docker-compose up` local siga funcionando sin configurar nada. Esto no borra
el secreto del historial de git (para eso haría falta reescribir historia — destructivo, no se
haría sin pedirlo explícitamente), pero permite que un despliegue real pise el valor solo con
una variable de entorno. Aplicar el mismo cambio a Postgres/Mongo.

## ⬜ Pendiente 3 — sin observabilidad

No hay `spring-boot-starter-actuator`/Micrometer en el proyecto — cero métricas de
hit/miss/eviction del caché, sin health check de Redis expuesto.

**Cómo resolverlo**: agregar `spring-boot-starter-actuator` a `erp-api/pom.xml`. Sin escribir
código adicional:
- `/actuator/health` incluye automáticamente un indicador de Redis (`RedisHealthIndicator`, se
  autoconfigura al detectar `spring-boot-starter-data-redis` + actuator juntos).
- El `CacheManager` ya registrado queda instrumentado por Micrometer — métricas `cache.gets`,
  `cache.puts`, `cache.evictions` por nombre de cache en `/actuator/metrics/cache.gets`.

Además hace falta `management.endpoints.web.exposure.include: health,metrics` — por defecto
Actuator solo expone `health`/`info`. **No** exponer `*`: el proyecto no tiene autenticación en
ningún endpoint todavía, y `*` incluye `/actuator/env` (puede filtrar variables de entorno).

## Por qué no se hicieron ya

Los 3 pendientes solo importan cuando el proyecto pasa a un entorno compartido/productivo con
tráfico real y secretos verdaderos. Hoy (`docker-compose` local, sin CI/CD ni stack de
monitoreo) no tienen ningún síntoma real — instrumentarlos ahora sería asegurar/observar código
que todavía nadie usa ni mira. El fix "Ya resuelto" sí se priorizó porque corregía un bug
presente en el estado actual (Redis caído tumbaba requests hoy mismo, no en un futuro
hipotético).

**Cuándo retomarlo**: cuando el proyecto se acerque a un despliegue real, tratar estos 3 puntos
como un checklist a resolver juntos antes de salir a producción, no de a uno.
