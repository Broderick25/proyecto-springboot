package com.SpringBoot.infrastructure.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Habilita el caché respaldado por Redis (ya provisionado en docker-compose, sin usar hasta
 * ahora). Serializa los valores como JSON (no JDK serialization) para no exigir que las clases
 * cacheadas implementen {@code Serializable} — {@code CustomerInfo} es un record y no lo hace.
 *
 * <p>El {@code CacheManager} se declara explícitamente en vez de confiar en la
 * auto-configuración de Spring Boot para Redis: en esta versión (4.1) esa auto-configuración
 * no registra el bean, y sin él cualquier método {@code @Cacheable} falla al arrancar con
 * "no CacheResolver specified".
 *
 * <p>{@code order = -100} pone el advisor de caché por FUERA de {@code HandlerAuditAspect}
 * (que quedó en {@code @Order(0)}): en un cache hit, el interceptor de {@code @Cacheable}
 * devuelve el valor cacheado sin siquiera invocar al método real, así que el advice de
 * auditoría (más adentro) nunca se ejecuta y no escribe en {@code audit_logs} en cada lectura
 * cacheada. Para los {@code CommandHandler} (que no tienen {@code @Cacheable}) esto no cambia
 * nada — ahí lo relevante sigue siendo que la auditoría quede por fuera de {@code @Transactional}
 * (que no se toca, sigue en su order por defecto).
 *
 * <p>Implementa {@code CachingConfigurer} solo para instalar {@link LoggingCacheErrorHandler}:
 * sin esto, un fallo de Redis (timeout, conexión rechazada) tumba el request completo en vez de
 * degradar a "sin caché" — ver el javadoc de esa clase.
 */
@Configuration
@EnableCaching(order = -100)
public class CacheConfig implements CachingConfigurer {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                      RedisCacheConfiguration cacheConfiguration) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }
}
