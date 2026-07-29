package com.SpringBoot.application.command.order;

import com.SpringBoot.domain.entity.OrderProduct;
import com.SpringBoot.domain.order.Customer;
import com.SpringBoot.domain.order.OrderId;
import com.SpringBoot.domain.order.OrderItem;
import com.SpringBoot.domain.order.OrderItemId;
import com.SpringBoot.domain.order.OrderNumber;
import com.SpringBoot.domain.order.OrderStatus;
import com.SpringBoot.domain.product.ProductId;
import com.SpringBoot.domain.shared.AuditInfo;
import com.SpringBoot.domain.shared.CustomerId;
import com.SpringBoot.domain.shared.Money;
import com.SpringBoot.domain.shared.Quantity;

import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Map;

/**
 * Mapea entre la entidad JPA ({@code domain.entity.Order}, lo que realmente se persiste en
 * Postgres) y el agregado DDD ({@code domain.order.Order}, donde vive la máquina de estados).
 *
 * <p>A diferencia de {@code ProductMapper}, aquí no hace falta ningún valor de relleno: la
 * entidad JPA de Order sí tiene {@code currency} y {@code createdBy}, así que se mapean tal
 * cual están persistidos.
 */
public final class OrderMapper {

    private OrderMapper() {
    }

    public static com.SpringBoot.domain.order.Order toAggregate(com.SpringBoot.domain.entity.Order entity) {
        Currency currency = Currency.getInstance(entity.getCurrency());

        List<OrderItem> items = entity.getOrderProducts().stream()
                .map(op -> toOrderItem(op, currency))
                .toList();

        return com.SpringBoot.domain.order.Order.reconstitute(
                OrderId.of(entity.getId()),
                OrderNumber.of(entity.getOrderNumber()),
                new Customer(CustomerId.of(entity.getCustomerId()), entity.getCustomerName()),
                OrderStatus.of(entity.getStatus().name()),
                items,
                Money.of(entity.getTotalAmount(), currency),
                AuditInfo.create(entity.getCreatedBy(),
                        entity.getOrderDate().atZone(ZoneId.systemDefault()).toInstant()));
    }

    /**
     * Construye la entidad JPA para una orden NUEVA, incluyendo sus líneas. {@code productsById}
     * debe traer, para cada {@link ProductId} referenciado por un {@link OrderItem} del
     * agregado, la entidad {@code Product} ya persistida (evita volver a consultarla).
     */
    public static com.SpringBoot.domain.entity.Order toNewEntity(
            com.SpringBoot.domain.order.Order aggregate,
            Map<ProductId, com.SpringBoot.domain.entity.Product> productsById) {
        com.SpringBoot.domain.entity.Order entity = com.SpringBoot.domain.entity.Order.builder()
                .id(aggregate.getId().value())
                .orderNumber(aggregate.getOrderNumber().value())
                .customerId(aggregate.getCustomer().customerId().value())
                .customerName(aggregate.getCustomer().customerName())
                .createdBy(aggregate.getAuditInfo().createdBy())
                .status(com.SpringBoot.domain.entity.OrderStatus.valueOf(aggregate.getStatus().value()))
                .currency(aggregate.getTotalAmount().currency().getCurrencyCode())
                .build();

        for (OrderItem item : aggregate.getItems()) {
            entity.addOrderProduct(OrderProduct.builder()
                    .product(productsById.get(item.getProductReference()))
                    .productName(item.getProductName())
                    .quantity(item.getQuantity().value())
                    .unitPrice(item.getUnitPrice().amount())
                    .subtotal(item.getSubtotal().amount())
                    .build());
        }

        return entity;
    }

    public static void copyToEntity(com.SpringBoot.domain.order.Order aggregate,
                                     com.SpringBoot.domain.entity.Order entity) {
        entity.setStatus(com.SpringBoot.domain.entity.OrderStatus.valueOf(aggregate.getStatus().value()));
    }

    /**
     * Reemplaza por completo las líneas de {@code entity} por las de {@code aggregate}. Se apoya
     * en {@code orphanRemoval=true} de {@code Order.orderProducts}: al limpiar la colección y
     * volver a agregar, Hibernate borra las filas viejas y recalcula el total.
     */
    public static void syncItems(com.SpringBoot.domain.order.Order aggregate,
                                  com.SpringBoot.domain.entity.Order entity,
                                  Map<ProductId, com.SpringBoot.domain.entity.Product> productsById) {
        entity.getOrderProducts().clear();

        for (OrderItem item : aggregate.getItems()) {
            entity.addOrderProduct(OrderProduct.builder()
                    .product(productsById.get(item.getProductReference()))
                    .productName(item.getProductName())
                    .quantity(item.getQuantity().value())
                    .unitPrice(item.getUnitPrice().amount())
                    .subtotal(item.getSubtotal().amount())
                    .build());
        }
    }

    private static OrderItem toOrderItem(OrderProduct orderProduct, Currency currency) {
        return OrderItem.reconstitute(
                OrderItemId.of(orderProduct.getId()),
                ProductId.of(orderProduct.getProduct().getId()),
                orderProduct.getProductName(),
                Quantity.of(orderProduct.getQuantity()),
                Money.of(orderProduct.getUnitPrice(), currency),
                Money.of(orderProduct.getSubtotal(), currency));
    }
}
