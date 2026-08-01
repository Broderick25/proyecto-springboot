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
class ChangeProductPriceCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ChangeProductPriceCommandHandler handler;

    @Test
    void handle_cambiaElPrecioYPublicaProductUpdated() {
        UUID productId = UUID.randomUUID();
        Product entity = ProductMapperTest.productEntity(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(entity));

        handler.handle(new ChangeProductPriceCommand(productId, new BigDecimal("1299.50")));

        assertThat(entity.getPrice()).isEqualByComparingTo("1299.50");
        verify(productRepository).save(entity);
        verify(eventPublisher).publishEvent(any(ProductUpdated.class));
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoElPrecioNoEsPositivo() {
        UUID productId = UUID.randomUUID();
        Product entity = ProductMapperTest.productEntity(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> handler.handle(new ChangeProductPriceCommand(productId, BigDecimal.ZERO)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handle_lanzaProductNotFoundException_cuandoElProductoNoExiste() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new ChangeProductPriceCommand(productId, BigDecimal.TEN)))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void handle_lanzaProductInactiveException_cuandoElProductoEstaDesactivado() {
        UUID productId = UUID.randomUUID();
        Product entity = ProductMapperTest.productEntity(productId);
        entity.setActive(false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> handler.handle(new ChangeProductPriceCommand(productId, BigDecimal.TEN)))
                .isInstanceOf(ProductInactiveException.class);

        verify(productRepository, never()).save(any());
    }
}
