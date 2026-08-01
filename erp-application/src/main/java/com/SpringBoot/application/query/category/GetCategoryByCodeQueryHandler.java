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
 * Busca un item de categoría por {@code code} dentro del catálogo {@code PRODUCT_CATEGORIES}.
 * No hay una forma barata de proyectar un solo elemento del array {@code items} desde Mongo con
 * una derived query — se reutiliza {@link CatalogRepository#findByCatalogType}, ya cargado
 * completo por {@link ListCategoriesQueryHandler}, y se filtra en memoria (el catálogo es un
 * único documento con pocos items, no una colección grande).
 */
@Service
public class GetCategoryByCodeQueryHandler implements QueryHandler<GetCategoryByCodeQuery, ItemView> {

    private final CatalogRepository catalogRepository;

    public GetCategoryByCodeQueryHandler(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Override
    @Cacheable(cacheNames = CategoryCacheNames.BY_CODE, key = "#query.code()")
    public ItemView handle(GetCategoryByCodeQuery query) {
        if (query.code() == null || query.code().isBlank()) {
            throw new IllegalArgumentException("code must not be null or blank");
        }

        return catalogRepository.findByCatalogType(CatalogTypes.PRODUCT_CATEGORIES)
                .flatMap(catalog -> catalog.getItems() == null
                        ? Optional.empty()
                        : catalog.getItems().stream()
                                .filter(item -> query.code().equals(item.code()))
                                .findFirst())
                .map(item -> new ItemView(item.id(), item.code(), item.value(), item.description(),
                        item.displayOrder()))
                .orElseThrow(() -> CategoryNotFoundException.byCode(query.code()));
    }
}
