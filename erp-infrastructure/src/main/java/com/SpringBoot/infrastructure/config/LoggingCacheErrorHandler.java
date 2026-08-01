package com.SpringBoot.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * El caché es best-effort, no una dependencia dura: el comportamiento por defecto de Spring
 * ({@code SimpleCacheErrorHandler}) relanza cualquier excepción del proveedor de caché (ej.
 * Redis caído o con timeout), lo que tumba el request completo aunque el handler real
 * (Postgres/Mongo) esté perfectamente sano. Acá se loguea y se continúa — un GET/PUT/EVICT que
 * falla en Redis simplemente hace que esa lectura no se sirva (ni se guarde) desde el caché
 * esta vez, en vez de devolver un 500.
 */
public class LoggingCacheErrorHandler implements CacheErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Fallo al leer del caché '{}' (key={}) — se continúa sin caché",
                cache.getName(), key, exception);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("Fallo al escribir en el caché '{}' (key={}) — se ignora",
                cache.getName(), key, exception);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Fallo al invalidar el caché '{}' (key={}) — se ignora",
                cache.getName(), key, exception);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("Fallo al limpiar el caché '{}' — se ignora", cache.getName(), exception);
    }
}
