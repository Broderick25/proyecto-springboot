package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductDocumentRepository extends MongoRepository<ProductDocument, String> {

    Optional<ProductDocument> findBySku(String sku);
    Page<ProductDocument> findByCategoryId(String categoryId, Pageable pageable);
    Page<ProductDocument> findByCategoryIdAndActiveTrue(String categoryId, Pageable pageable);
    Page<ProductDocument> findByActiveTrue(Pageable pageable);
    boolean existsBySku(String sku);
    boolean existsByCategoryId(String categoryId);

    @Query(value = "{ $text: { $search: ?0 }, active: true }", sort = "{ score: { $meta: 'textScore' } }")
    Slice<ProductDocument> searchByText(String text, Pageable pageable);
}
