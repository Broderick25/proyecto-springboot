package com.SpringBoot.application.query.category;

import com.SpringBoot.application.query.QueryHandler;
import com.SpringBoot.application.query.category.view.ItemView;
import com.SpringBoot.domain.document.CatalogTypes;
import com.SpringBoot.domain.document.CategoryNotFoundException;
import com.SpringBoot.domain.repository.CatalogRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Busca un item de categoría por {@code id} dentro del catálogo {@code PRODUCT_CATEGORIES}.
 * Complementa a {@link GetCategoryByCodeQueryHandler} — {@code Create}/{@code Update}/
 * {@code DeleteCategoryCommand} direccionan por {@code id}, así que hacía falta una forma de
 * releer ese mismo recurso por el mismo identificador.
 */
@Service
public class GetCategoryByIdQueryHandler implements QueryHandler<GetCategoryByIdQuery, ItemView> {

    private final CatalogRepository catalogRepository;

    public GetCategoryByIdQueryHandler(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Override
    @Cacheable(cacheNames = CategoryCacheNames.BY_ID, key = "#query.id()")
    public ItemView handle(GetCategoryByIdQuery query) {
        if (query.id() == null || query.id().isBlank()) {
            throw new IllegalArgumentException("id must not be null or blank");
        }

        return catalogRepository.findByCatalogType(CatalogTypes.PRODUCT_CATEGORIES)
                .flatMap(catalog -> catalog.getItems() == null
                        ? Optional.empty()
                        : catalog.getItems().stream()
                                .filter(item -> query.id().equals(item.id()))
                                .findFirst())
                .map(item -> new ItemView(item.id(), item.code(), item.value(), item.description(),
                        item.displayOrder()))
                .orElseThrow(() -> CategoryNotFoundException.byId(query.id()));
    }
}
