package com.SpringBoot.application.command.order;

import com.SpringBoot.application.command.product.DecrementProductStockCommand;
import com.SpringBoot.application.command.product.DecrementProductStockCommandHandler;
import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.entity.OrderProduct;
import com.SpringBoot.domain.entity.OrderStatus;
import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.order.events.OrderConfirmed;
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
class ConfirmOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DecrementProductStockCommandHandler decrementProductStockCommandHandler;

    @InjectMocks
    private ConfirmOrderCommandHandler handler;

    @Test
    void handle_confirmaLaOrdenYDecrementaElStockDeCadaItem() {
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
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("2999.98"))
                .currency("USD")
                .orderProducts(List.of(item))
                .build();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(entity));

        handler.handle(new ConfirmOrderCommand(orderId));

        ArgumentCaptor<DecrementProductStockCommand> captor =
                ArgumentCaptor.forClass(DecrementProductStockCommand.class);
        verify(decrementProductStockCommandHandler).handle(captor.capture());
        assertThat(captor.getValue().productId()).isEqualTo(productId);
        assertThat(captor.getValue().quantity()).isEqualTo(2);

        verify(orderRepository).save(entity);
        assertThat(entity.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(eventPublisher).publishEvent(any(OrderConfirmed.class));
    }

    @Test
    void handle_lanzaOrderNotFoundException_cuandoLaOrdenNoExiste() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new ConfirmOrderCommand(orderId)))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, never()).save(any());
        verify(decrementProductStockCommandHandler, never()).handle(any());
    }
}
