package com.SpringBoot.infrastructure.notification;

import com.SpringBoot.application.port.outbound.MailPort;
import com.SpringBoot.application.port.outbound.OrderCancelledEmail;
import com.SpringBoot.application.port.outbound.OrderDeliveredEmail;
import com.SpringBoot.application.port.outbound.OrderShippedEmail;
import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderStatus;
import com.SpringBoot.domain.order.OrderId;
import com.SpringBoot.domain.order.events.OrderCancelled;
import com.SpringBoot.domain.order.events.OrderDelivered;
import com.SpringBoot.domain.order.events.OrderShipped;
import com.SpringBoot.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderNotificationListenerTest {

    private static final String TEST_RECIPIENT = "test-recipient@example.com";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MailPort mailPort;

    private OrderNotificationListener listener;

    @BeforeEach
    void setUp() {
        listener = new OrderNotificationListener(orderRepository, mailPort, TEST_RECIPIENT);
    }

    private static Order order(UUID orderId) {
        return Order.builder()
                .id(orderId)
                .orderNumber("ORD-2025-001")
                .customerId(7L)
                .customerName("Juan Pérez")
                .createdBy("admin")
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.SHIPPED)
                .totalAmount(BigDecimal.TEN)
                .currency("USD")
                .orderProducts(List.of())
                .build();
    }

    @Test
    void on_orderShipped_enviaElCorreoDeEnvio() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order(orderId)));

        listener.on(new OrderShipped(OrderId.of(orderId), Instant.now()));

        verify(mailPort).sendOrderShipped(new OrderShippedEmail(TEST_RECIPIENT, "Juan Pérez", "ORD-2025-001"));
    }

    @Test
    void on_orderDelivered_enviaElCorreoDeEntrega() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order(orderId)));

        listener.on(new OrderDelivered(OrderId.of(orderId), Instant.now()));

        verify(mailPort).sendOrderDelivered(new OrderDeliveredEmail(TEST_RECIPIENT, "Juan Pérez", "ORD-2025-001"));
    }

    @Test
    void on_orderCancelled_enviaElCorreoConElMotivo() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order(orderId)));

        listener.on(new OrderCancelled(OrderId.of(orderId), "Sin stock", Instant.now()));

        verify(mailPort).sendOrderCancelled(
                new OrderCancelledEmail(TEST_RECIPIENT, "Juan Pérez", "ORD-2025-001", "Sin stock"));
    }

    @Test
    void on_noEnviaNada_cuandoLaOrdenYaNoExiste() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        listener.on(new OrderShipped(OrderId.of(orderId), Instant.now()));

        verify(mailPort, never()).sendOrderShipped(org.mockito.ArgumentMatchers.any());
    }
}
