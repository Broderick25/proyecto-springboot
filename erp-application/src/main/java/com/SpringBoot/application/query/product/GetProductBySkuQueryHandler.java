package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.QueryHandler;
import com.SpringBoot.application.query.product.view.ProductView;
import com.SpringBoot.domain.document.ProductDocument;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GetProductBySkuQueryHandler implements QueryHandler<GetProductBySkuQuery, ProductView> {

    private final ProductDocumentRepository productDocumentRepository;

    public GetProductBySkuQueryHandler(ProductDocumentRepository productDocumentRepository) {
        this.productDocumentRepository = productDocumentRepository;
    }

    @Override
    @Cacheable(cacheNames = ProductCacheNames.BY_SKU, key = "#query.sku()")
    public ProductView handle(GetProductBySkuQuery query) {
        if (query.sku() == null || query.sku().isBlank()) {
            throw new IllegalArgumentException("sku must not be null or blank");
        }

        ProductDocument document = productDocumentRepository.findBySku(query.sku())
                .orElseThrow(() -> new ProductNotFoundException(query.sku()));

        return GetProductByIdQueryHandler.toView(document);
    }
}
