package com.SpringBoot.application.command.order;

import com.SpringBoot.application.command.product.IncrementProductStockCommand;
import com.SpringBoot.application.command.product.IncrementProductStockCommandHandler;
import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.entity.OrderProduct;
import com.SpringBoot.domain.entity.OrderStatus;
import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.order.events.OrderCancelled;
import com.SpringBoot.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CancelOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private IncrementProductStockCommandHandler incrementProductStockCommandHandler;

    @InjectMocks
    private CancelOrderCommandHandler handler;

    @Test
    void handle_liberaElStock_cuandoLaOrdenCanceladaEstabaConfirmada() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Product product = Product.builder().id(productId).build();
        OrderProduct item = OrderProduct.builder()
                .id(UUID.randomUUID())
                .product(product)
                .productName("Laptop Dell XPS 15")
                .quantity(2)
                .unitPrice(new BigDecimal("1499.99"))
                .subtotal(new BigDecimal("2999.98"))
                .build();
        Order entity = Order.builder()
                .id(orderId)
                .orderNumber("ORD-2025-001")
                .customerId(7L)
                .customerName("Juan Pérez")
                .createdBy("admin")
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("2999.98"))
                .currency("USD")
                .orderProducts(List.of(item))
                .build();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(entity));

        handler.handle(new CancelOrderCommand(orderId, "El cliente ya no lo quiere"));

        ArgumentCaptor<IncrementProductStockCommand> captor =
                ArgumentCaptor.forClass(IncrementProductStockCommand.class);
        verify(incrementProductStockCommandHandler).handle(captor.capture());
        assertThat(captor.getValue().productId()).isEqualTo(productId);
        assertThat(captor.getValue().quantity()).isEqualTo(2);

        assertThat(entity.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(entity.getCancellationReason()).isEqualTo("El cliente ya no lo quiere");
        verify(eventPublisher).publishEvent(any(OrderCancelled.class));
    }

    @Test
    void handle_noTocaElStock_cuandoLaOrdenCanceladaEstabaPendiente() {
        UUID orderId = UUID.randomUUID();
        Order entity = Order.builder()
                .id(orderId)
                .orderNumber("ORD-2025-001")
                .customerId(7L)
                .customerName("Juan Pérez")
                .createdBy("admin")
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.TEN)
                .currency("USD")
                .orderProducts(List.of())
                .build();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(entity));

        handler.handle(new CancelOrderCommand(orderId, "El cliente ya no lo quiere"));

        assertThat(entity.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(incrementProductStockCommandHandler, never()).handle(any());
    }

    @Test
    void handle_lanzaOrderNotFoundException_cuandoLaOrdenNoExiste() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new CancelOrderCommand(orderId, "motivo")))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }
}
