package com.SpringBoot.application.command.product;

import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.product.events.StockChanged;
import com.SpringBoot.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecrementProductStockCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DecrementProductStockCommandHandler handler;

    @Test
    void handle_decrementaElStockYPublicaStockChanged() {
        UUID productId = UUID.randomUUID();
        Product entity = ProductMapperTest.productEntity(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(entity));

        handler.handle(new DecrementProductStockCommand(productId, 4, "venta"));

        assertThat(entity.getStock()).isEqualTo(6);
        verify(productRepository).save(entity);
        verify(eventPublisher).publishEvent(any(StockChanged.class));
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoNoHayStockSuficiente() {
        UUID productId = UUID.randomUUID();
        Product entity = ProductMapperTest.productEntity(productId); // stock inicial: 10
        when(productRepository.findById(productId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> handler.handle(new DecrementProductStockCommand(productId, 20, "venta")))
                .isInstanceOf(IllegalArgumentException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void handle_lanzaProductNotFoundException_cuandoElProductoNoExiste() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new DecrementProductStockCommand(productId, 1, "venta")))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
