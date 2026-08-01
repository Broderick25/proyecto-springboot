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
class GetCategoryByIdQueryHandlerTest {

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private GetCategoryByIdQueryHandler handler;

    private static Catalog catalogWith(CatalogItem... items) {
        return Catalog.builder()
                .catalogType("PRODUCT_CATEGORIES")
                .items(List.of(items))
                .build();
    }

    @Test
    void handle_devuelveElItem_cuandoElIdExiste() {
        CatalogItem item = new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", "desc", 1, null);
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES"))
                .thenReturn(Optional.of(catalogWith(item)));

        ItemView view = handler.handle(new GetCategoryByIdQuery("cat-electronics"));

        assertThat(view.id()).isEqualTo("cat-electronics");
        assertThat(view.code()).isEqualTo("ELECTRONICS");
    }

    @Test
    void handle_lanzaCategoryNotFoundException_cuandoElIdNoExiste() {
        CatalogItem item = new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", "desc", 1, null);
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES"))
                .thenReturn(Optional.of(catalogWith(item)));

        assertThatThrownBy(() -> handler.handle(new GetCategoryByIdQuery("cat-no-existe")))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void handle_lanzaCategoryNotFoundException_cuandoElCatalogoNoExiste() {
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetCategoryByIdQuery("cat-electronics")))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoElIdEsBlanco() {
        assertThatThrownBy(() -> handler.handle(new GetCategoryByIdQuery("  ")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
