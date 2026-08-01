package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.PageView;
import com.SpringBoot.application.query.product.view.ProductOrderHistoryView;
import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderProduct;
import com.SpringBoot.domain.entity.OrderStatus;
import com.SpringBoot.domain.repository.OrderProductRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrdersByProductQueryHandlerTest {

    @Mock
    private OrderProductRepository orderProductRepository;

    @InjectMocks
    private GetOrdersByProductQueryHandler handler;

    private static OrderProduct orderProduct(UUID orderId) {
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-001")
                .customerId(1L)
                .customerName("Cliente Test")
                .createdBy("system")
                .status(OrderStatus.CONFIRMED)
                .orderDate(LocalDateTime.of(2026, 1, 1, 10, 0))
                .totalAmount(BigDecimal.TEN)
                .build();

        return OrderProduct.builder()
                .id(UUID.randomUUID())
                .order(order)
                .productName("Laptop Dell XPS 15")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(500))
                .subtotal(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    void handle_devuelveElHistorialDeOrdenesDelProducto() {
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<OrderProduct> page = new PageImpl<>(List.of(orderProduct(orderId)), pageable, 1);
        when(orderProductRepository.findByProductIdWithOrder(productId, pageable)).thenReturn(page);

        PageView<ProductOrderHistoryView> result = handler.handle(new GetOrdersByProductQuery(productId, 0, 20));

        assertThat(result.content()).hasSize(1);
        ProductOrderHistoryView view = result.content().get(0);
        assertThat(view.orderId()).isEqualTo(orderId);
        assertThat(view.orderNumber()).isEqualTo("ORD-001");
        assertThat(view.orderStatus()).isEqualTo("CONFIRMED");
        assertThat(view.quantity()).isEqualTo(2);
        assertThat(view.subtotal()).isEqualByComparingTo("1000");
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoSizeExcedeElMaximo() {
        assertThatThrownBy(() -> handler.handle(new GetOrdersByProductQuery(UUID.randomUUID(), 0, 101)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
