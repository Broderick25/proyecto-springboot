package com.SpringBoot.application.command.product;

import com.SpringBoot.domain.document.CategoryNotFoundException;
import com.SpringBoot.domain.product.events.ProductCreated;
import com.SpringBoot.domain.repository.CatalogRepository;
import com.SpringBoot.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CatalogRepository catalogRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CreateProductCommandHandler handler;

    @Test
    void handle_creaElProductoYPublicaProductCreated() {
        lenient().when(catalogRepository.existsByCatalogTypeAndItemsId("PRODUCT_CATEGORIES", "cat-electronics"))
                .thenReturn(true);
        CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-001", "Laptop Dell XPS 15", "Laptop de alto rendimiento",
                new BigDecimal("1499.99"), 25, "cat-electronics", null, "admin");

        UUID productId = handler.handle(command);

        ArgumentCaptor<com.SpringBoot.domain.entity.Product> captor =
                ArgumentCaptor.forClass(com.SpringBoot.domain.entity.Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(productId);
        assertThat(captor.getValue().getSku()).isEqualTo("LAPTOP-001");
        assertThat(captor.getValue().getStock()).isEqualTo(25);
        assertThat(captor.getValue().getActive()).isTrue();

        verify(eventPublisher).publishEvent(any(ProductCreated.class));
    }

    @Test
    void handle_lanzaCategoryNotFoundException_cuandoLaCategoriaNoExiste() {
        when(catalogRepository.existsByCatalogTypeAndItemsId("PRODUCT_CATEGORIES", "cat-inexistente"))
                .thenReturn(false);
        CreateProductCommand command = new CreateProductCommand(
                "LAPTOP-001", "Laptop Dell XPS 15", "Laptop de alto rendimiento",
                new BigDecimal("1499.99"), 25, "cat-inexistente", null, "admin");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(productRepository, never()).save(any());
    }
}
