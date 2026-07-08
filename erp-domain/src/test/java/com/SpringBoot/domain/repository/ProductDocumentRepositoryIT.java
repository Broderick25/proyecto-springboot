package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.document.ProductDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
class ProductDocumentRepositoryIT {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private ProductDocumentRepository productDocumentRepository;

    @Test
    void sku_esUnicoEIndexado() {
        productDocumentRepository.save(ProductDocument.builder()
                .sku("SKU-MONGO-1")
                .name("Laptop Gamer")
                .price(BigDecimal.valueOf(1500))
                .active(true)
                .build());

        Optional<ProductDocument> found = productDocumentRepository.findBySku("SKU-MONGO-1");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Laptop Gamer");
    }

    @Test
    void findByCategoryId_filtraPorCategoria() {
        productDocumentRepository.save(ProductDocument.builder()
                .sku("SKU-MONGO-2")
                .name("Teclado Mecánico RGB")
                .description("Switches azules, retroiluminado")
                .price(BigDecimal.valueOf(80))
                .categoryId("cat-perifericos")
                .active(true)
                .build());

        var results = productDocumentRepository.findByCategoryId("cat-perifericos");

        assertThat(results).hasSize(1);
    }
}
