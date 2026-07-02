package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, UUID> {

    List<OrderProduct> findByOrderId(UUID orderId);
    List<OrderProduct> findByProductId(UUID productId);
}
