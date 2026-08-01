package com.SpringBoot.application.command.order;

import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.entity.OrderProduct;
import com.SpringBoot.domain.entity.OrderStatus;
import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.order.events.OrderUpdated;
import com.SpringBoot.domain.repository.OrderRepository;
import com.SpringBoot.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

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
class UpdateOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateOrderCommandHandler handler;

    private static Product productEntity(UUID id, String name, BigDecimal price) {
        Product product = Product.builder()
                .id(id)
                .sku("LAPTOP-001")
                .name(name)
                .price(price)
                .stock(10)
                .categoryId("cat-electronics")
                .active(true)
                .build();
        ReflectionTestUtils.setField(product, "createdAt", LocalDateTime.now());
        return product;
    }

    private static Order pendingOrder(UUID orderId, List<OrderProduct> items) {
        return Order.builder()
                .id(orderId)
                .orderNumber("ORD-2025-001")
                .customerId(7L)
                .customerName("Juan Pérez")
                .createdBy("admin")
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("1499.99"))
                .currency("USD")
                .orderProducts(items)
                .build();
    }

    @Test
    void handle_reemplazaLasLineasYRecalculaElTotal() {
        UUID orderId = UUID.randomUUID();
        UUID oldProductId = UUID.randomUUID();
        UUID newProductId = UUID.randomUUID();

        OrderProduct oldItem = OrderProduct.builder()
                .id(UUID.randomUUID())
                .product(productEntity(oldProductId, "Laptop Dell XPS 15", new BigDecimal("1499.99")))
                .productName("Laptop Dell XPS 15")
                .quantity(1)
                .unitPrice(new BigDecimal("1499.99"))
                .subtotal(new BigDecimal("1499.99"))
                .build();
        Order entity = pendingOrder(orderId, new java.util.ArrayList<>(List.of(oldItem)));
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(entity));

        Product newProduct = productEntity(newProductId, "Mouse Logitech", new BigDecimal("99.99"));
        when(productRepository.findById(newProductId)).thenReturn(Optional.of(newProduct));

        handler.handle(new UpdateOrderCommand(orderId, List.of(new UpdateOrderCommand.OrderLine(newProductId, 3))));

        verify(orderRepository).save(entity);
        assertThat(entity.getOrderProducts()).hasSize(1);
        assertThat(entity.getOrderProducts().get(0).getProductName()).isEqualTo("Mouse Logitech");
        assertThat(entity.getOrderProducts().get(0).getQuantity()).isEqualTo(3);
        assertThat(entity.getTotalAmount()).isEqualByComparingTo("299.97");
        verify(eventPublisher).publishEvent(any(OrderUpdated.class));
    }

    @Test
    void handle_lanzaIllegalStateException_cuandoLaOrdenYaNoEstaPendiente() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Order entity = pendingOrder(orderId, new java.util.ArrayList<>());
        entity.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> handler.handle(
                new UpdateOrderCommand(orderId, List.of(new UpdateOrderCommand.OrderLine(productId, 1)))))
                .isInstanceOf(IllegalStateException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoItemsEsNull() {
        UUID orderId = UUID.randomUUID();

        assertThatThrownBy(() -> handler.handle(new UpdateOrderCommand(orderId, null)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(orderRepository, never()).findByIdWithItems(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void handle_lanzaOrderNotFoundException_cuandoLaOrdenNoExiste() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new UpdateOrderCommand(orderId, List.of())))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void handle_lanzaProductNotFoundException_cuandoUnProductoNoExiste() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Order entity = pendingOrder(orderId, new java.util.ArrayList<>());
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(entity));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(
                new UpdateOrderCommand(orderId, List.of(new UpdateOrderCommand.OrderLine(productId, 1)))))
                .isInstanceOf(ProductNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }
}
