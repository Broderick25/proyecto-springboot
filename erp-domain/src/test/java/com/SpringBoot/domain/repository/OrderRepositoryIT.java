package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderProduct;
import com.SpringBoot.domain.entity.OrderStatus;
import com.SpringBoot.domain.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class OrderRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findByIdWithItems_traeOrderProductsSinLazyInitializationException() {
        Product product = productRepository.save(Product.builder()
                .id(UUID.randomUUID())
                .sku("SKU-TEST")
                .name("Producto Test")
                .price(BigDecimal.TEN)
                .stock(5)
                .build());

        Order order = orderRepository.save(Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-001")
                .customerId(1L)
                .customerName("Cliente Test")
                .createdBy("system")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.TEN)
                .build());

        order.addOrderProduct(OrderProduct.builder()
                .product(product)
                .productName(product.getName())
                .quantity(1)
                .unitPrice(BigDecimal.TEN)
                .subtotal(BigDecimal.TEN)
                .build());
        orderRepository.save(order);

        Optional<Order> found = orderRepository.findByIdWithItems(order.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getOrderProducts()).hasSize(1); // no lanza LazyInitializationException
    }

    @Test
    void findByOrderNumber_encuentraPorNumeroUnico() {
        orderRepository.save(Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-UNICO")
                .customerId(1L)
                .customerName("Cliente")
                .createdBy("system")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ONE)
                .build());

        Optional<Order> found = orderRepository.findByOrderNumber("ORD-UNICO");

        assertThat(found).isPresent();
    }

    @Test
    void findByStatus_filtraPorEnumTipado() {
        orderRepository.save(Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-STATUS")
                .customerId(1L)
                .customerName("Cliente")
                .createdBy("system")
                .status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.ONE)
                .build());

        var results = orderRepository.findByStatus(OrderStatus.CONFIRMED);

        assertThat(results).isNotEmpty();
    }
}
