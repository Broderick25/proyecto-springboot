package com.SpringBoot.application.query.category;

import com.SpringBoot.application.query.category.view.CatalogView;
import com.SpringBoot.domain.document.Catalog;
import com.SpringBoot.domain.document.CatalogItem;
import com.SpringBoot.domain.repository.CatalogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCategoriesQueryHandlerTest {

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private ListCategoriesQueryHandler handler;

    @Test
    void handle_devuelveElCatalogoConSusItems_cuandoExiste() {
        List<CatalogItem> items = List.of(
                new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", "Electronic devices", 1, null));
        Catalog catalog = Catalog.builder()
                .id("catalog-1")
                .catalogType("PRODUCT_CATEGORIES")
                .name("Product Categories")
                .active(true)
                .items(items)
                .build();
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES")).thenReturn(Optional.of(catalog));

        CatalogView view = handler.handle(new ListCategoriesQuery());

        assertThat(view.id()).isEqualTo("catalog-1");
        assertThat(view.active()).isTrue();
        assertThat(view.items()).hasSize(1);
        assertThat(view.items().get(0).id()).isEqualTo("cat-electronics");
        assertThat(view.items().get(0).value()).isEqualTo("Electronics");
    }

    @Test
    void handle_devuelveUnCatalogoVacio_cuandoAunNoExiste() {
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES")).thenReturn(Optional.empty());

        CatalogView view = handler.handle(new ListCategoriesQuery());

        assertThat(view.items()).isEmpty();
        assertThat(view.active()).isFalse();
    }
}
