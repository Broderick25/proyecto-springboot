package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.QueryHandler;
import com.SpringBoot.application.query.SliceView;
import com.SpringBoot.application.query.product.view.ProductView;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
public class SearchProductsQueryHandler implements QueryHandler<SearchProductsQuery, SliceView<ProductView>> {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductDocumentRepository productDocumentRepository;

    public SearchProductsQueryHandler(ProductDocumentRepository productDocumentRepository) {
        this.productDocumentRepository = productDocumentRepository;
    }

    @Override
    public SliceView<ProductView> handle(SearchProductsQuery query) {
        if (query.text() == null || query.text().isBlank()) {
            throw new IllegalArgumentException("text must not be null or blank");
        }
        if (query.size() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must not exceed " + MAX_PAGE_SIZE);
        }

        Pageable pageable = PageRequest.of(query.page(), query.size());
        Slice<ProductView> slice = productDocumentRepository.searchByText(query.text(), pageable)
                .map(GetProductByIdQueryHandler::toView);

        return new SliceView<>(slice.getContent(), slice.getNumber(), slice.getSize(), slice.hasNext());
    }
}
