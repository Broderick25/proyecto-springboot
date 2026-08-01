package com.SpringBoot.application.query.category;

import com.SpringBoot.application.query.QueryHandler;
import com.SpringBoot.application.query.category.view.CatalogView;
import com.SpringBoot.application.query.category.view.ItemView;
import com.SpringBoot.domain.document.Catalog;
import com.SpringBoot.domain.document.CatalogTypes;
import com.SpringBoot.domain.repository.CatalogRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListCategoriesQueryHandler implements QueryHandler<ListCategoriesQuery, CatalogView> {

    private final CatalogRepository catalogRepository;

    public ListCategoriesQueryHandler(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Override
    @Cacheable(cacheNames = CategoryCacheNames.CATALOG, key = "'catalog'")
    public CatalogView handle(ListCategoriesQuery query) {
        return catalogRepository.findByCatalogType(CatalogTypes.PRODUCT_CATEGORIES)
                .map(ListCategoriesQueryHandler::toView)
                .orElseGet(() -> new CatalogView(null, CatalogTypes.PRODUCT_CATEGORIES, "Product Categories", null,
                        false, List.of(), null, null));
    }

    private static CatalogView toView(Catalog catalog) {
        List<ItemView> items = catalog.getItems() == null
                ? List.of()
                : catalog.getItems().stream()
                        .map(item -> new ItemView(item.id(), item.code(), item.value(), item.description(),
                                item.displayOrder()))
                        .toList();

        return new CatalogView(
                catalog.getId(),
                catalog.getCatalogType(),
                catalog.getName(),
                catalog.getDescription(),
                Boolean.TRUE.equals(catalog.getActive()),
                items,
                catalog.getCreatedAt(),
                catalog.getUpdatedAt());
    }
}
