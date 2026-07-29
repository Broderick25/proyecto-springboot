package com.SpringBoot.application.command.order;

import com.SpringBoot.domain.customer.CustomerInfo;
import com.SpringBoot.domain.customer.CustomerNotFoundException;
import com.SpringBoot.domain.customer.CustomerProvider;
import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.order.events.OrderCreated;
import com.SpringBoot.domain.repository.OrderRepository;
import com.SpringBoot.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CreateOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerProvider customerProvider;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CreateOrderCommandHandler handler;

    @Test
    void handle_creaLaOrdenConSusLineas_yPublicaOrderCreated() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .sku("LAPTOP-001")
                .name("Laptop Dell XPS 15")
                .price(new BigDecimal("1499.99"))
                .stock(10)
                .categoryId("cat-electronics")
                .active(true)
                .build();
        ReflectionTestUtils.setField(product, "createdAt", LocalDateTime.now());
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(customerProvider.findById(7L)).thenReturn(Optional.of(
                new CustomerInfo(7L, "Juan Pérez", "juan@example.com", "555-0100",
                        "Calle Falsa 123", "Springfield", "00000", "ACME")));

        CreateOrderCommand command = new CreateOrderCommand(7L,
                List.of(new CreateOrderCommand.OrderLine(productId, 2)), "admin");

        UUID orderId = handler.handle(command);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order savedEntity = captor.getValue();
        assertThat(savedEntity.getId()).isEqualTo(orderId);
        assertThat(savedEntity.getCustomerId()).isEqualTo(7L);
        assertThat(savedEntity.getCustomerName()).isEqualTo("Juan Pérez");
        assertThat(savedEntity.getCreatedBy()).isEqualTo("admin");
        assertThat(savedEntity.getOrderProducts()).hasSize(1);
        assertThat(savedEntity.getOrderProducts().get(0).getProductName()).isEqualTo("Laptop Dell XPS 15");
        assertThat(savedEntity.getOrderProducts().get(0).getQuantity()).isEqualTo(2);
        assertThat(savedEntity.getTotalAmount()).isEqualByComparingTo("2999.98");

        verify(eventPublisher).publishEvent(any(OrderCreated.class));
    }

    @Test
    void handle_lanzaCustomerNotFoundException_cuandoElClienteNoExiste() {
        when(customerProvider.findById(7L)).thenReturn(Optional.empty());

        CreateOrderCommand command = new CreateOrderCommand(7L,
                List.of(new CreateOrderCommand.OrderLine(UUID.randomUUID(), 1)), "admin");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void handle_lanzaProductNotFoundException_cuandoUnProductoNoExiste() {
        UUID productId = UUID.randomUUID();
        when(customerProvider.findById(7L)).thenReturn(Optional.of(
                new CustomerInfo(7L, "Juan Pérez", "juan@example.com", "555-0100",
                        "Calle Falsa 123", "Springfield", "00000", "ACME")));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        CreateOrderCommand command = new CreateOrderCommand(7L,
                List.of(new CreateOrderCommand.OrderLine(productId, 1)), "admin");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ProductNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }
}
