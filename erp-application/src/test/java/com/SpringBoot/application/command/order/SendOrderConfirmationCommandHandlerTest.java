package com.SpringBoot.application.command.order;

import com.SpringBoot.application.port.outbound.MailPort;
import com.SpringBoot.application.port.outbound.OrderConfirmationEmail;
import com.SpringBoot.domain.customer.CustomerNotFoundException;
import com.SpringBoot.domain.customer.CustomerProvider;
import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.entity.OrderProduct;
import com.SpringBoot.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendOrderConfirmationCommandHandlerTest {

    private static final String TEST_RECIPIENT = "test-recipient@example.com";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerProvider customerProvider;

    @Mock
    private MailPort mailPort;

    private SendOrderConfirmationCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SendOrderConfirmationCommandHandler(orderRepository, customerProvider, mailPort,
                TEST_RECIPIENT);
    }

    @Test
    void handle_envaiElCorreoAlDestinatarioDePrueba_cuandoOrdenYClienteExisten() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-1001")
                .customerId(7L)
                .customerName("Juan Pérez")
                .orderDate(LocalDateTime.of(2026, 7, 17, 10, 30))
                .totalAmount(new BigDecimal("129.90"))
                .currency("USD")
                .orderProducts(List.of(
                        OrderProduct.builder().id(UUID.randomUUID()).build(),
                        OrderProduct.builder().id(UUID.randomUUID()).build()))
                .build();

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        when(customerProvider.existsById(7L)).thenReturn(true);

        handler.handle(new SendOrderConfirmationCommand(orderId));

        ArgumentCaptor<OrderConfirmationEmail> captor = ArgumentCaptor.forClass(OrderConfirmationEmail.class);
        verify(mailPort).sendOrderConfirmation(captor.capture());

        OrderConfirmationEmail email = captor.getValue();
        assertThat(email.to()).isEqualTo(TEST_RECIPIENT);
        assertThat(email.customerName()).isEqualTo("Juan Pérez");
        assertThat(email.orderNumber()).isEqualTo("ORD-1001");
        assertThat(email.orderId()).isEqualTo(orderId.toString());
        assertThat(email.itemsCount()).isEqualTo(2);
        assertThat(email.currency()).isEqualTo("USD");
        assertThat(email.totalAmount()).isEqualByComparingTo("129.90");
    }

    @Test
    void handle_lanzaOrderNotFoundException_cuandoLaOrdenNoExiste() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new SendOrderConfirmationCommand(orderId)))
                .isInstanceOf(OrderNotFoundException.class);

        verify(mailPort, never()).sendOrderConfirmation(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handle_lanzaCustomerNotFoundException_cuandoElClienteNoExiste() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-1001")
                .customerId(7L)
                .customerName("Juan Pérez")
                .orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.TEN)
                .currency("USD")
                .orderProducts(List.of())
                .build();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        when(customerProvider.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> handler.handle(new SendOrderConfirmationCommand(orderId)))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(mailPort, never()).sendOrderConfirmation(org.mockito.ArgumentMatchers.any());
    }
}
