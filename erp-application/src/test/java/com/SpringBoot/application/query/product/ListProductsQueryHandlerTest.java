package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.product.view.ProductView;
import com.SpringBoot.domain.document.ProductDocument;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProductsQueryHandlerTest {

    @Mock
    private ProductDocumentRepository productDocumentRepository;

    @InjectMocks
    private ListProductsQueryHandler handler;

    private static ProductDocument document(boolean active) {
        return ProductDocument.builder()
                .id(UUID.randomUUID().toString())
                .sku("LAPTOP-001")
                .name("Laptop Dell XPS 15")
                .price(new BigDecimal("1499.99"))
                .currency("USD")
                .stock(25)
                .categoryId("cat-electronics")
                .active(active)
                .build();
    }

    @Test
    void handle_llamaFindByActiveTrue_cuandoOnlyActiveEsTrue() {
        when(productDocumentRepository.findByActiveTrue()).thenReturn(List.of(document(true)));

        List<ProductView> views = handler.handle(new ListProductsQuery(true));

        assertThat(views).hasSize(1);
        verify(productDocumentRepository, never()).findAll();
    }

    @Test
    void handle_llamaFindAll_cuandoOnlyActiveEsFalse() {
        when(productDocumentRepository.findAll()).thenReturn(List.of(document(true), document(false)));

        List<ProductView> views = handler.handle(new ListProductsQuery(false));

        assertThat(views).hasSize(2);
        verify(productDocumentRepository, never()).findByActiveTrue();
    }
}
