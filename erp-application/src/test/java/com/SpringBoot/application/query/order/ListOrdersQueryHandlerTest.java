package com.SpringBoot.application.query.order;

import com.SpringBoot.application.query.PageView;
import com.SpringBoot.application.query.order.view.OrderSummaryView;
import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderStatus;
import com.SpringBoot.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOrdersQueryHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ListOrdersQueryHandler handler;

    private static Order order(OrderStatus status) {
        return Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-001")
                .customerId(1L)
                .customerName("Cliente Test")
                .createdBy("system")
                .status(status)
                .totalAmount(BigDecimal.TEN)
                .build();
    }

    private static Page<Order> pageOf(Pageable pageable, Order... orders) {
        return new PageImpl<>(List.of(orders), pageable, orders.length);
    }

    @Test
    void handle_llamaFindAll_cuandoNoHayFiltros() {
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findAll(pageable)).thenReturn(pageOf(pageable, order(OrderStatus.PENDING)));

        PageView<OrderSummaryView> result = handler.handle(new ListOrdersQuery(null, null, 0, 20));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).status()).isEqualTo("PENDING");
    }

    @Test
    void handle_llamaFindByCustomerId_cuandoSeIndicaCustomerId() {
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(pageOf(pageable, order(OrderStatus.PENDING)));

        PageView<OrderSummaryView> result = handler.handle(new ListOrdersQuery(1L, null, 0, 20));

        assertThat(result.content()).hasSize(1);
        verify(orderRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void handle_llamaFindByStatus_cuandoSeIndicaStatus() {
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findByStatus(OrderStatus.CONFIRMED, pageable))
                .thenReturn(pageOf(pageable, order(OrderStatus.CONFIRMED)));

        PageView<OrderSummaryView> result = handler.handle(new ListOrdersQuery(null, "confirmed", 0, 20));

        assertThat(result.content()).hasSize(1);
        verify(orderRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void handle_llamaFindByCustomerIdAndStatus_cuandoSeIndicanAmbos() {
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findByCustomerIdAndStatus(1L, OrderStatus.SHIPPED, pageable))
                .thenReturn(pageOf(pageable, order(OrderStatus.SHIPPED)));

        PageView<OrderSummaryView> result = handler.handle(new ListOrdersQuery(1L, "SHIPPED", 0, 20));

        assertThat(result.content()).hasSize(1);
        verify(orderRepository, never()).findByCustomerId(any(), any(Pageable.class));
        verify(orderRepository, never()).findByStatus(any(), any(Pageable.class));
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoStatusEsInvalido() {
        assertThatThrownBy(() -> handler.handle(new ListOrdersQuery(null, "NO_EXISTE", 0, 20)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoSizeExcedeElMaximo() {
        assertThatThrownBy(() -> handler.handle(new ListOrdersQuery(null, null, 0, 101)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
