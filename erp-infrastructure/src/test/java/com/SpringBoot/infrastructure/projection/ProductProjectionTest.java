package com.SpringBoot.infrastructure.projection;

import com.SpringBoot.domain.document.Catalog;
import com.SpringBoot.domain.document.CatalogItem;
import com.SpringBoot.domain.document.ProductDocument;
import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.product.ProductId;
import com.SpringBoot.domain.product.events.ProductCreated;
import com.SpringBoot.domain.product.events.ProductDeactivated;
import com.SpringBoot.domain.repository.CatalogRepository;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
import com.SpringBoot.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductProjectionTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductDocumentRepository productDocumentRepository;

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private ProductProjection projection;

    private static Product productEntity(UUID id) {
        return Product.builder()
                .id(id)
                .sku("LAPTOP-001")
                .name("Laptop Dell XPS 15")
                .description("Laptop de alto rendimiento")
                .price(new BigDecimal("1499.99"))
                .stock(25)
                .categoryId("cat-electronics")
                .active(true)
                .build();
    }

    private static Catalog productCategoriesCatalog() {
        return Catalog.builder()
                .catalogType("PRODUCT_CATEGORIES")
                .items(List.of(
                        new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", null, 1, null),
                        new CatalogItem("cat-furniture", "FURNITURE", "Furniture", null, 2, null)))
                .build();
    }

    @Test
    void on_creaUnProductDocumentNuevo_conElNombreDeCategoriaResuelto() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity(productId)));
        when(productDocumentRepository.findById(productId.toString())).thenReturn(Optional.empty());
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES"))
                .thenReturn(Optional.of(productCategoriesCatalog()));

        projection.on(new ProductCreated(ProductId.of(productId),
                com.SpringBoot.domain.product.SKU.of("LAPTOP-001"),
                com.SpringBoot.domain.product.ProductName.of("Laptop Dell XPS 15"),
                com.SpringBoot.domain.shared.Money.of(new BigDecimal("1499.99"), java.util.Currency.getInstance("USD")),
                Instant.now()));

        ArgumentCaptor<ProductDocument> captor = ArgumentCaptor.forClass(ProductDocument.class);
        verify(productDocumentRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(productId.toString());
        assertThat(captor.getValue().getSku()).isEqualTo("LAPTOP-001");
        assertThat(captor.getValue().getCategoryId()).isEqualTo("cat-electronics");
        assertThat(captor.getValue().getCategoryName()).isEqualTo("Electronics");
    }

    @Test
    void on_dejaCategoryNameNulo_cuandoNoHayCatalogoDeCategorias() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity(productId)));
        when(productDocumentRepository.findById(productId.toString())).thenReturn(Optional.empty());
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES")).thenReturn(Optional.empty());

        projection.on(new ProductDeactivated(ProductId.of(productId), Instant.now()));

        ArgumentCaptor<ProductDocument> captor = ArgumentCaptor.forClass(ProductDocument.class);
        verify(productDocumentRepository).save(captor.capture());
        assertThat(captor.getValue().getCategoryName()).isNull();
    }

    @Test
    void on_actualizaElDocumentoExistente_enVezDeCrearUnoNuevo() {
        UUID productId = UUID.randomUUID();
        ProductDocument existing = ProductDocument.builder().id(productId.toString()).sku("OLD-SKU").build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity(productId)));
        when(productDocumentRepository.findById(productId.toString())).thenReturn(Optional.of(existing));
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES")).thenReturn(Optional.empty());

        projection.on(new ProductDeactivated(ProductId.of(productId), Instant.now()));

        verify(productDocumentRepository).save(existing);
        assertThat(existing.getSku()).isEqualTo("LAPTOP-001");
    }

    @Test
    void on_noHaceNada_cuandoElProductoYaNoExisteEnPostgres() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        projection.on(new ProductDeactivated(ProductId.of(productId), Instant.now()));

        verify(productDocumentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
