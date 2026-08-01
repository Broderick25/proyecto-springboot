package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.QueryHandler;
import com.SpringBoot.application.query.product.view.ProductView;
import com.SpringBoot.domain.document.ProductDocument;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GetProductByIdQueryHandler implements QueryHandler<GetProductByIdQuery, ProductView> {

    private final ProductDocumentRepository productDocumentRepository;

    public GetProductByIdQueryHandler(ProductDocumentRepository productDocumentRepository) {
        this.productDocumentRepository = productDocumentRepository;
    }

    @Override
    @Cacheable(cacheNames = ProductCacheNames.BY_ID, key = "#query.productId().toString()")
    public ProductView handle(GetProductByIdQuery query) {
        ProductDocument document = productDocumentRepository.findById(query.productId().toString())
                .orElseThrow(() -> new ProductNotFoundException(query.productId()));

        return toView(document);
    }

    static ProductView toView(ProductDocument document) {
        return new ProductView(
                document.getId(),
                document.getSku(),
                document.getName(),
                document.getDescription(),
                document.getPrice(),
                document.getCurrency(),
                document.getStock(),
                document.getCategoryId(),
                document.getCategoryName(),
                document.getImageUrl(),
                Boolean.TRUE.equals(document.getActive()),
                document.getTags(),
                document.getSpecifications());
    }
}
