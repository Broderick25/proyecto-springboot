package com.SpringBoot.application.command.category;

import com.SpringBoot.domain.document.Catalog;
import com.SpringBoot.domain.document.CatalogItem;
import com.SpringBoot.domain.document.CategoryInUseException;
import com.SpringBoot.domain.document.CategoryNotFoundException;
import com.SpringBoot.domain.repository.CatalogRepository;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCategoryCommandHandlerTest {

    @Mock
    private CatalogRepository catalogRepository;

    @Mock
    private ProductDocumentRepository productDocumentRepository;

    @InjectMocks
    private DeleteCategoryCommandHandler handler;

    private static Catalog catalogWith(CatalogItem... items) {
        return Catalog.builder()
                .catalogType("PRODUCT_CATEGORIES")
                .items(new ArrayList<>(List.of(items)))
                .build();
    }

    @Test
    void handle_eliminaElItem_cuandoNingunProductoLoUsa() {
        CatalogItem toDelete = new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", null, 1, null);
        CatalogItem keep = new CatalogItem("cat-furniture", "FURNITURE", "Furniture", null, 2, null);
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES"))
                .thenReturn(Optional.of(catalogWith(toDelete, keep)));
        when(productDocumentRepository.existsByCategoryId("cat-electronics")).thenReturn(false);

        handler.handle(new DeleteCategoryCommand("cat-electronics"));

        ArgumentCaptor<Catalog> captor = ArgumentCaptor.forClass(Catalog.class);
        verify(catalogRepository).save(captor.capture());
        assertThat(captor.getValue().getItems()).extracting(CatalogItem::id).containsExactly("cat-furniture");
    }

    @Test
    void handle_lanzaCategoryInUseException_cuandoHayProductosAsociados() {
        CatalogItem item = new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", null, 1, null);
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES"))
                .thenReturn(Optional.of(catalogWith(item)));
        when(productDocumentRepository.existsByCategoryId("cat-electronics")).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(new DeleteCategoryCommand("cat-electronics")))
                .isInstanceOf(CategoryInUseException.class);

        verify(catalogRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handle_lanzaCategoryNotFoundException_cuandoElCatalogoNoExiste() {
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new DeleteCategoryCommand("cat-electronics")))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void handle_lanzaCategoryNotFoundException_cuandoElIdNoExisteEnElCatalogo() {
        CatalogItem item = new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", null, 1, null);
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES"))
                .thenReturn(Optional.of(catalogWith(item)));

        assertThatThrownBy(() -> handler.handle(new DeleteCategoryCommand("cat-no-existe")))
                .isInstanceOf(CategoryNotFoundException.class);
    }
}
