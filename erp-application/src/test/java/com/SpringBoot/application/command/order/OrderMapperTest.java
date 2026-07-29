package com.SpringBoot.application.command.order;

import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderProduct;
import com.SpringBoot.domain.entity.OrderStatus;
import com.SpringBoot.domain.entity.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    @Test
    void toAggregate_mapeaLaOrdenYSusItems() {
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
                .orderDate(LocalDateTime.of(2025, 1, 15, 10, 30))
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("2999.98"))
                .currency("USD")
                .orderProducts(List.of(item))
                .build();

        com.SpringBoot.domain.order.Order aggregate = OrderMapper.toAggregate(entity);

        assertThat(aggregate.getId().value()).isEqualTo(orderId);
        assertThat(aggregate.getOrderNumber().value()).isEqualTo("ORD-2025-001");
        assertThat(aggregate.getCustomer().customerId().value()).isEqualTo(7L);
        assertThat(aggregate.getCustomer().customerName()).isEqualTo("Juan Pérez");
        assertThat(aggregate.getStatus().value()).isEqualTo("CONFIRMED");
        assertThat(aggregate.getTotalAmount().amount()).isEqualByComparingTo("2999.98");

        assertThat(aggregate.getItems()).hasSize(1);
        assertThat(aggregate.getItems().get(0).getProductName()).isEqualTo("Laptop Dell XPS 15");
        assertThat(aggregate.getItems().get(0).getProductReference().value()).isEqualTo(productId);
        assertThat(aggregate.getItems().get(0).getQuantity().value()).isEqualTo(2);
        assertThat(aggregate.getItems().get(0).getUnitPrice().amount()).isEqualByComparingTo("1499.99");
    }

    @Test
    void copyToEntity_actualizaElStatus_cuandoElAgregadoCambiaDeEstado() {
        Order entity = Order.builder()
                .id(UUID.randomUUID())
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

        com.SpringBoot.domain.order.Order aggregate = OrderMapper.toAggregate(entity);
        aggregate.confirm();

        OrderMapper.copyToEntity(aggregate, entity);

        assertThat(entity.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }
}
