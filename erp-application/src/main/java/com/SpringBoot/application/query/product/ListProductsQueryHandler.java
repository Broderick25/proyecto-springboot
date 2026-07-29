package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.QueryHandler;
import com.SpringBoot.application.query.product.view.ProductView;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListProductsQueryHandler implements QueryHandler<ListProductsQuery, List<ProductView>> {

    private final ProductDocumentRepository productDocumentRepository;

    public ListProductsQueryHandler(ProductDocumentRepository productDocumentRepository) {
        this.productDocumentRepository = productDocumentRepository;
    }

    @Override
    public List<ProductView> handle(ListProductsQuery query) {
        var documents = query.onlyActive()
                ? productDocumentRepository.findByActiveTrue()
                : productDocumentRepository.findAll();

        return documents.stream()
                .map(GetProductByIdQueryHandler::toView)
                .toList();
    }
}
