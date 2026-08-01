package com.SpringBoot.application.command.category;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.application.query.category.CategoryCacheNames;
import com.SpringBoot.domain.document.Catalog;
import com.SpringBoot.domain.document.CatalogItem;
import com.SpringBoot.domain.document.CatalogTypes;
import com.SpringBoot.domain.document.CategoryNotFoundException;
import com.SpringBoot.domain.repository.CatalogRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UpdateCategoryCommandHandler implements CommandHandler<UpdateCategoryCommand, Void> {

    private final CatalogRepository catalogRepository;

    public UpdateCategoryCommandHandler(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CategoryCacheNames.CATALOG, allEntries = true),
            @CacheEvict(cacheNames = CategoryCacheNames.BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = CategoryCacheNames.BY_ID, allEntries = true)
    })
    public Void handle(UpdateCategoryCommand command) {
        if (command.code() == null || command.code().isBlank()) {
            throw new IllegalArgumentException("code must not be null or blank");
        }
        if (command.value() == null || command.value().isBlank()) {
            throw new IllegalArgumentException("value must not be null or blank");
        }

        Catalog catalog = catalogRepository.findByCatalogType(CatalogTypes.PRODUCT_CATEGORIES)
                .orElseThrow(() -> CategoryNotFoundException.byId(command.id()));

        List<CatalogItem> items = new ArrayList<>(catalog.getItems() != null ? catalog.getItems() : List.of());
        int index = indexOf(items, command.id());
        if (index < 0) {
            throw CategoryNotFoundException.byId(command.id());
        }

        CatalogItem existing = items.get(index);
        items.set(index, new CatalogItem(existing.id(), command.code(), command.value(), command.description(),
                existing.displayOrder(), existing.metadata()));
        catalog.setItems(items);

        catalogRepository.save(catalog);

        return null;
    }

    private static int indexOf(List<CatalogItem> items, String id) {
        for (int i = 0; i < items.size(); i++) {
            if (id.equals(items.get(i).id())) {
                return i;
            }
        }
        return -1;
    }
}
