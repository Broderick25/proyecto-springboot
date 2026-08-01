package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    // JOIN FETCH para evitar N+1 al cargar los items de una orden
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.orderProducts WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);
}
