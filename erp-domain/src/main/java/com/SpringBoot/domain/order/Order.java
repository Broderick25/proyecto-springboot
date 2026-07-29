package com.SpringBoot.domain.order;

import com.SpringBoot.domain.common.AggregateRoot;
import com.SpringBoot.domain.order.events.OrderCancelled;
import com.SpringBoot.domain.order.events.OrderConfirmed;
import com.SpringBoot.domain.order.events.OrderCreated;
import com.SpringBoot.domain.order.events.OrderDelivered;
import com.SpringBoot.domain.order.events.OrderShipped;
import com.SpringBoot.domain.order.events.OrderUpdated;
import com.SpringBoot.domain.shared.AuditInfo;
import com.SpringBoot.domain.shared.Money;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends AggregateRoot<OrderId> {

    private OrderId id;
    private OrderNumber orderNumber;
    private Customer customer;
    private OrderStatus status;
    @Getter(AccessLevel.NONE)
    private List<OrderItem> items;
    private Money totalAmount;
    private AuditInfo auditInfo;

    private Order(OrderId id, OrderNumber orderNumber, Customer customer, OrderStatus status,
                  List<OrderItem> items, Money totalAmount, AuditInfo auditInfo) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.status = status;
        this.items = items;
        this.totalAmount = totalAmount;
        this.auditInfo = auditInfo;
    }

    /**
     * Reconstruye un agregado a partir de estado ya persistido, sin registrar ningún
     * {@link com.SpringBoot.domain.common.DomainEvent} (a diferencia de {@link #create}, que
     * siempre registra {@code OrderCreated}). Uso exclusivo de los mappers de infraestructura
     * al cargar una orden existente.
     */
    public static Order reconstitute(OrderId id, OrderNumber orderNumber, Customer customer, OrderStatus status,
                                      List<OrderItem> items, Money totalAmount, AuditInfo auditInfo) {
        return new Order(id, orderNumber, customer, status, new ArrayList<>(items), totalAmount, auditInfo);
    }

    public static Order create(OrderNumber orderNumber, Customer customer, List<OrderItem> items,
                                String createdBy) {
        if (orderNumber == null) {
            throw new IllegalArgumentException("orderNumber must not be null");
        }
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null");
        }
        validateItems(items);

        List<OrderItem> orderItems = new ArrayList<>(items);
        Money totalAmount = sumItems(orderItems);
        Instant now = Instant.now();

        Order order = new Order(OrderId.generate(), orderNumber, customer, OrderStatus.pending(), orderItems,
                totalAmount, AuditInfo.create(createdBy, now));

        order.registerEvent(new OrderCreated(order.getId(), customer.customerId(), customer.customerName(),
                totalAmount, now));
        return order;
    }

    public void confirm() {
        validateTransition(OrderStatus.confirmed());
        this.status = OrderStatus.confirmed();
        this.auditInfo = this.auditInfo.updateTimestamp();
        registerEvent(new OrderConfirmed(this.id, Instant.now()));
    }

    public void ship() {
        validateTransition(OrderStatus.shipped());
        this.status = OrderStatus.shipped();
        this.auditInfo = this.auditInfo.updateTimestamp();
        registerEvent(new OrderShipped(this.id, Instant.now()));
    }

    public void deliver() {
        validateTransition(OrderStatus.delivered());
        this.status = OrderStatus.delivered();
        this.auditInfo = this.auditInfo.updateTimestamp();
        registerEvent(new OrderDelivered(this.id, Instant.now()));
    }

    public void cancel(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be null or blank");
        }
        validateTransition(OrderStatus.cancelled());
        this.status = OrderStatus.cancelled();
        this.auditInfo = this.auditInfo.updateTimestamp();
        registerEvent(new OrderCancelled(this.id, reason, Instant.now()));
    }

    /**
     * Reemplaza por completo las líneas de la orden. Solo permitido en {@code PENDING}: una vez
     * confirmada, el stock ya se reservó contra el contenido original — cambiar items después
     * dejaría esa reserva inconsistente con lo que realmente se factura/envía.
     */
    public void updateItems(List<OrderItem> newItems) {
        if (!this.status.isPending()) {
            throw new IllegalStateException(
                    "Cannot update items of an order in status " + this.status.value() + "; only PENDING allowed");
        }
        validateItems(newItems);

        this.items = new ArrayList<>(newItems);
        this.totalAmount = sumItems(this.items);
        this.auditInfo = this.auditInfo.updateTimestamp();
        registerEvent(new OrderUpdated(this.id, Instant.now()));
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }
        List<OrderItem> updatedItems = new ArrayList<>(this.items);
        updatedItems.add(item);
        Money updatedTotal = sumItems(updatedItems);

        this.items = updatedItems;
        this.totalAmount = updatedTotal;
    }

    public void removeItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }
        if (!this.items.contains(item)) {
            throw new IllegalArgumentException("item not found in order");
        }

        List<OrderItem> updatedItems = new ArrayList<>(this.items);
        updatedItems.remove(item);
        validateItems(updatedItems);
        Money updatedTotal = sumItems(updatedItems);

        this.items = updatedItems;
        this.totalAmount = updatedTotal;
    }

    public void calculateTotal() {
        this.totalAmount = sumItems(this.items);
    }

    private void validateTransition(OrderStatus nextStatus) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new IllegalStateException(
                    "Cannot transition order from " + this.status.value() + " to " + nextStatus.value());
        }
    }

    private static void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
    }

    private static Money sumItems(List<OrderItem> items) {
        Money total = null;
        for (OrderItem item : items) {
            total = total == null ? item.getSubtotal() : total.add(item.getSubtotal());
        }
        return total;
    }
}
