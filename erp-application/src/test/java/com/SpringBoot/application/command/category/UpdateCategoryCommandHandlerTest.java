package com.SpringBoot.application.command.category;

import com.SpringBoot.domain.document.Catalog;
import com.SpringBoot.domain.document.CatalogItem;
import com.SpringBoot.domain.document.CategoryNotFoundException;
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
class UpdateCategoryCommandHandlerTest {

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private UpdateCategoryCommandHandler handler;

    private static Catalog catalogWith(CatalogItem... items) {
        return Catalog.builder()
                .catalogType("PRODUCT_CATEGORIES")
                .items(new ArrayList<>(List.of(items)))
                .build();
    }

    @Test
    void handle_actualizaElItem_manteniendoIdYDisplayOrder() {
        CatalogItem existing = new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", "old desc", 1, null);
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES"))
                .thenReturn(Optional.of(catalogWith(existing)));

        handler.handle(new UpdateCategoryCommand("cat-electronics", "ELEC", "Electronics & Tech", "new desc"));

        ArgumentCaptor<Catalog> captor = ArgumentCaptor.forClass(Catalog.class);
        verify(catalogRepository).save(captor.capture());
        CatalogItem updated = captor.getValue().getItems().get(0);
        assertThat(updated.id()).isEqualTo("cat-electronics");
        assertThat(updated.code()).isEqualTo("ELEC");
        assertThat(updated.value()).isEqualTo("Electronics & Tech");
        assertThat(updated.description()).isEqualTo("new desc");
        assertThat(updated.displayOrder()).isEqualTo(1);
    }

    @Test
    void handle_lanzaCategoryNotFoundException_cuandoElCatalogoNoExiste() {
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(
                new UpdateCategoryCommand("cat-electronics", "ELEC", "Electronics", null)))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void handle_lanzaCategoryNotFoundException_cuandoElIdNoExisteEnElCatalogo() {
        CatalogItem existing = new CatalogItem("cat-electronics", "ELECTRONICS", "Electronics", null, 1, null);
        when(catalogRepository.findByCatalogType("PRODUCT_CATEGORIES"))
                .thenReturn(Optional.of(catalogWith(existing)));

        assertThatThrownBy(() -> handler.handle(
                new UpdateCategoryCommand("cat-no-existe", "X", "X", null)))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoElCodeEsBlanco() {
        assertThatThrownBy(() -> handler.handle(
                new UpdateCategoryCommand("cat-electronics", "  ", "Electronics", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoElValueEsBlanco() {
        assertThatThrownBy(() -> handler.handle(
                new UpdateCategoryCommand("cat-electronics", "ELEC", "  ", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
