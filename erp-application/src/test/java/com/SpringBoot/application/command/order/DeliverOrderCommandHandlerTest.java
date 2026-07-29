package com.SpringBoot.application.command.order;

import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.entity.OrderStatus;
import com.SpringBoot.domain.order.events.OrderDelivered;
import com.SpringBoot.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliverOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DeliverOrderCommandHandler handler;

    private static Order shippedOrder(UUID orderId) {
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
    void handle_marcaLaOrdenComoEntregada() {
        UUID orderId = UUID.randomUUID();
        Order entity = shippedOrder(orderId);
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(entity));

        handler.handle(new DeliverOrderCommand(orderId));

        verify(orderRepository).save(entity);
        assertThat(entity.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(eventPublisher).publishEvent(any(OrderDelivered.class));
    }

    @Test
    void handle_lanzaOrderNotFoundException_cuandoLaOrdenNoExiste() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new DeliverOrderCommand(orderId)))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }
}
