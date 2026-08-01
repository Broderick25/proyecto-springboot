package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.SliceView;
import com.SpringBoot.application.query.product.view.ProductView;
import com.SpringBoot.domain.document.ProductDocument;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchProductsQueryHandlerTest {

    @Mock
    private ProductDocumentRepository productDocumentRepository;

    @InjectMocks
    private SearchProductsQueryHandler handler;

    @Test
    void handle_devuelveLosProductViewsEncontrados() {
        ProductDocument document = ProductDocument.builder()
                .id(UUID.randomUUID().toString())
                .sku("LAPTOP-001")
                .name("Laptop Dell XPS 15")
                .price(new BigDecimal("1499.99"))
                .currency("USD")
                .stock(25)
                .active(true)
                .build();
        Pageable pageable = PageRequest.of(0, 20);
        when(productDocumentRepository.searchByText("intel", pageable))
                .thenReturn(new SliceImpl<>(List.of(document), pageable, false));

        SliceView<ProductView> result = handler.handle(new SearchProductsQuery("intel", 0, 20));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).sku()).isEqualTo("LAPTOP-001");
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void handle_indicaHasNext_cuandoHayMasResultados() {
        ProductDocument document = ProductDocument.builder()
                .id(UUID.randomUUID().toString())
                .sku("LAPTOP-001")
                .name("Laptop Dell XPS 15")
                .price(new BigDecimal("1499.99"))
                .currency("USD")
                .stock(25)
                .active(true)
                .build();
        Pageable pageable = PageRequest.of(0, 1);
        when(productDocumentRepository.searchByText("intel", pageable))
                .thenReturn(new SliceImpl<>(List.of(document), pageable, true));

        SliceView<ProductView> result = handler.handle(new SearchProductsQuery("intel", 0, 1));

        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoElTextoEsBlanco() {
        assertThatThrownBy(() -> handler.handle(new SearchProductsQuery("  ", 0, 20)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoSizeExcedeElMaximo() {
        assertThatThrownBy(() -> handler.handle(new SearchProductsQuery("intel", 0, 101)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
