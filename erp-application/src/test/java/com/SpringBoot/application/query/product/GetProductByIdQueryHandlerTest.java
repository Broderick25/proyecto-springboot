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
class GetProductByIdQueryHandlerTest {

    @Mock
    private ProductDocumentRepository productDocumentRepository;

    @InjectMocks
    private GetProductByIdQueryHandler handler;

    @Test
    void handle_devuelveElProductView_cuandoElDocumentoExiste() {
        UUID id = UUID.randomUUID();
        ProductDocument document = ProductDocument.builder()
                .id(id.toString())
                .sku("LAPTOP-001")
                .name("Laptop Dell XPS 15")
                .price(new BigDecimal("1499.99"))
                .currency("USD")
                .stock(25)
                .categoryId("cat-electronics")
                .categoryName("Electronics")
                .active(true)
                .build();
        when(productDocumentRepository.findById(id.toString())).thenReturn(Optional.of(document));

        ProductView view = handler.handle(new GetProductByIdQuery(id));

        assertThat(view.id()).isEqualTo(id.toString());
        assertThat(view.sku()).isEqualTo("LAPTOP-001");
        assertThat(view.price()).isEqualByComparingTo("1499.99");
        assertThat(view.categoryName()).isEqualTo("Electronics");
        assertThat(view.active()).isTrue();
    }

    @Test
    void handle_lanzaProductNotFoundException_cuandoNoExisteElDocumento() {
        UUID id = UUID.randomUUID();
        when(productDocumentRepository.findById(id.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetProductByIdQuery(id)))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
