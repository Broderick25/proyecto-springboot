package com.SpringBoot.application.command.category;

import com.SpringBoot.domain.document.Catalog;
import com.SpringBoot.domain.document.CatalogItem;
import com.SpringBoot.domain.document.DuplicateCategoryException;
import com.SpringBoot.domain.repository.CatalogRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCategoryCommandHandlerTest {

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private CreateCategoryCommandHandler handler;

    @Test
    void handle_creaElCatalogo_cuandoAunNoExiste() {
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES")).thenReturn(Optional.empty());

        String categoryId = handler.handle(
                new CreateCategoryCommand("cat-sports", "SPORTS", "Sports", "Sporting goods"));

        ArgumentCaptor<Catalog> captor = ArgumentCaptor.forClass(Catalog.class);
        verify(catalogRepository).save(captor.capture());
        assertThat(categoryId).isEqualTo("cat-sports");
        assertThat(captor.getValue().getCatalogType()).isEqualTo("PRODUCT_CATEGORIES");
        assertThat(captor.getValue().getItems()).hasSize(1);
        assertThat(captor.getValue().getItems().get(0).id()).isEqualTo("cat-sports");
        assertThat(captor.getValue().getItems().get(0).value()).isEqualTo("Sports");
    }

    @Test
    void handle_agregaLaCategoriaAlCatalogoExistente_sinBorrarLasQueYaHabia() {
        List<CatalogItem> existingItems = new ArrayList<>(List.of(
                new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", null, 1, null)));
        Catalog catalog = Catalog.builder()
                .catalogType("PRODUCT_CATEGORIES")
                .items(existingItems)
                .build();
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES")).thenReturn(Optional.of(catalog));

        handler.handle(new CreateCategoryCommand("cat-sports", "SPORTS", "Sports", "Sporting goods"));

        ArgumentCaptor<Catalog> captor = ArgumentCaptor.forClass(Catalog.class);
        verify(catalogRepository).save(captor.capture());
        assertThat(captor.getValue().getItems()).hasSize(2);
        assertThat(captor.getValue().getItems()).extracting(CatalogItem::id)
                .containsExactly("cat-electronics", "cat-sports");
    }

    @Test
    void handle_lanzaDuplicateCategoryException_cuandoElIdYaExiste() {
        List<CatalogItem> existingItems = new ArrayList<>(List.of(
                new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", null, 1, null)));
        Catalog catalog = Catalog.builder()
                .catalogType("PRODUCT_CATEGORIES")
                .items(existingItems)
                .build();
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES")).thenReturn(Optional.of(catalog));

        assertThatThrownBy(() -> handler.handle(
                new CreateCategoryCommand("cat-electronics", "ELECTRONICS", "Electronics", null)))
                .isInstanceOf(DuplicateCategoryException.class);
    }
}
