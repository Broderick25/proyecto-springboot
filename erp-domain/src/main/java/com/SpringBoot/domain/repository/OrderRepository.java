package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(OrderStatus status);

    // JOIN FETCH para evitar N+1 al cargar los items de una orden
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.orderProducts WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);
}
