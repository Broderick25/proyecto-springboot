package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.document.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductDocumentRepository extends MongoRepository<ProductDocument, String> {

    Optional<ProductDocument> findBySku(String sku);
    List<ProductDocument> findByCategoryId(String categoryId);
    List<ProductDocument> findByActiveTrue();
    boolean existsBySku(String sku);
}
