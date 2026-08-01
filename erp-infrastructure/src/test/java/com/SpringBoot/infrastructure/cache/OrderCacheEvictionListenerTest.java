package com.SpringBoot.infrastructure.cache;

import com.SpringBoot.application.query.order.OrderCacheNames;
import com.SpringBoot.domain.order.OrderId;
import com.SpringBoot.domain.order.events.OrderCancelled;
import com.SpringBoot.domain.order.events.OrderConfirmed;
import com.SpringBoot.domain.order.events.OrderCreated;
import com.SpringBoot.domain.shared.CustomerId;
import com.SpringBoot.domain.shared.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCacheEvictionListenerTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache ordersByIdCache;

    private OrderCacheEvictionListener listener;

    @BeforeEach
    void setUp() {
        listener = new OrderCacheEvictionListener(cacheManager);
    }

    @Test
    void on_orderConfirmed_evictaLaEntradaPorId() {
        UUID orderId = UUID.randomUUID();
        when(cacheManager.getCache(OrderCacheNames.BY_ID)).thenReturn(ordersByIdCache);

        listener.on(new OrderConfirmed(OrderId.of(orderId), Instant.now()));

        verify(ordersByIdCache).evict(orderId.toString());
    }

    @Test
    void on_orderCreated_evictaLaEntradaPorId() {
        UUID orderId = UUID.randomUUID();
        when(cacheManager.getCache(OrderCacheNames.BY_ID)).thenReturn(ordersByIdCache);

        listener.on(new OrderCreated(OrderId.of(orderId), CustomerId.of(1L), "Cliente Test",
                Money.of(BigDecimal.TEN, Currency.getInstance("USD")), Instant.now()));

        verify(ordersByIdCache).evict(orderId.toString());
    }

    @Test
    void on_orderCancelled_evictaLaEntradaPorId() {
        UUID orderId = UUID.randomUUID();
        when(cacheManager.getCache(OrderCacheNames.BY_ID)).thenReturn(ordersByIdCache);

        listener.on(new OrderCancelled(OrderId.of(orderId), "Sin stock", Instant.now()));

        verify(ordersByIdCache).evict(orderId.toString());
    }

    @Test
    void on_noFalla_cuandoElCacheNoEstaRegistrado() {
        UUID orderId = UUID.randomUUID();
        when(cacheManager.getCache(OrderCacheNames.BY_ID)).thenReturn(null);

        listener.on(new OrderConfirmed(OrderId.of(orderId), Instant.now()));

        verify(ordersByIdCache, never()).evict(org.mockito.ArgumentMatchers.any());
    }
}
