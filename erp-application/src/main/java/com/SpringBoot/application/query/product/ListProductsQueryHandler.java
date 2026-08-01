package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.PageView;
import com.SpringBoot.application.query.QueryHandler;
import com.SpringBoot.application.query.product.view.ProductView;
import com.SpringBoot.domain.document.ProductDocument;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListProductsQueryHandler implements QueryHandler<ListProductsQuery, PageView<ProductView>> {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductDocumentRepository productDocumentRepository;

    public ListProductsQueryHandler(ProductDocumentRepository productDocumentRepository) {
        this.productDocumentRepository = productDocumentRepository;
    }

    @Override
    public PageView<ProductView> handle(ListProductsQuery query) {
        if (query.size() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must not exceed " + MAX_PAGE_SIZE);
        }

        Pageable pageable = PageRequest.of(query.page(), query.size());

        Page<ProductDocument> page = query.categoryId() != null
                ? query.onlyActive()
                        ? productDocumentRepository.findByCategoryIdAndActiveTrue(query.categoryId(), pageable)
                        : productDocumentRepository.findByCategoryId(query.categoryId(), pageable)
                : query.onlyActive()
                        ? productDocumentRepository.findByActiveTrue(pageable)
                        : productDocumentRepository.findAll(pageable);

        return new PageView<>(
                page.getContent().stream().map(GetProductByIdQueryHandler::toView).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
