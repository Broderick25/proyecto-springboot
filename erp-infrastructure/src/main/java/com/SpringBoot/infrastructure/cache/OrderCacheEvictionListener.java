package com.SpringBoot.infrastructure.cache;

import com.SpringBoot.application.query.order.OrderCacheNames;
import com.SpringBoot.domain.order.OrderId;
import com.SpringBoot.domain.order.events.OrderCancelled;
import com.SpringBoot.domain.order.events.OrderConfirmed;
import com.SpringBoot.domain.order.events.OrderCreated;
import com.SpringBoot.domain.order.events.OrderDelivered;
import com.SpringBoot.domain.order.events.OrderShipped;
import com.SpringBoot.domain.order.events.OrderUpdated;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Invalida el caché de {@code GetOrderByIdQuery} cuando una orden cambia. A diferencia de
 * Product (que tiene {@code ProductProjection} sincronizando un modelo de lectura en Mongo),
 * Order no tiene modelo de lectura separado — las queries leen directo de Postgres — así que acá
 * no hay nada que sincronizar, solo invalidar la entrada cacheada por id.
 */
@Component
public class OrderCacheEvictionListener {

    private final CacheManager cacheManager;

    public OrderCacheEvictionListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderCreated event) {
        evict(event.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderUpdated event) {
        evict(event.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderConfirmed event) {
        evict(event.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderShipped event) {
        evict(event.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderDelivered event) {
        evict(event.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderCancelled event) {
        evict(event.orderId());
    }

    private void evict(OrderId orderId) {
        Cache cache = cacheManager.getCache(OrderCacheNames.BY_ID);
        if (cache != null) {
            cache.evict(orderId.value().toString());
        }
    }
}
