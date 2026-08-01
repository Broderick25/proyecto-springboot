package com.SpringBoot.application.query.category;

import com.SpringBoot.application.query.category.view.ItemView;
import com.SpringBoot.domain.document.Catalog;
import com.SpringBoot.domain.document.CatalogItem;
import com.SpringBoot.domain.document.CategoryNotFoundException;
import com.SpringBoot.domain.repository.CatalogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCategoryByCodeQueryHandlerTest {

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private GetCategoryByCodeQueryHandler handler;

    private static Catalog catalogWith(CatalogItem... items) {
        return Catalog.builder()
                .catalogType("PRODUCT_CATEGORIES")
                .items(List.of(items))
                .build();
    }

    @Test
    void handle_devuelveElItem_cuandoElCodeExiste() {
        CatalogItem item = new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", "desc", 1, null);
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES"))
                .thenReturn(Optional.of(catalogWith(item)));

        ItemView view = handler.handle(new GetCategoryByCodeQuery("ELECTRONICS"));

        assertThat(view.id()).isEqualTo("cat-electronics");
        assertThat(view.code()).isEqualTo("ELECTRONICS");
    }

    @Test
    void handle_lanzaCategoryNotFoundException_cuandoElCodeNoExiste() {
        CatalogItem item = new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", "desc", 1, null);
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES"))
                .thenReturn(Optional.of(catalogWith(item)));

        assertThatThrownBy(() -> handler.handle(new GetCategoryByCodeQuery("NO_EXISTE")))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void handle_lanzaCategoryNotFoundException_cuandoElCatalogoNoExiste() {
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetCategoryByCodeQuery("ELECTRONICS")))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoElCodeEsBlanco() {
        assertThatThrownBy(() -> handler.handle(new GetCategoryByCodeQuery("  ")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
