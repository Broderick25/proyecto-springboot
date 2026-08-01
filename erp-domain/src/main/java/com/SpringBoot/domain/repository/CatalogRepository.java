package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.document.Catalog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatalogRepository extends MongoRepository<Catalog, String> {

    Optional<Catalog> findByCatalogType(String catalogType);
    boolean existsByCatalogTypeAndItemsId(String catalogType, String itemId);
}
