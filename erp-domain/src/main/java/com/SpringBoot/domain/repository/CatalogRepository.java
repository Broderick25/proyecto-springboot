package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.document.Catalog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogRepository extends MongoRepository<Catalog, String> {

    Optional<Catalog> findByCatalogType(String catalogType);
    List<Catalog> findByActiveTrue();
    boolean existsByCatalogType(String catalogType);
}
