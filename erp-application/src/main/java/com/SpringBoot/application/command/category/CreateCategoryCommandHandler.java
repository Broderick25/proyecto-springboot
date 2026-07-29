package com.SpringBoot.application.command.category;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.domain.document.Catalog;
import com.SpringBoot.domain.document.CatalogItem;
import com.SpringBoot.domain.document.CatalogTypes;
import com.SpringBoot.domain.document.DuplicateCategoryException;
import com.SpringBoot.domain.repository.CatalogRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Agrega una categoría nueva al catálogo {@code PRODUCT_CATEGORIES} (Mongo). No existe un
 * agregado DDD para "categoría" (a diferencia de Product/Order) — {@link Catalog} es un
 * documento plano sin invariantes de negocio propias, así que este handler lo manipula
 * directamente en vez de pasar por un mapper agregado↔entidad.
 */
@Service
public class CreateCategoryCommandHandler implements CommandHandler<CreateCategoryCommand, String> {

    private final CatalogRepository catalogRepository;

    public CreateCategoryCommandHandler(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Override
    public String handle(CreateCategoryCommand command) {
        Catalog catalog = catalogRepository.findByCatalogType(CatalogTypes.PRODUCT_CATEGORIES)
                .orElseGet(() -> Catalog.builder()
                        .catalogType(CatalogTypes.PRODUCT_CATEGORIES)
                        .name("Product Categories")
                        .active(true)
                        .items(new ArrayList<>())
                        .build());

        List<CatalogItem> items = new ArrayList<>(catalog.getItems() != null ? catalog.getItems() : List.of());

        boolean alreadyExists = items.stream().anyMatch(item -> command.id().equals(item.id()));
        if (alreadyExists) {
            throw new DuplicateCategoryException(command.id());
        }

        items.add(new CatalogItem(command.id(), command.code(), command.name(), command.description(),
                items.size() + 1, null));
        catalog.setItems(items);

        catalogRepository.save(catalog);

        return command.id();
    }
}
