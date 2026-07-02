package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);
    List<Product> findByCategoryId(String categoryId);
    List<Product> findByActiveTrue();
    boolean existsBySku(String sku);
}
