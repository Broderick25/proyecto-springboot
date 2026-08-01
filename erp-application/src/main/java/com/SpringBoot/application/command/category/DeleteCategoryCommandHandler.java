package com.SpringBoot.application.command.category;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.application.query.category.CategoryCacheNames;
import com.SpringBoot.domain.document.Catalog;
import com.SpringBoot.domain.document.CatalogItem;
import com.SpringBoot.domain.document.CatalogTypes;
import com.SpringBoot.domain.document.CategoryInUseException;
import com.SpringBoot.domain.document.CategoryNotFoundException;
import com.SpringBoot.domain.repository.CatalogRepository;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeleteCategoryCommandHandler implements CommandHandler<DeleteCategoryCommand, Void> {

    private final CatalogRepository catalogRepository;
    private final ProductDocumentRepository productDocumentRepository;

    public DeleteCategoryCommandHandler(CatalogRepository catalogRepository,
                                         ProductDocumentRepository productDocumentRepository) {
        this.catalogRepository = catalogRepository;
        this.productDocumentRepository = productDocumentRepository;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CategoryCacheNames.CATALOG, allEntries = true),
            @CacheEvict(cacheNames = CategoryCacheNames.BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = CategoryCacheNames.BY_ID, allEntries = true)
    })
    public Void handle(DeleteCategoryCommand command) {
        Catalog catalog = catalogRepository.findByCatalogType(CatalogTypes.PRODUCT_CATEGORIES)
                .orElseThrow(() -> CategoryNotFoundException.byId(command.id()));

        List<CatalogItem> items = catalog.getItems() != null ? catalog.getItems() : List.of();
        boolean exists = items.stream().anyMatch(item -> command.id().equals(item.id()));
        if (!exists) {
            throw CategoryNotFoundException.byId(command.id());
        }

        if (productDocumentRepository.existsByCategoryId(command.id())) {
            throw new CategoryInUseException(command.id());
        }

        catalog.setItems(items.stream().filter(item -> !command.id().equals(item.id())).toList());
        catalogRepository.save(catalog);

        return null;
    }
}
