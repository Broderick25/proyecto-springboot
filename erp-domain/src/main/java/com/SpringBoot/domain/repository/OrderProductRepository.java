package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.entity.OrderProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, UUID> {

    List<OrderProduct> findByOrderId(UUID orderId);

    // JOIN FETCH para evitar LazyInitializationException al leer order.* de cada línea
    // (OrderProduct.order es LAZY) — Spring Data deriva el count query quitando el JOIN FETCH.
    @Query("SELECT op FROM OrderProduct op JOIN FETCH op.order WHERE op.product.id = :productId")
    Page<OrderProduct> findByProductIdWithOrder(@Param("productId") UUID productId, Pageable pageable);
}
