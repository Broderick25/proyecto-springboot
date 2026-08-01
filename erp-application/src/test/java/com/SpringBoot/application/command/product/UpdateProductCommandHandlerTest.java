package com.SpringBoot.application.command.product;

import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.entity.ProductInactiveException;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.product.events.ProductUpdated;
import com.SpringBoot.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateProductCommandHandler handler;

    @Test
    void handle_actualizaElProductoYPublicaProductUpdated() {
        UUID productId = UUID.randomUUID();
        Product entity = ProductMapperTest.productEntity(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(entity));

        UpdateProductCommand command = new UpdateProductCommand(
                productId, "Laptop Dell XPS 15 Plus", "Nueva descripción",
                new BigDecimal("1999.99"), "cat-electronics", null);

        handler.handle(command);

        assertThat(entity.getName()).isEqualTo("Laptop Dell XPS 15 Plus");
        assertThat(entity.getDescription()).isEqualTo("Nueva descripción");
        assertThat(entity.getPrice()).isEqualByComparingTo("1999.99");
        verify(productRepository).save(entity);
        verify(eventPublisher).publishEvent(any(ProductUpdated.class));
    }

    @Test
    void handle_lanzaProductNotFoundException_cuandoElProductoNoExiste() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        UpdateProductCommand command = new UpdateProductCommand(
                productId, "x", "y", BigDecimal.TEN, "cat-electronics", null);

        assertThatThrownBy(() -> handler.handle(command)).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void handle_lanzaProductInactiveException_cuandoElProductoEstaDesactivado() {
        UUID productId = UUID.randomUUID();
        Product entity = ProductMapperTest.productEntity(productId);
        entity.setActive(false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(entity));

        UpdateProductCommand command = new UpdateProductCommand(
                productId, "x", "y", BigDecimal.TEN, "cat-electronics", null);

        assertThatThrownBy(() -> handler.handle(command)).isInstanceOf(ProductInactiveException.class);

        verify(productRepository, never()).save(any());
    }
}
