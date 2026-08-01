package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.product.view.ProductView;
import com.SpringBoot.domain.document.ProductDocument;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductBySkuQueryHandlerTest {

    @Mock
    private ProductDocumentRepository productDocumentRepository;

    @InjectMocks
    private GetProductBySkuQueryHandler handler;

    @Test
    void handle_devuelveElProductView_cuandoElSkuExiste() {
        UUID id = UUID.randomUUID();
        ProductDocument document = ProductDocument.builder()
                .id(id.toString())
                .sku("LAPTOP-001")
                .name("Laptop Dell XPS 15")
                .price(new BigDecimal("1499.99"))
                .currency("USD")
                .stock(25)
                .active(true)
                .build();
        when(productDocumentRepository.findBySku("LAPTOP-001")).thenReturn(Optional.of(document));

        ProductView view = handler.handle(new GetProductBySkuQuery("LAPTOP-001"));

        assertThat(view.id()).isEqualTo(id.toString());
        assertThat(view.sku()).isEqualTo("LAPTOP-001");
    }

    @Test
    void handle_lanzaProductNotFoundException_cuandoNoExisteElSku() {
        when(productDocumentRepository.findBySku("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetProductBySkuQuery("UNKNOWN")))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoElSkuEsBlanco() {
        assertThatThrownBy(() -> handler.handle(new GetProductBySkuQuery("  ")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoElSkuEsNull() {
        assertThatThrownBy(() -> handler.handle(new GetProductBySkuQuery(null)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
