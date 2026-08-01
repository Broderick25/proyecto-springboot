package com.SpringBoot.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingCacheErrorHandlerTest {

    @Mock
    private Cache cache;

    private final LoggingCacheErrorHandler handler = new LoggingCacheErrorHandler();

    @Test
    void handleCacheGetError_noRelanzaLaExcepcion() {
        when(cache.getName()).thenReturn("productsById");

        assertThatCode(() -> handler.handleCacheGetError(new RuntimeException("redis down"), cache, "key-1"))
                .doesNotThrowAnyException();
    }

    @Test
    void handleCachePutError_noRelanzaLaExcepcion() {
        when(cache.getName()).thenReturn("productsById");

        assertThatCode(() -> handler.handleCachePutError(new RuntimeException("redis down"), cache, "key-1", "value"))
                .doesNotThrowAnyException();
    }

    @Test
    void handleCacheEvictError_noRelanzaLaExcepcion() {
        when(cache.getName()).thenReturn("productsById");

        assertThatCode(() -> handler.handleCacheEvictError(new RuntimeException("redis down"), cache, "key-1"))
                .doesNotThrowAnyException();
    }

    @Test
    void handleCacheClearError_noRelanzaLaExcepcion() {
        when(cache.getName()).thenReturn("productsById");

        assertThatCode(() -> handler.handleCacheClearError(new RuntimeException("redis down"), cache))
                .doesNotThrowAnyException();
    }
}
