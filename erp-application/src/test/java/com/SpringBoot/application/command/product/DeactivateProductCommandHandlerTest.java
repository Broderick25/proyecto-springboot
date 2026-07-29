package com.SpringBoot.application.command.product;

import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.product.events.ProductDeactivated;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateProductCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DeactivateProductCommandHandler handler;

    @Test
    void handle_desactivaElProductoYPublicaProductDeactivated() {
        UUID productId = UUID.randomUUID();
        Product entity = ProductMapperTest.productEntity(productId); // active: true
        when(productRepository.findById(productId)).thenReturn(Optional.of(entity));

        handler.handle(new DeactivateProductCommand(productId));

        assertThat(entity.getActive()).isFalse();
        verify(productRepository).save(entity);
        verify(eventPublisher).publishEvent(any(ProductDeactivated.class));
    }

    @Test
    void handle_lanzaProductNotFoundException_cuandoElProductoNoExiste() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new DeactivateProductCommand(productId)))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
