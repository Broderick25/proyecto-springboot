package com.SpringBoot.application.query.order;

import com.SpringBoot.application.query.order.view.OrderView;
import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.entity.OrderProduct;
import com.SpringBoot.domain.entity.OrderStatus;
import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrderByIdQueryHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private GetOrderByIdQueryHandler handler;

    @Test
    void handle_devuelveElOrderViewConSusItems() {
        UUID orderId = UUID.randomUUID();
        Product product = Product.builder().id(UUID.randomUUID()).build();
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
                .orderDate(LocalDateTime.of(2025, 1, 15, 10, 30))
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("2999.98"))
                .currency("USD")
                .orderProducts(List.of(item))
                .build();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(entity));

        OrderView view = handler.handle(new GetOrderByIdQuery(orderId));

        assertThat(view.id()).isEqualTo(orderId);
        assertThat(view.orderNumber()).isEqualTo("ORD-2025-001");
        assertThat(view.status()).isEqualTo("CONFIRMED");
        assertThat(view.items()).hasSize(1);
        assertThat(view.items().get(0).productName()).isEqualTo("Laptop Dell XPS 15");
        assertThat(view.items().get(0).quantity()).isEqualTo(2);
    }

    @Test
    void handle_lanzaOrderNotFoundException_cuandoLaOrdenNoExiste() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetOrderByIdQuery(orderId)))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
