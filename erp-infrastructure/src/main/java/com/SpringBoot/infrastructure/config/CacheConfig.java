package com.SpringBoot.infrastructure.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
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
 */
@Configuration
@EnableCaching
public class CacheConfig {

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
}
