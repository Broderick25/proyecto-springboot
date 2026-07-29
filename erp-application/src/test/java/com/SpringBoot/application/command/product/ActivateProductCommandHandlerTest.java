package com.SpringBoot.application.command.product;

import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.product.events.ProductUpdated;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivateProductCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ActivateProductCommandHandler handler;

    @Test
    void handle_activaElProductoYPublicaProductUpdated() {
        UUID productId = UUID.randomUUID();
        Product entity = inactiveProductEntity(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(entity));

        handler.handle(new ActivateProductCommand(productId));

        assertThat(entity.getActive()).isTrue();
        verify(productRepository).save(entity);
        verify(eventPublisher).publishEvent(any(ProductUpdated.class));
    }

    @Test
    void handle_lanzaProductNotFoundException_cuandoElProductoNoExiste() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new ActivateProductCommand(productId)))
                .isInstanceOf(ProductNotFoundException.class);
    }

    private static Product inactiveProductEntity(UUID id) {
        Product entity = Product.builder()
                .id(id)
                .sku("LAPTOP-001")
                .name("Laptop Dell XPS 15")
                .description("Laptop de alto rendimiento")
                .price(new BigDecimal("1499.99"))
                .stock(10)
                .categoryId("cat-electronics")
                .active(false)
                .build();
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
        return entity;
    }
}
