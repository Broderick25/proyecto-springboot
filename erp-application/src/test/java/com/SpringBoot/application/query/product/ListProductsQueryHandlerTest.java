package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.PageView;
import com.SpringBoot.application.query.product.view.ProductView;
import com.SpringBoot.domain.document.ProductDocument;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
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

    private static Page<ProductDocument> pageOf(Pageable pageable, ProductDocument... documents) {
        return new PageImpl<>(List.of(documents), pageable, documents.length);
    }

    @Test
    void handle_llamaFindByActiveTrue_cuandoOnlyActiveEsTrue() {
        Pageable pageable = PageRequest.of(0, 20);
        when(productDocumentRepository.findByActiveTrue(pageable)).thenReturn(pageOf(pageable, document(true)));

        PageView<ProductView> result = handler.handle(new ListProductsQuery(true, null, 0, 20));

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        verify(productDocumentRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void handle_llamaFindAll_cuandoOnlyActiveEsFalse() {
        Pageable pageable = PageRequest.of(0, 20);
        when(productDocumentRepository.findAll(pageable))
                .thenReturn(pageOf(pageable, document(true), document(false)));

        PageView<ProductView> result = handler.handle(new ListProductsQuery(false, null, 0, 20));

        assertThat(result.content()).hasSize(2);
        verify(productDocumentRepository, never()).findByActiveTrue(any(Pageable.class));
    }

    @Test
    void handle_llamaFindByCategoryId_cuandoSeIndicaCategoryId() {
        Pageable pageable = PageRequest.of(0, 20);
        when(productDocumentRepository.findByCategoryId("cat-electronics", pageable))
                .thenReturn(pageOf(pageable, document(true), document(false)));

        PageView<ProductView> result = handler.handle(new ListProductsQuery(false, "cat-electronics", 0, 20));

        assertThat(result.content()).hasSize(2);
        verify(productDocumentRepository, never()).findByActiveTrue(any(Pageable.class));
        verify(productDocumentRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void handle_llamaFindByCategoryIdAndActiveTrue_cuandoSeIndicaCategoryIdYOnlyActive() {
        Pageable pageable = PageRequest.of(0, 20);
        when(productDocumentRepository.findByCategoryIdAndActiveTrue("cat-electronics", pageable))
                .thenReturn(pageOf(pageable, document(true)));

        PageView<ProductView> result = handler.handle(new ListProductsQuery(true, "cat-electronics", 0, 20));

        assertThat(result.content()).hasSize(1);
        verify(productDocumentRepository, never()).findByCategoryId(any(), any(Pageable.class));
    }

    @Test
    void handle_propagaPageYSizeDelPageable() {
        Pageable pageable = PageRequest.of(2, 5);
        when(productDocumentRepository.findAll(pageable)).thenReturn(pageOf(pageable, document(true)));

        PageView<ProductView> result = handler.handle(new ListProductsQuery(false, null, 2, 5));

        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(5);
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoSizeExcedeElMaximo() {
        assertThatThrownBy(() -> handler.handle(new ListProductsQuery(false, null, 0, 101)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
